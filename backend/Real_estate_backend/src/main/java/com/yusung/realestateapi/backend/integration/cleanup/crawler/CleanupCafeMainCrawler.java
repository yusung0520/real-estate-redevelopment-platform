package com.yusung.realestateapi.backend.integration.cleanup.crawler;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class CleanupCafeMainCrawler {

    private static final String CAFE_MAIN_URL_TEMPLATE =
            "https://cleanup.seoul.go.kr/cafe/mainIndx.do?cafeUrl=%s";

    private static final Pattern P_EXEC_ID_1 = Pattern.compile("executeUrlCafeId\\s*[:=]\\s*['\"]?([0-9A-Za-z]+)['\"]?");
    private static final Pattern P_EXEC_LD_1 = Pattern.compile("executeUrlCafeLd\\s*[:=]\\s*['\"]?([0-9A-Za-z]+)['\"]?");
    private static final Pattern P_EXEC_ID_2 = Pattern.compile("cafeId\\s*[:=]\\s*['\"]?([0-9A-Za-z]+)['\"]?");
    private static final Pattern P_EXEC_LD_2 = Pattern.compile("cafeLd\\s*[:=]\\s*['\"]?([0-9A-Za-z]+)['\"]?");

    public CleanupCafeMainResult crawlMain(String cafeMainUrlOrSlug) throws Exception {

        if (cafeMainUrlOrSlug == null || cafeMainUrlOrSlug.isBlank()) {
            throw new IllegalArgumentException("cafeMainUrlOrSlug is empty");
        }

        String input = cafeMainUrlOrSlug.trim();
        String normalized = normalizeMainUrl(input);

        Connection conn = Jsoup.connect(normalized)
                .method(Connection.Method.GET)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36")
                .referrer("https://cleanup.seoul.go.kr/")
                .timeout(20000);

        Connection.Response resp = conn.execute();
        Document doc = resp.parse();
        String html = doc.outerHtml();

        // ✅ 1. 조합/사업 명칭 추출 (상세 페이지 상단 타이틀)
        // 서울시 정보몽땅 페이지의 h4.title 태그에서 조합 이름을 가져옵니다.
        String businessName = doc.select("h4.title").text().trim();
        if (businessName.isEmpty()) {
            businessName = doc.select(".cafe_name").text().trim();
        }

        // ✅ 2. 지번 주소 추출 (기본 areaName)
        String areaName = doc.select(".location, .addr").text().trim();
        if (areaName.isEmpty()) {
            areaName = doc.title(); // 실패 시 페이지 타이틀이라도 반환
        }

        // ✅ 3. 인증/진행에 필요한 파라미터(ID, LD) 추출
        String executeUrlCafeId = attrValueFirst(doc, "input[name=executeUrlCafeId]", "input#executeUrlCafeId", "input[name=cafeId]");
        String executeUrlCafeLd = attrValueFirst(doc, "input[name=executeUrlCafeLd]", "input#executeUrlCafeLd", "input[name=cafeLd]");

        if (isBlank(executeUrlCafeId)) {
            executeUrlCafeId = extractFirst(html, P_EXEC_ID_1, P_EXEC_ID_2);
        }
        if (isBlank(executeUrlCafeLd)) {
            executeUrlCafeLd = extractFirst(html, P_EXEC_LD_1, P_EXEC_LD_2);
        }

        Map<String, String> cookies = new HashMap<>(resp.cookies());

        log.info("[CLEANUP-MAIN] businessName='{}', areaName='{}'", businessName, areaName);

        // ✅ CleanupCafeMainResult 레코드 생성자 순서에 맞춰 반환
        return new CleanupCafeMainResult(
                input,
                normalized,
                areaName,           // 지번 주소
                businessName,       // ✅ 추가: 조합 명칭
                normalized,         // mainUrl
                nullToEmpty(executeUrlCafeId),
                nullToEmpty(executeUrlCafeLd),
                cookies,
                html.length(),
                html.substring(0, Math.min(200, html.length())), // 요약본
                "",                 // currentStage (초기값)
                new ArrayList<>()   // stages (초기값)
        );
    }

    private static String attrValueFirst(Document doc, String... selectors) {
        for (String sel : selectors) {
            Element el = doc.selectFirst(sel);
            if (el != null) {
                String v = el.attr("value");
                if (!isBlank(v)) return v.trim();
            }
        }
        return null;
    }

    private static String extractFirst(String text, Pattern... patterns) {
        if (text == null) return null;
        for (Pattern p : patterns) {
            Matcher m = p.matcher(text);
            if (m.find()) return m.group(1);
        }
        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private String normalizeMainUrl(String input) {
        String v = input == null ? "" : input.trim();
        if (v.isEmpty()) return v;
        v = v.replace("/cleanup/cafe/", "/cafe/");
        if (!v.startsWith("http")) {
            v = String.format(CAFE_MAIN_URL_TEMPLATE, v);
        }
        return v;
    }
}