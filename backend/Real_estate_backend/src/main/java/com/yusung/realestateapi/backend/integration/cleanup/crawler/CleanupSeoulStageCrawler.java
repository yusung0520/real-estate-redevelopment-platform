package com.yusung.realestateapi.backend.integration.cleanup.crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CleanupSeoulStageCrawler {

    /**
     * ✅ 호환성 유지용 메서드 (인자 2개 버전)
     * CleanupBizListCrawler 등에서 호출할 때 에러가 나지 않도록 추가합니다.
     */
    public List<StageItem> crawlStages(String cafeUrlValue, Map<String, String> cookies) {
        // 쿠키가 들어와도 내부적으로는 최신 로직(인자 1개)을 수행하도록 연결합니다.
        return crawlStages(cafeUrlValue);
    }

    /**
     * ✅ 실제 크롤링 수행 메서드 (인자 1개 버전)
     */
    public List<StageItem> crawlStages(String cafeUrlValue) {
        if (cafeUrlValue == null || cafeUrlValue.isBlank()) return new ArrayList<>();

        String mainUrl = "https://cleanup.seoul.go.kr/cafe/mainIndx.do?cafeUrl=" + cafeUrlValue;

        try {
            Connection.Response mainRes = Jsoup.connect(mainUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .execute();

            String html = mainRes.body();
            String realCafeId = "";
            if (html.contains("cafeId=")) {
                realCafeId = html.split("cafeId=")[1].split("[&\"']")[0];
            }

            // 1순위: 신규 폼 (assc)
            String targetUrl = "https://cleanup.seoul.go.kr/assc/scrin-bbs/execute.do";
            Document doc = Jsoup.connect(targetUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Referer", mainUrl)
                    .cookies(mainRes.cookies())
                    .data("cafeId", realCafeId.isEmpty() ? cafeUrlValue : realCafeId)
                    .data("menuId", "100")
                    .get();

            List<StageItem> stages = parseStages(doc, targetUrl);

            // 2순위: 기존 폼 (cafe)
            if (stages.isEmpty()) {
                String backupUrl = "https://cleanup.seoul.go.kr/cafe/mainIndx/execute.do";
                doc = Jsoup.connect(backupUrl)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .header("Referer", mainUrl)
                        .cookies(mainRes.cookies())
                        .data("cafcId", realCafeId.isEmpty() ? cafeUrlValue : realCafeId)
                        .get();
                stages = parseStages(doc, backupUrl);
            }

            return stages;

        } catch (Exception e) {
            log.error("[CRAWL] 수집 실패: {} (사유: {})", cafeUrlValue, e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<StageItem> parseStages(Document doc, String sourceUrl) {
        List<StageItem> stages = new ArrayList<>();
        Elements items = doc.select(".progress_course-list li, .main-progress-box li, .bsns_step_list li, .progress-cont li");

        for (Element li : items) {
            String stepName = li.select(".s-tit, .txt, .step_tit, .step").text().trim();
            String dateText = li.select(".completion-date, .date").text().trim();
            boolean isCurrent = li.hasClass("active") || li.hasClass("on");

            if (!stepName.isEmpty() && !dateText.isEmpty()) {
                LocalDate parsedDate = parseLocalDate(dateText);
                if (parsedDate != null) {
                    int standardStepNo = getStandardStepNo(stepName);
                    stages.add(new StageItem(standardStepNo, stepName, parsedDate, isCurrent, sourceUrl));
                }
            }
        }
        return stages;
    }

    private int getStandardStepNo(String stepName) {
        if (stepName.contains("기본계획")) return 1;
        if (stepName.contains("안전진단")) return 2;
        if (stepName.contains("구역지정")) return 3;
        if (stepName.contains("추진위원회")) return 4;
        if (stepName.contains("조합설립")) return 5;
        if (stepName.contains("사업시행")) return 6;
        if (stepName.contains("관리처분")) return 7;
        if (stepName.contains("철거신고")) return 8;
        if (stepName.contains("착공신고")) return 9;
        if (stepName.contains("일반분양")) return 10;
        if (stepName.contains("준공인가")) return 11;
        if (stepName.contains("이전고시")) return 12;
        if (stepName.contains("조합해산")) return 13;
        if (stepName.contains("조합청산")) return 14;
        return 99;
    }

    private LocalDate parseLocalDate(String raw) {
        try {
            String s = raw.replace(".", "-").trim();
            if (s.matches("\\d{2}-\\d{2}-\\d{2}")) s = "20" + s;
            return LocalDate.parse(s);
        } catch (Exception e) { return null; }
    }

    public record StageItem(int stepNo, String stepName, LocalDate eventDate, boolean isCurrent, String sourceUrl) {}
}