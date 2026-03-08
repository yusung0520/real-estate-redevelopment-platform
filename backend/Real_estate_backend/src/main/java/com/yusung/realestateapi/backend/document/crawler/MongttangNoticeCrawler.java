package com.yusung.realestateapi.backend.document.crawler;

import com.yusung.realestateapi.backend.document.domain.SourceDocument;
import com.yusung.realestateapi.backend.document.infra.SourceDocumentRepository;
import com.yusung.realestateapi.backend.common.util.SslIgnoreUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;

@Service
public class MongttangNoticeCrawler {

    private static final String SOURCE = "info_mongttang";
    private static final String BASE = "https://cleanup.seoul.go.kr";
    private static final String LIST_URL =
            "https://cleanup.seoul.go.kr/cleanup/bbs/lscr.do?bbsClCode=100&ctgryClCode=100";

    private final SourceDocumentRepository docRepo;

    public MongttangNoticeCrawler(SourceDocumentRepository docRepo) {
        this.docRepo = docRepo;
    }

    public int crawlListPage(int cpage) throws IOException {

        SslIgnoreUtil.ignoreSsl();

        // 목록은 cpage 파라미터가 붙는 형태가 일반적이라, 우선 이렇게 붙여서 요청
        String url = LIST_URL + "&cpage=" + cpage;

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .referrer("https://cleanup.seoul.go.kr")
                .timeout(15_000)
                .followRedirects(true)
                .ignoreHttpErrors(true)
                .get();


        int saved = 0;

        // ✅ 핵심: 상세 링크는 보통 /cleanup/bbs/vscr.do?bbs.bbsSn=... 형태
        for (Element a : doc.select("a[href*=/cleanup/bbs/vscr.do]")) {
            String href = a.attr("href").trim();
            if (href.isEmpty()) continue;

            String detailUrl = href.startsWith("http") ? href : (BASE + href);

            // 중복 방지
            if (docRepo.existsBySourceAndDetailUrl(SOURCE, detailUrl)) continue;

            String title = a.text().trim();
            if (title.isEmpty()) continue;

            // ✅ 1) 포함돼야 하는 키워드(최소 1개)
            boolean hasCore = title.matches(".*(정비구역|재개발|재건축|조합|추진위|사업시행|관리처분|정비사업).*");
            if (!hasCore) continue;

// ✅ 2) 제외 키워드(잡음)
            boolean hasNoise = title.matches(".*(모집|교육|설명회|지원사업|전자투표|설문|캠페인|행사|홍보|남산).*");
            if (hasNoise) continue;

            SourceDocument sd = new SourceDocument();
            sd.setSource(SOURCE);
            sd.setDetailUrl(detailUrl);
            sd.setTitle(title);

            // 목록에서 “등록기관/등록일”은 DOM 구조마다 달라서
            // 1차는 상세 페이지에서 채우는 걸 추천(다음 단계)
            docRepo.save(sd);
            fillDetail(sd);   // ✅ 추가
            docRepo.save(sd); // ✅ 업데이트 저장 (두 번째 save)
            saved++;
        }

        return saved;
    }

    private void fillDetail(SourceDocument sd) {
        try {
            System.out.println("▶ fillDetail url=" + sd.getDetailUrl());

            String url = sd.getDetailUrl();

            Document d = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .referrer("https://cleanup.seoul.go.kr")
                    .timeout(15_000)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .get();

            System.out.println("▶ status ok, htmlLen=" + d.html().length());

            // ✅ 1) 본문 텍스트: 페이지마다 구조가 달라서 "가장 큰 컨텐츠 영역"을 우선 잡는 방식
            //    아래 셀렉터는 1차 후보들(몽땅은 보통 본문이 특정 컨테이너에 있음)
            Element content =
                    firstNonNull(
                            d.selectFirst("#contents"),
                            d.selectFirst(".contents"),
                            d.selectFirst(".bbsView"),      // 자주 쓰는 뷰 컨테이너 후보
                            d.selectFirst(".board_view"),
                            d.selectFirst("article")
                    );

            if (content != null) {
                String text = content.text().trim();
                if (!text.isEmpty()) {
                    sd.setContentText(text);
                }
            }

            // ✅ 2) 등록일/기관은 HTML에서 라벨 기반으로 찾는게 안전함
            //    일단 전체 텍스트에서 날짜 패턴(YYYY.MM.DD) 같은 걸 뽑는 1차 방식
            //    (정확 셀렉터는 다음 단계에서 페이지 소스 보고 고정)
            String pageText = d.text();

            // 날짜 패턴 예: 2025.11.10 / 2025-11-10
            LocalDate date = extractDate(pageText);
            if (date != null) sd.setPublishedDate(date);

            // 기관(부서)도 우선 키워드 근처로 추출(1차)
            String org = extractOrg(pageText);
            if (org != null && !org.isBlank()) sd.setOrg(org);

        } catch (Exception e) {
            // 상세 파싱 실패해도 목록 수집은 성공으로 두기
            System.out.println("❗ detail parse failed: " + sd.getDetailUrl() + " / " + e.getMessage());
        }
    }

    private Element firstNonNull(Element... elems) {
        for (Element e : elems) if (e != null) return e;
        return null;
    }

    private LocalDate extractDate(String text) {
        // 2025.12.28
        Matcher m1 = Pattern.compile("(20\\d{2})\\.(\\d{1,2})\\.(\\d{1,2})").matcher(text);
        if (m1.find()) {
            int y = Integer.parseInt(m1.group(1));
            int mo = Integer.parseInt(m1.group(2));
            int d = Integer.parseInt(m1.group(3));
            return LocalDate.of(y, mo, d);
        }
        // 2025-12-28
        Matcher m2 = Pattern.compile("(20\\d{2})-(\\d{1,2})-(\\d{1,2})").matcher(text);
        if (m2.find()) {
            int y = Integer.parseInt(m2.group(1));
            int mo = Integer.parseInt(m2.group(2));
            int d = Integer.parseInt(m2.group(3));
            return LocalDate.of(y, mo, d);
        }
        return null;
    }

    private String extractOrg(String text) {
        // 1차 임시: "담당부서", "부서", "기관" 근처 라인을 대충 잡는 방식
        // 다음 단계에서 HTML 구조 보고 정확 셀렉터로 바꿀 거임
        String[] keys = {"담당부서", "부서", "기관", "담당"};
        for (String k : keys) {
            int idx = text.indexOf(k);
            if (idx >= 0) {
                int end = Math.min(text.length(), idx + 50);
                return text.substring(idx, end).replaceAll("\\s+", " ").trim();
            }
        }
        return null;
    }

    public int fillLatestEmptyDetails(int limit) {

        SslIgnoreUtil.ignoreSsl();

        List<SourceDocument> targets =
                docRepo.findTop50BySourceAndContentTextIsNullOrderByIdDesc(SOURCE);

        int filled = 0;

        for (SourceDocument sd : targets) {
            if (filled >= limit) break;

            // 상세 채우기
            fillDetail(sd);

            // contentText가 실제로 채워졌을 때만 저장 카운트
            if (sd.getContentText() != null && !sd.getContentText().isBlank()) {
                docRepo.save(sd);
                filled++;
            }
        }

        return filled;
    }

    /**
     * 몽땅 게시판 여러 페이지를 순회하며 수집
     */
    public int crawlAllPages(int startPage, int endPage) throws IOException {
        int totalSaved = 0;

        for (int page = startPage; page <= endPage; page++) {
            System.out.println("=== crawl page " + page + " ===");
            int saved = crawlListPage(page);
            totalSaved += saved;

            // 너무 빠르면 차단 위험 → 잠깐 쉬기
            try {
                Thread.sleep(1000); // 1초
            } catch (InterruptedException ignored) {}
        }

        System.out.println("=== total saved: " + totalSaved + " ===");
        return totalSaved;
    }
}
