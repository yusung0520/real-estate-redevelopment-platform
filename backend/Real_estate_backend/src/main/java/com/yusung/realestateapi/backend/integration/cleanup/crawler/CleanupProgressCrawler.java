package com.yusung.realestateapi.backend.integration.cleanup.crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CleanupProgressCrawler {

    private static final String UA = "Mozilla/5.0";
    private static final String HOST = "https://cleanup.seoul.go.kr";
    private static final Logger log = LoggerFactory.getLogger(CleanupProgressCrawler.class);
    // ✅ 날짜 패턴 (몽땅 화면에서 자주 나오는 형태들)
    private static final java.util.regex.Pattern P_YY_DOT =
            java.util.regex.Pattern.compile("(\\d{2})\\.(\\d{1,2})\\.(\\d{1,2})");      // 03.11.14
    private static final java.util.regex.Pattern P_YYYY_DOT =
            java.util.regex.Pattern.compile("(\\d{4})\\.(\\d{1,2})\\.(\\d{1,2})");     // 2024.11.16
    private static final java.util.regex.Pattern P_YYYY_DASH =
            java.util.regex.Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2})");         // 2024-11-16

    public ProgressResult crawlProgress(
            String executeUrlCafeId,
            String executeUrlCafeLd,
            String mainUrl,
            Map<String, String> cookies
    ) throws Exception {

        List<String> candidates = new ArrayList<>();
        candidates.addAll(buildCandidates(executeUrlCafeId, mainUrl));
        candidates.addAll(buildCandidates(executeUrlCafeLd, mainUrl));

        Document doc = null;
        String usedUrl = "";

        for (String u : candidates) {
            FetchMeta meta = tryFetch(u, mainUrl, cookies);
            doc = meta.doc();
            usedUrl = u;

            log.info("[progress-try] url={} status={} location={} finalUrl={} title={} htmlLength={} head={}",
                    u, meta.status(), meta.location(), meta.finalUrl(), meta.title(), meta.htmlLength(), meta.bodyHead());

            if (isErrorPage(meta)) {
                log.warn("[progress-try] detected ERROR page -> skip url={}", u);
                continue;
            }

            if (hasProgressBox(doc)) {
                log.info("[progress-try] SUCCESS progress box found! url={}", u);
                break;
            }

            // wrapper(mainIndx)일 수 있어서 iframe hop
            if (doc != null) {
                var iframe = doc.selectFirst("iframe#contentFrame");
                if (iframe != null) {
                    String src = iframe.attr("src");
                    if (src != null && !src.isBlank()) {
                        String abs = src.startsWith("http") ? src : HOST + src;

                        FetchMeta meta2 = tryFetch(abs, u, cookies);
                        log.info("[progress-try] iframe hop: wrapper={} -> iframeSrc={}", u, abs);

                        if (!isErrorPage(meta2) && hasProgressBox(meta2.doc())) {
                            doc = meta2.doc();
                            usedUrl = abs;
                            log.info("[progress-try] SUCCESS progress box found via iframe hop! url={}", abs);
                            break;
                        }
                    }
                }
            }

            String html = doc != null ? doc.html() : "";
            boolean hasKeyword = html.contains("main-progress") || html.contains("main-progress-box") || html.contains("진행");
            log.info("[progress-try] no progress box. hasKeyword={} url={}", hasKeyword, u);
        }

        if (doc == null || !hasProgressBox(doc)) {
            return new ProgressResult(
                    "",
                    List.of(),
                    doc == null ? "" : doc.location(),
                    doc == null ? "" : doc.title(),
                    doc == null ? 0 : doc.html().length(),
                    false,
                    usedUrl
            );
        }

        String currentStage = textOrEmpty(doc.selectFirst(".main-progress-box .progress p"));

        List<StageItem> stages = new ArrayList<>();
        var lis = doc.select(".main-progress-box .progress-cont > ul > li");

        for (var li : lis) {
            String noRaw = textOrEmpty(li.selectFirst(".num"));
            String name  = textOrEmpty(li.selectFirst(".txt"));

            // ✅ 1) 가장 흔한 셀렉터(.date)
            String date = textOrEmpty(li.selectFirst(".date"));

            // ✅ 2) 혹시 태그 구조가 다른 경우 대비 (span.date / div.date 등)
            if (date.isBlank()) date = textOrEmpty(li.selectFirst("span.date"));
            if (date.isBlank()) date = textOrEmpty(li.selectFirst("div.date"));

            // ✅ 3) 그래도 없으면 li 전체 텍스트에서 날짜 패턴만 "추출"
            if (date.isBlank()) date = extractDateText(li.text());

            if (noRaw.isBlank() && name.isBlank()) continue;

            boolean isCurrent = li.hasClass("active");
            stages.add(new StageItem(noRaw, name, date, isCurrent));
        }

        return new ProgressResult(
                currentStage,
                stages,
                doc.location(),
                doc.title(),
                doc.html().length(),
                true,
                usedUrl
        );
    }

    private List<String> buildCandidates(String idOrUrl, String mainUrl) {
        List<String> list = new ArrayList<>();
        if (idOrUrl == null || idOrUrl.isBlank()) return list;

        String v = idOrUrl.trim();

        if (v.startsWith("http://") || v.startsWith("https://")) {
            list.add(v);
            return list;
        }

        // wrapper 후보
        list.add(HOST + "/cafe/mainIndx.do?cafeId=" + v);
        list.add(HOST + "/cafe/mainIndx.do?cafeLd=" + v);
        list.add(HOST + "/cafe/mainIndx.do?executeUrlCafeId=" + v);
        list.add(HOST + "/cafe/mainIndx.do?executeUrlCafeLd=" + v);

        // 실제 진행단계 페이지 후보
        list.add(HOST + "/assc/scrin-bbs/execute.do?cafeId=" + v);
        list.add(HOST + "/assc/scrin-bbs/execute.do?cafeLd=" + v);

        if (mainUrl != null && !mainUrl.isBlank()) {
            String join = mainUrl.contains("?") ? "&" : "?";
            list.add(mainUrl + join + "executeUrlCafeId=" + v);
            list.add(mainUrl + join + "executeUrlCafeLd=" + v);

            String cafeUrl = extractCafeUrl(mainUrl);
            if (!cafeUrl.isBlank()) {
                list.add(HOST + "/cafe/mainIndx.do?cafeUrl=" + cafeUrl + "&executeUrlCafeId=" + v);
                list.add(HOST + "/cafe/mainIndx.do?cafeUrl=" + cafeUrl + "&executeUrlCafeLd=" + v);
            }
        }

        return list;
    }

    private FetchMeta tryFetch(String url, String ref, Map<String, String> cookies) throws Exception {
        Connection.Response res = Jsoup.connect(url)
                .userAgent(UA)
                .referrer(ref)
                .cookies(cookies)
                .timeout(15_000)
                .followRedirects(false)
                .ignoreHttpErrors(true)
                .execute();

        int status = res.statusCode();
        String location = res.header("Location");
        String finalUrl = res.url().toString();

        String body = res.body();
        String bodyHead = body == null ? "" : body.substring(0, Math.min(body.length(), 600)).replace("\n", " ");

        Document doc = null;
        String title = "";
        int htmlLength = 0;
        try {
            doc = res.parse();
            title = doc.title();
            htmlLength = doc.html().length();
        } catch (Exception ignored) {}

        return new FetchMeta(status, location, finalUrl, title, htmlLength, bodyHead, doc);
    }

    private boolean hasProgressBox(Document doc) {
        return doc != null && doc.selectFirst(".main-progress-box") != null;
    }

    private static String textOrEmpty(org.jsoup.nodes.Element el) {
        return el == null ? "" : el.text().trim();
    }

    private String extractCafeUrl(String mainUrl) {
        try {
            int idx = mainUrl.indexOf("cafeUrl=");
            if (idx < 0) return "";
            String tail = mainUrl.substring(idx + "cafeUrl=".length());
            int amp = tail.indexOf("&");
            return amp >= 0 ? tail.substring(0, amp) : tail;
        } catch (Exception e) {
            return "";
        }
    }

    public record ProgressResult(
            String currentStage,
            List<StageItem> stages,
            String finalUrl,
            String title,
            int htmlLength,
            boolean ok,
            String triedUrl
    ) {}

    public record StageItem(
            String stepNoRaw,
            String stepName,
            String dateRaw,
            boolean current
    ) {}

    private record FetchMeta(
            int status,
            String location,
            String finalUrl,
            String title,
            int htmlLength,
            String bodyHead,
            Document doc
    ) {}

    private boolean isErrorPage(FetchMeta meta) {
        if (meta == null) return true;

        if (meta.status() >= 400) return true;

        String head = meta.bodyHead() == null ? "" : meta.bodyHead();

        // cleanup 에러 페이지 특징 / 문구
        if (head.contains("/cmmn/css/error_jsp.css")) return true;
        if (head.contains("요청하신 페이지를 찾을 수 없습니다")) return true;
        if (head.toLowerCase().contains("whitelabel error page")) return true;

        return meta.doc() == null;
    }

    private static String extractDateText(String raw) {
        if (raw == null) return "";
        String s = raw.trim().replaceAll("\\s+", "");

        var m1 = P_YY_DOT.matcher(s);
        if (m1.find()) return m1.group();

        var m2 = P_YYYY_DOT.matcher(s);
        if (m2.find()) return m2.group();

        var m3 = P_YYYY_DASH.matcher(s);
        if (m3.find()) return m3.group();

        return "";
    }
}