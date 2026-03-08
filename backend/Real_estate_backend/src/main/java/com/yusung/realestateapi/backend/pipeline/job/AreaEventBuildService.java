package com.yusung.realestateapi.backend.pipeline.job;

import com.yusung.realestateapi.backend.area.domain.AreaDocument;
import com.yusung.realestateapi.backend.area.domain.AreaEvent;
import com.yusung.realestateapi.backend.document.domain.SourceDocument;
import com.yusung.realestateapi.backend.area.infra.AreaDocumentRepository;
import com.yusung.realestateapi.backend.area.infra.AreaEventRepository;
import com.yusung.realestateapi.backend.document.infra.SourceDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AreaEventBuildService {

    private final AreaDocumentRepository areaDocumentRepository;
    private final SourceDocumentRepository sourceDocumentRepository;
    private final AreaEventRepository areaEventRepository;

    // 너 UI 5단계에 맞춘 stageKey
    private static final List<StageRule> RULES = List.of(
            new StageRule("DESIGNATION", "정비구역 지정", List.of("정비구역지정", "정비구역 지정", "정비계획결정", "정비계획 결정", "정비계획수립", "정비계획 수립", "지정고시", "지정 고시")),
            new StageRule("ASSOCIATION", "조합 설립", List.of("조합설립인가", "조합 설립인가", "조합설립 인가", "조합창립총회", "창립총회", "추진위원회승인", "추진위원회 승인", "추진위 승인")),
            new StageRule("EXECUTION", "사업시행 인가", List.of("사업시행인가", "사업시행 인가", "사업시행계획인가", "사업시행계획 인가", "사업시행계획")),
            new StageRule("MANAGEMENT", "관리처분 인가", List.of("관리처분인가", "관리처분 인가", "관리처분계획인가", "관리처분계획 인가", "관리처분계획")),
            new StageRule("CONSTRUCTION", "착공/준공", List.of("착공", "준공", "입주", "사용승인", "사용 승인"))
    );

    /**
     * 최근 매칭된 area_documents를 기반으로 area_events 생성
     * @param limit area_documents에서 최근 몇 건을 처리할지
     */
    @Transactional
    public int buildEventsFromMatchedDocs(int limit) {
        // 최근 매칭 데이터부터 처리 (id DESC)
        List<AreaDocument> ads = areaDocumentRepository.findTopNOrderByIdDesc(limit);

        int inserted = 0;

        for (AreaDocument ad : ads) {
            Long areaId = ad.getAreaId();
            Long docId = ad.getDocumentId();

            Optional<SourceDocument> opt = sourceDocumentRepository.findById(docId);
            if (opt.isEmpty()) continue;

            SourceDocument doc = opt.get();

            // ✅ OpenGov(결재문서)만 이벤트 생성 대상으로
            if (!"opengov_sanction".equals(doc.getSource())) continue;

            String text = ((doc.getTitle() == null ? "" : doc.getTitle()) + " " +
                    (doc.getContentText() == null ? "" : doc.getContentText()));

            String norm = normalize(text);

            // ✅ 공식 문서가 아니면 단계 이벤트 생성 자체를 하지 않는다
            if (!isOfficialDocument(text)) continue;

            // 문서의 발행일(없으면 오늘로)
            LocalDate eventDate = doc.getPublishedDate() != null ? doc.getPublishedDate() : LocalDate.now();

            // ✅ 제목 기반 “공식 단계” 판정
            Optional<Stage> stageOpt = detectOfficialStage(doc.getTitle());
            if (stageOpt.isEmpty()) continue;

            Stage stage = stageOpt.get();

            // ✅ 3번: 중복 방지 (여기!)
            if (areaEventRepository.existsByAreaIdAndDocumentIdAndStageKey(
                    areaId, docId, stage.key
            )) {
                continue;
            }

            // ✅ 4번: 이벤트 저장 (여기!)
            AreaEvent ev = new AreaEvent();
            ev.setAreaId(areaId);
            ev.setDocumentId(docId);

            ev.setEventType("STAGE");
            ev.setEventLabel(stage.label);

            ev.setStageKey(stage.key);
            ev.setStageLabel(stage.label);

            // 날짜는 publishedDate 있으면 쓰고, 없으면 null (중요)
            ev.setEventDate(doc.getPublishedDate());

            ev.setDetectedText(doc.getTitle());
            ev.setConfidence(ad.getConfidence() != null ? ad.getConfidence() : new BigDecimal("0.95"));

            ev.setCreatedAt(LocalDateTime.now());
            ev.setIsOfficial(true);

            areaEventRepository.save(ev);
            inserted++;

        }

        return inserted;
    }

    private boolean containsAny(String normText, List<String> keywordsNorm) {
        for (String k : keywordsNorm) {
            if (normText.contains(k)) return true;
        }
        return false;
    }

    private String normalize(String s) {
        if (s == null) return "";
        String lower = s.toLowerCase(Locale.KOREA);
        return lower.replaceAll("[^0-9a-z가-힣]", "");
    }

    private static class StageRule {
        final String stageKey;
        final String label;
        final List<String> keywordsNorm;

        StageRule(String stageKey, String label, List<String> keywords) {
            this.stageKey = stageKey;
            this.label = label;
            this.keywordsNorm = keywords.stream()
                    .map(k -> k.toLowerCase(Locale.KOREA).replaceAll("[^0-9a-z가-힣]", ""))
                    .toList();
        }
    }

    // ✅ STEP 1-2: "공식 고시/인가/지정" 문서만 통과시키는 필터
    private boolean isOfficialDocument(String rawText) {
        if (rawText == null) return false;

        // 원문을 normalize 한 형태로 검사 (너 코드 방식 그대로)
        String norm = normalize(rawText);

        // 1) 탈락 키워드 (하나라도 있으면 무조건 탈락)
        String[] reject = {"신청", "예정", "계획", "검토", "공람", "설명회", "안내", "홍보", "모집"};
        for (String r : reject) {
            if (norm.contains(r)) return false;
        }

        // 2) "문서 성격" 키워드: 고시/인가/지정/승인/결정 중 최소 1개
        boolean hasDocType =
                norm.contains("고시") || norm.contains("인가") || norm.contains("지정") ||
                        norm.contains("승인") || norm.contains("결정");

        if (!hasDocType) return false;

        // 3) "완료/확정" 표현: ~한다/되었다 류가 있어야 통과
        // (페이지마다 문장 다를 수 있어서 최소 패턴만)
        boolean hasConfirm =
                norm.contains("고시") ||
                        norm.contains("고시문") ||
                        norm.contains("고시함") ||
                        norm.contains("인가") ||
                        norm.contains("승인") ||
                        norm.contains("결정") ||
                        norm.contains("되었") ||
                        norm.contains("확정") ||
                        norm.matches(".*제20\\d{2}.*"); // 제2024-123 같은 고시번호


        return hasConfirm;
    }

    private static class Stage {
        final String key;
        final String label;
        Stage(String key, String label) { this.key = key; this.label = label; }
    }

    private Optional<Stage> detectOfficialStage(String title) {
        if (title == null) return Optional.empty();
        String t = title.replaceAll("\\s+", "");

        // ✅ 너가 원하는 “공식 단계”만 엄격하게
        if (t.contains("정비구역지정") || t.contains("정비구역지정고시") || t.contains("지구단위계획구역결정") || t.contains("정비계획결정") && t.contains("고시")) {
            return Optional.of(new Stage("DESIGNATION", "정비구역 지정"));
        }
        if (t.contains("조합설립인가") || (t.contains("조합설립") && t.contains("인가"))) {
            return Optional.of(new Stage("ASSOCIATION", "조합 설립"));
        }
        if (t.contains("사업시행인가") || (t.contains("사업시행") && t.contains("인가"))) {
            return Optional.of(new Stage("IMPLEMENTATION", "사업시행 인가"));
        }
        if (t.contains("관리처분인가") || (t.contains("관리처분") && t.contains("인가"))) {
            return Optional.of(new Stage("DISPOSITION", "관리처분 인가"));
        }
        if (t.contains("착공") || t.contains("준공") || t.contains("사용승인")) {
            return Optional.of(new Stage("CONSTRUCTION", "착공/준공"));
        }

        return Optional.empty();
    }

}
