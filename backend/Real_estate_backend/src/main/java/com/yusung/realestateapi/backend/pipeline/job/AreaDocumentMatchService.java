package com.yusung.realestateapi.backend.pipeline.job;

import com.yusung.realestateapi.backend.area.domain.AreaV3;
import com.yusung.realestateapi.backend.area.domain.AreaDocument;
import com.yusung.realestateapi.backend.area.infra.AreaDocumentRepository;
import com.yusung.realestateapi.backend.area.infra.AreaV3Repository;
import com.yusung.realestateapi.backend.document.domain.SourceDocument;
import com.yusung.realestateapi.backend.document.infra.SourceDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AreaDocumentMatchService {

    private final AreaV3Repository areaRepository;
    private final SourceDocumentRepository sourceDocumentRepository;
    private final AreaDocumentRepository areaDocumentRepository;

    private static final int MIN_AREA_NAME_LEN = 3;
    private static final BigDecimal SCORE_THRESHOLD = new BigDecimal("0.75");
    private static final BigDecimal MIN_GAP = new BigDecimal("0.10");

    @Transactional
    public int matchRecentDocuments(int docLimit, boolean onlyActive) {
        // ✅ List<AreaV3>로 통합
        List<AreaV3> areas = onlyActive ? areaRepository.findActiveAreas() : areaRepository.findAll();

        List<AreaKey> areaKeys = areas.stream()
                .map(a -> new AreaKey(
                        a.getAreaId(),
                        a.getName(), // 이제 COALESCE된 정식 명칭 기반으로 매칭함
                        buildVariants(a.getName())
                ))
                .filter(k -> k.variants != null && !k.variants.isEmpty())
                .collect(Collectors.toList());

        List<SourceDocument> docs = sourceDocumentRepository.findTopNOrderByPublishedDateDesc(docLimit);
        int inserted = 0;

        for (SourceDocument doc : docs) {
            String titleNorm = normalize(doc.getTitle());
            String bodyNorm  = normalize(doc.getContentText());

            MatchPick best = null;
            MatchPick second = null;

            for (AreaKey ak : areaKeys) {
                MatchResult mr = findBestMatch(titleNorm, bodyNorm, ak);
                if (mr == null) continue;

                MatchPick pick = new MatchPick(ak, mr.variantUsed, mr.score);

                if (best == null || pick.score.compareTo(best.score) > 0) {
                    second = best;
                    best = pick;
                } else if (second == null || pick.score.compareTo(second.score) > 0) {
                    second = pick;
                }
            }

            if (best == null || best.score.compareTo(SCORE_THRESHOLD) < 0) continue;

            // 중복 및 오탐 방지 로직 (기존 유지)
            if (second != null && best.score.subtract(second.score).compareTo(MIN_GAP) < 0) continue;
            if (areaDocumentRepository.existsByAreaIdAndDocumentId(best.ak.areaId, doc.getId())) continue;

            AreaDocument ad = new AreaDocument();
            ad.setAreaId(best.ak.areaId);
            ad.setDocumentId(doc.getId());
            ad.setConfidence(best.score);
            ad.setCreatedAt(LocalDateTime.now());

            areaDocumentRepository.save(ad);
            inserted++;
        }
        return inserted;
    }

    private MatchResult findBestMatch(String titleNorm, String bodyNorm, AreaKey ak) {
        BigDecimal bestScore = BigDecimal.ZERO;
        String bestVariant = null;

        for (String v : ak.variants) {
            BigDecimal score = BigDecimal.ZERO;
            if (titleNorm != null && titleNorm.contains(v)) score = score.add(new BigDecimal("0.70"));
            if (bodyNorm != null && bodyNorm.contains(v)) score = score.add(new BigDecimal("0.45"));

            if (score.compareTo(bestScore) > 0) {
                bestScore = score;
                bestVariant = v;
            }
        }
        return bestVariant == null ? null : new MatchResult(bestVariant, bestScore.setScale(2, RoundingMode.HALF_UP));
    }

    private List<String> buildVariants(String name) {
        String base = normalize(name);
        if (base == null || base.length() < MIN_AREA_NAME_LEN) return Collections.emptyList();
        Set<String> variants = new LinkedHashSet<>();
        variants.add(base);
        return new ArrayList<>(variants);
    }

    private String normalize(String s) {
        return s == null ? null : s.toLowerCase().replaceAll("[^0-9a-z가-힣]", "");
    }

    private static class AreaKey {
        final Long areaId;
        final String rawName;
        final List<String> variants;
        AreaKey(Long areaId, String rawName, List<String> variants) {
            this.areaId = areaId; this.rawName = rawName; this.variants = variants;
        }
    }
    private static class MatchResult {
        final String variantUsed; final BigDecimal score;
        MatchResult(String variantUsed, BigDecimal score) { this.variantUsed = variantUsed; this.score = score; }
    }
    private static class MatchPick {
        final AreaKey ak; final String variantUsed; final BigDecimal score;
        MatchPick(AreaKey ak, String variantUsed, BigDecimal score) { this.ak = ak; this.variantUsed = variantUsed; this.score = score; }
    }
}