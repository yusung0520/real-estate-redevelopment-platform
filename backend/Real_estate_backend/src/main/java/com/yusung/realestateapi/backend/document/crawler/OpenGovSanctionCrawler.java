//package com.yusung.realestateapi.backend.document.crawler;
//
//import com.yusung.realestateapi.backend.document.domain.SourceDocument;
//import com.yusung.realestateapi.backend.document.infra.SourceDocumentRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class OpenGovSanctionCrawler {
//
//    private final SourceDocumentRepository sourceDocumentRepository;
//
//    private static final String SOURCE = "opengov_sanction";
//    private static final String BASE = "https://opengov.seoul.go.kr";
//
//    // sanction/{id} 추출용
//    private static final Pattern SANCTION_ID = Pattern.compile("/sanction/(\\d+)");
//
//    /**
//     * ✅ A 방식: 검색 페이지(키워드) → sanction 상세 페이지들 수집 → SourceDocument 저장
//     */
//    public int crawlByKeywords(List<String> keywords, int maxPagesPerKeyword) {
//        int saved = 0;
//
//        for (String keyword : keywords) {
//            for (int page = 1; page <= maxPagesPerKeyword; page++) {
//                String searchUrl = BASE + "/search?searchKeyword=" + encode(keyword) + "&page=" + page;
//
//                log.info("[OpenGov] search keyword='{}' page={} url={}", keyword, page, searchUrl);
//
//                Set<String> sanctionUrls = fetchSanctionUrls(searchUrl);
//                if (sanctionUrls.isEmpty()) {
//                    log.info("[OpenGov] no sanction links found. stop keyword='{}' at page={}", keyword, page);
//                    break;
//                }
//
//                for (String detailUrl : sanctionUrls) {
//                    saved += fetchAndSaveDetail(detailUrl);
//                }
//            }
//        }
//
//        log.info("[OpenGov] total saved={}", saved);
//        return saved;
//    }
//
//    /**
//     * 검색 결과 페이지에서 /sanction/{id} 링크 수집
//     */
//    private Set<String> fetchSanctionUrls(String searchUrl) {
//        try {
//            Document doc = Jsoup.connect(searchUrl)
//                    .userAgent("Mozilla/5.0")
//                    .timeout(10_000)
//                    .get();
//
//            // 페이지 내 모든 a 링크에서 /sanction/{id} 패턴만 뽑는다
//            Elements links = doc.select("a[href]");
//            Set<String> results = new LinkedHashSet<>();
//
//            for (Element a : links) {
//                String href = a.attr("href");
//                Matcher m = SANCTION_ID.matcher(href);
//                if (m.find()) {
//                    String abs = href.startsWith("http") ? href : (BASE + href);
//                    results.add(abs);
//                }
//            }
//
//            return results;
//
//        } catch (Exception e) {
//            log.warn("[OpenGov] search fetch failed url={} err={}", searchUrl, e.toString());
//            return Collections.emptySet();
//        }
//    }
//
//    /**
//     * sanction 상세 페이지에서 제목/기관/날짜/URL만 뽑아 저장
//     * - 이미 저장된 detail_url이면 0 리턴
//     */
//    private int fetchAndSaveDetail(String detailUrl) {
//        try {
//            // 중복 방지: detail_url + source unique를 이미 쓰고 있으면 여기서 미리 컷 가능
//            if (sourceDocumentRepository.existsBySourceAndDetailUrl(SOURCE, detailUrl)) {
//                return 0;
//            }
//
//            Document doc = Jsoup.connect(detailUrl)
//                    .userAgent("Mozilla/5.0")
//                    .timeout(10_000)
//                    .get();
//
//            String title = extractTitle(doc);
//            String org = extractOrg(doc);
//            LocalDate publishedAt = extractProducedDate(doc);
//
//            if (title == null || title.isBlank()) {
//                log.info("[OpenGov] skip(no title) {}", detailUrl);
//                return 0;
//            }
//
//            SourceDocument sd = new SourceDocument();
//            sd.setSource(SOURCE);
//            sd.setOrg(org);
//            sd.setTitle(title);
//            sd.setPublishedDate(publishedAt);
//            sd.setDetailUrl(detailUrl);
//            sd.setContentText(null);
//
//            sourceDocumentRepository.save(sd);
//
//            log.info("[OpenGov] saved title='{}' date={} org={} url={}", title, publishedAt, org, detailUrl);
//            return 1;
//
//        } catch (Exception e) {
//            log.warn("[OpenGov] detail fetch failed url={} err={}", detailUrl, e.toString());
//            return 0;
//        }
//    }
//
//    private String extractTitle(Document doc) {
//
//        // 1) sanction 문서 페이지에서 제목이 들어갈만한 영역 후보들 (우선순위)
//        String[] selectors = {
//                "div.view_tit h3",
//                "div.view_tit h2",
//                "div.view_tit h1",
//                "div.cont_tit h2",
//                "div.cont_tit h3",
//                "div.tit_area h2",
//                "div.tit_area h3",
//                "h2.tit",
//                "h3.tit"
//        };
//
//        for (String sel : selectors) {
//            Element el = doc.selectFirst(sel);
//            if (el != null) {
//                String t = el.text().trim();
//                if (isValidTitle(t)) return t;
//            }
//        }
//
//        // 2) fallback: meta og:title (종종 진짜 제목이 여기에 있음)
//        Element og = doc.selectFirst("meta[property=og:title]");
//        if (og != null) {
//            String t = og.attr("content").trim();
//            if (isValidTitle(t)) return t;
//        }
//
//        // 3) 마지막 fallback: <title> 태그
//        String t = doc.title() == null ? null : doc.title().trim();
//        if (isValidTitle(t)) return t;
//
//        return null;
//    }
//
//    private boolean isValidTitle(String t) {
//        if (t == null) return false;
//        if (t.length() < 8) return false; // 너무 짧은건 제목 아닐 가능성 높음
//        // 슬로건/공통문구 제거
//        if (t.contains("동행매력") || t.contains("서울정보소통광장")) return false;
//        return true;
//    }
//
//
//    private String extractOrg(Document doc) {
//        return extractTableValueByLabel(doc, "기관명");
//    }
//
//    private LocalDate extractProducedDate(Document doc) {
//        String raw = extractTableValueByLabel(doc, "생산일자");
//        if (raw == null) return null;
//        raw = raw.trim().replace(".", "-"); // 혹시 2018.12.03 같은 형식 대비
//        try {
//            return LocalDate.parse(raw);
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    private String extractTableValueByLabel(Document doc, String label) {
//        // th에 label이 있고, 바로 옆 td가 값인 구조를 탐색
//        Element th = doc.selectFirst("th:matchesOwn(^" + java.util.regex.Pattern.quote(label) + "$)");
//        if (th != null) {
//            Element td = th.parent().selectFirst("td");
//            if (td != null) return td.text().trim();
//        }
//
//        // 혹시 td에 label이 들어가고 다음 td가 값인 경우도 대비
//        Element tdLabel = doc.selectFirst("td:matchesOwn(^" + java.util.regex.Pattern.quote(label) + "$)");
//        if (tdLabel != null) {
//            Element next = tdLabel.nextElementSibling();
//            if (next != null) return next.text().trim();
//        }
//
//        return null;
//    }
//
//
//    private String encode(String s) {
//        try {
//            return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
//        } catch (Exception e) {
//            return s;
//        }
//    }
//}
