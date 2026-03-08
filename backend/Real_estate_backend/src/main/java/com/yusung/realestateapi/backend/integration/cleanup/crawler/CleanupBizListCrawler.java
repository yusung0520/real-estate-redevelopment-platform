package com.yusung.realestateapi.backend.integration.cleanup.crawler;

import com.yusung.realestateapi.backend.integration.cleanup.application.CleanupProgressUpsertService;
import com.yusung.realestateapi.backend.integration.cleanup.infra.CleanupCafeUpsertJdbcRepository;
import com.yusung.realestateapi.backend.integration.cleanup.infra.CleanupCafeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupBizListCrawler {

    private static final String LIST_URL = "https://cleanup.seoul.go.kr/cleanup/bsnssttus/lsubBsnsSttus.do";
    private static final Pattern CAFE_URL_PATTERN = Pattern.compile("cafeOpenPopup\\('([^']+)'\\)");
    private static final Pattern BSNS_PK_PATTERN  = Pattern.compile("mapOpenPopup\\('([0-9]+AGZ[A-Z0-9]+)'\\)");
    private static final String MAIN_URL_PREFIX = "https://cleanup.seoul.go.kr/cleanup/cafe/mainIndx.do?cafeUrl=";
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    private final CleanupCafeUpsertJdbcRepository upsertJdbcRepository;
    private final CleanupCafeRepository cleanupCafeRepository;
    private final CleanupSeoulStageCrawler stageCrawler;
    private final CleanupProgressUpsertService progressService;

    public CrawlAllResult crawlAllPages(String signguCode, int sleepMs, int maxEmptyPagesGuard, int maxPagesGuard) {
        return crawlAllPages(signguCode, sleepMs, maxPagesGuard);
    }

    public CrawlPageResult crawlOnePage(String signguCode, int cpage) {
        Connection.Response response = fetchListResponse(signguCode, cpage);
        if (response == null) return new CrawlPageResult(signguCode, cpage, 0, 0, 0);

        List<CleanupCafeUpsertJdbcRepository.CafeRow> rows = parse(response.body(), signguCode);
        int upserted = upsertJdbcRepository.batchUpsert(rows);

        deepCrawlStages(rows, response.cookies());

        return new CrawlPageResult(normSigngu(signguCode), cpage, rows.size(), upserted, 0);
    }

    public CrawlAllResult crawlAllPages(String signguCode, int sleepMs, int maxPagesGuard) {
        String s = normSigngu(signguCode);
        int page = 1;
        int totalUpserted = 0;

        while (page <= maxPagesGuard) {
            Connection.Response response = fetchListResponse(signguCode, page);
            if (response == null) break;

            List<CleanupCafeUpsertJdbcRepository.CafeRow> rows = parse(response.body(), signguCode);
            if (rows.isEmpty()) break;

            totalUpserted += upsertJdbcRepository.batchUpsert(rows);
            deepCrawlStages(rows, response.cookies());

            page++;
            sleep(sleepMs);
        }
        return new CrawlAllResult(s, page - 1, 0, totalUpserted);
    }

    private void deepCrawlStages(List<CleanupCafeUpsertJdbcRepository.CafeRow> rows, Map<String, String> cookies) {
        for (var row : rows) {
            try {
                var cafeOpt = cleanupCafeRepository.findByCafeUrl(row.cafeUrl());
                if (cafeOpt.isEmpty()) continue;
                var cafe = cafeOpt.get();

                // 1. 상세 페이지 접속 (세션 및 리퍼러 강화)
                String targetUrl = "https://cleanup.seoul.go.kr/assc/scrin-bbs/execute.do?cafeId=" + row.bsnsPk();
                Document detailDoc = Jsoup.connect(targetUrl)
                        .userAgent(UA)
                        .cookies(cookies)
                        .referrer("https://cleanup.seoul.go.kr/")
                        .get();

                // 2. 헤더 공식 명칭 추출 (i 태그와 h4.title 병행 확인)
                String officialBizName = "";
                Element logoTag = detailDoc.select(".logoTxt i").first();
                if (logoTag != null) {
                    officialBizName = logoTag.text().trim();
                } else {
                    officialBizName = detailDoc.select("h4.title").text().trim();
                }

                log.info("[DEEP-LOG] 구역: {} | 리스트명: {} | 헤더명: {}",
                        cafe.getAreaName(), row.officialBizName(), officialBizName);

                if (!officialBizName.isEmpty()) {
                    cafe.setOfficialBizName(officialBizName);
                }

                // 리스트에서 가져온 사업장명(조합명) 저장
                cafe.setBusinessName(row.officialBizName());
                cleanupCafeRepository.save(cafe);

                if (cafe.getAreaId() == null) continue;

                // 3. 단계별 날짜 크롤링
                List<CleanupSeoulStageCrawler.StageItem> stages = stageCrawler.crawlStages(row.bsnsPk(), cookies);
                if (stages.isEmpty()) continue;

                stages.stream()
                        .filter(CleanupSeoulStageCrawler.StageItem::isCurrent)
                        .findFirst()
                        .ifPresent(s -> {
                            cafe.setStageName(s.stepName());
                            cleanupCafeRepository.save(cafe);
                        });

                List<CleanupProgressUpsertService.Item> progressItems = stages.stream()
                        .map(s -> new CleanupProgressUpsertService.Item(s.stepNo(), s.stepName(), s.eventDate()))
                        .toList();

                Integer currentNo = stages.stream()
                        .filter(CleanupSeoulStageCrawler.StageItem::isCurrent)
                        .map(CleanupSeoulStageCrawler.StageItem::stepNo)
                        .findFirst().orElse(null);

                progressService.upsertAll(cafe.getAreaId(), progressItems, row.mainUrl(), currentNo);

            } catch (Exception e) {
                log.error("[ERROR] 상세 크롤링 중 에러 - {}: {}", row.areaName(), e.getMessage());
            }
        }
    }

    private Connection.Response fetchListResponse(String signguCode, int cpage) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(LIST_URL)
                    .queryParam("scupBsnsSttus.signguCode", normSigngu(signguCode))
                    .queryParam("cpage", String.valueOf(cpage))
                    .build(true).toUriString();

            return Jsoup.connect(url).userAgent(UA).method(Connection.Method.POST).timeout(20000).execute();
        } catch (Exception e) {
            log.error("리스트 페이지 호출 실패: {}", e.getMessage());
            return null;
        }
    }

    private List<CleanupCafeUpsertJdbcRepository.CafeRow> parse(String html, String signguCode) {
        Document doc = Jsoup.parse(html);
        Elements trs = doc.select("table tbody tr");
        List<CleanupCafeUpsertJdbcRepository.CafeRow> out = new ArrayList<>();

        for (Element tr : trs) {
            String rowHtml = tr.outerHtml();
            String cafeUrl = extractFirst(CAFE_URL_PATTERN, rowHtml);
            String bsnsPk = extractFirst(BSNS_PK_PATTERN, rowHtml);

            if (cafeUrl == null) continue;

            Elements tds = tr.select("td");
            if (tds.size() < 6) continue;

            /**
             * ✅ 로그 분석 결과 기반 인덱스 재설정
             * tds.get(1): 자치구 (강남구)
             * tds.get(3): 운영주체 (잠원동 61-2 조합설립추진위원회 등) -> businessName
             * tds.get(4): 사업장명/주소 (잠원동 61-2) -> areaName
             * tds.get(5): 진행단계 (조합설립인가) -> stageName
             */
            String businessName = tds.get(3).text().trim();
            String areaName = tds.get(4).text().trim();
            String stageName = tds.get(5).text().trim();

            if (!areaName.isEmpty()) {
                out.add(new CleanupCafeUpsertJdbcRepository.CafeRow(
                        cafeUrl, areaName, bsnsPk,
                        MAIN_URL_PREFIX + cafeUrl, stageName, normSigngu(signguCode),
                        businessName
                ));
            }
        }
        return out;
    }

    private String extractFirst(Pattern p, String text) {
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1) : null;
    }

    private String normSigngu(String signguCode) { return (signguCode == null) ? "" : signguCode.trim(); }
    private void sleep(int ms) { try { Thread.sleep(ms); } catch (InterruptedException e) {} }

    public record CrawlPageResult(String signguCode, int page, int items, int upserted, long elapsedMs) {}
    public record CrawlAllResult(String signguCode, int pagesCrawled, int totalItems, int totalUpserted) {}
}