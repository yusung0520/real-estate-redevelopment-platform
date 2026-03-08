package com.yusung.realestateapi.backend.integration.cleanup.application;

import com.yusung.realestateapi.backend.area.domain.AreaV3;
import com.yusung.realestateapi.backend.area.infra.AreaV3Repository;
import com.yusung.realestateapi.backend.common.util.NameNormalizer;
import com.yusung.realestateapi.backend.integration.cleanup.domain.CleanupCafe;
import com.yusung.realestateapi.backend.integration.cleanup.infra.CleanupCafeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupCafeAreaAutoMatchService {

    private final CleanupCafeRepository cleanupCafeRepository;
    private final AreaV3Repository areaRepository;

    /**
     * 1차: 정규화 완전일치 매칭
     */
    @Transactional
    public MatchResult matchExact(Integer limit, boolean dryRun) {

        // 이미 area_id가 채워진 areaId들(유니크 제약 회피용)
        Set<Long> usedAreaIds = new HashSet<>(cleanupCafeRepository.findDistinctAreaIdsInUse());

        // 1) areas 전체를 정규화 key로 맵 구성 (중복은 ambiguous 처리)
        Map<String, Long> unique = new HashMap<>();
        Set<String> ambiguous = new HashSet<>();

        areaRepository.findAll().forEach(a -> {
            String key = NameNormalizer.norm(a.getName());
            if (key.isBlank()) return;

            Long existed = unique.get(key);
            if (existed == null) unique.put(key, a.getAreaId());
            else ambiguous.add(key);
        });

        // ambiguous는 제거(안전하게 스킵)
        ambiguous.forEach(unique::remove);

        // 2) areaId가 null인 cleanup_cafes 가져오기
        List<CleanupCafe> targets = cleanupCafeRepository.findAllByAreaIdIsNull();
        if (limit != null && limit > 0 && targets.size() > limit) {
            targets = targets.subList(0, limit);
        }

        int scanned = 0;
        int matched = 0;
        int skippedAmbiguous = 0;
        int skippedAlreadyTaken = 0;
        int noMatch = 0;

        List<CleanupCafe> toSave = new ArrayList<>();

        for (CleanupCafe c : targets) {
            scanned++;

            String key = NameNormalizer.norm(c.getAreaName());
            if (key.isBlank()) {
                noMatch++;
                continue;
            }

            Long areaId = unique.get(key);
            if (areaId == null) {
                if (ambiguous.contains(key)) skippedAmbiguous++;
                else noMatch++;
                continue;
            }

            // ✅ 1차 방어: 메모리(Set) 기준 이미 사용중이면 스킵
            if (usedAreaIds.contains(areaId)) {
                skippedAlreadyTaken++;
                continue;
            }

            // ✅ 2차 방어: DB에 실제로 존재하는지 최종 확인(이러면 duplicate 100% 방지)
            if (cleanupCafeRepository.existsByAreaId(areaId)) {
                usedAreaIds.add(areaId); // 앞으로도 계속 스킵되게 등록
                skippedAlreadyTaken++;
                continue;
            }

            matched++;
            usedAreaIds.add(areaId);

            if (!dryRun) {
                c.setAreaId(areaId);
                toSave.add(c);
            }
        }

        if (!dryRun && !toSave.isEmpty()) {
            cleanupCafeRepository.saveAll(toSave);
        }

        log.info("[AUTO-MATCH-EXACT] scanned={}, matched={}, takenSkip={}, ambiguousSkip={}, noMatch={}, dryRun={}",
                scanned, matched, skippedAlreadyTaken, skippedAmbiguous, noMatch, dryRun);

        return new MatchResult("exact", scanned, matched, skippedAlreadyTaken, skippedAmbiguous, noMatch, dryRun);
    }

    /**
     * 2차: contains(부분일치) + 점수 기반 안전 매칭
     */
    @Transactional
    public MatchResult matchContains(Integer limit, boolean dryRun, int minScore) {

        Set<Long> usedAreaIds = new HashSet<>(cleanupCafeRepository.findDistinctAreaIdsInUse());

        List<AreaKey> areaKeys = new ArrayList<>();
        for (AreaV3 a : areaRepository.findAll()) {
            String key = normForContains(a.getName());
            if (key.isBlank()) continue;
            areaKeys.add(new AreaKey(a.getAreaId(), key));
        }

        List<CleanupCafe> targets = cleanupCafeRepository.findAllByAreaIdIsNull();
        if (limit != null && limit > 0 && targets.size() > limit) {
            targets = targets.subList(0, limit);
        }

        int scanned = 0;
        int matched = 0;
        int skippedAmbiguous = 0;
        int skippedAlreadyTaken = 0;
        int noMatch = 0;

        List<CleanupCafe> toSave = new ArrayList<>();

        for (CleanupCafe c : targets) {
            scanned++;

            String ck = normForContains(c.getAreaName());
            if (ck.isBlank()) {
                noMatch++;
                continue;
            }

            BestCandidate best = findBestContains(ck, areaKeys, minScore);

            if (!best.found) {
                noMatch++;
                continue;
            }

            if (best.ambiguous) {
                skippedAmbiguous++;
                continue;
            }

            Long areaId = best.areaId;

            // ✅ 1차 방어
//            if (usedAreaIds.contains(areaId)) {
//                skippedAlreadyTaken++;
//                continue;
//            }

            // ✅ 2차 방어(duplicate 완전 차단)
            if (cleanupCafeRepository.existsByAreaId(areaId)) {
                usedAreaIds.add(areaId);
                skippedAlreadyTaken++;
                continue;
            }

            matched++;
            usedAreaIds.add(areaId);

            if (!dryRun) {
                c.setAreaId(areaId);
                toSave.add(c);
            }
        }

        if (!dryRun && !toSave.isEmpty()) {
            cleanupCafeRepository.saveAll(toSave);
        }

        log.info("[AUTO-MATCH-CONTAINS] scanned={}, matched={}, takenSkip={}, ambiguousSkip={}, noMatch={}, minScore={}, dryRun={}",
                scanned, matched, skippedAlreadyTaken, skippedAmbiguous, noMatch, minScore, dryRun);

        return new MatchResult("contains", scanned, matched, skippedAlreadyTaken, skippedAmbiguous, noMatch, dryRun);
    }

    private String normForContains(String s) {
        if (s == null) return "";
        String x = s;

        x = x.replaceAll("\\(.*?\\)", " ");

        x = x.replace("정비사업", " ");
        x = x.replace("재개발", " ");
        x = x.replace("재건축", " ");
        x = x.replace("조합", " ");
        x = x.replace("추진위원회", " ");
        x = x.replace("추진위", " ");
        x = x.replace("추진", " ");

        x = x.replace("일대", " ");
        x = x.replace("일원", " ");

        x = x.replaceAll("[^0-9a-zA-Z가-힣 ]", " ");
        x = x.replaceAll("\\s+", " ").trim();

        return x;
    }

    private BestCandidate findBestContains(String cleanupKey, List<AreaKey> areaKeys, int minScore) {
        int bestScore = -1;
        Long bestAreaId = null;
        int secondScore = -1;

        for (AreaKey ak : areaKeys) {
            String areaKey = ak.key;

            boolean contains = areaKey.contains(cleanupKey) || cleanupKey.contains(areaKey);
            if (!contains) continue;

            int score = scoreContains(cleanupKey, areaKey);

            if (score > bestScore) {
                secondScore = bestScore;
                bestScore = score;
                bestAreaId = ak.areaId;
            } else if (score > secondScore) {
                secondScore = score;
            }
        }

        if (bestScore < minScore || bestAreaId == null) {
            return BestCandidate.notFound();
        }

        boolean ambiguous = (secondScore == bestScore) || (secondScore >= 0 && (bestScore - secondScore) < 15);
        return new BestCandidate(true, ambiguous, bestAreaId, bestScore, secondScore);
    }

    private int scoreContains(String a, String b) {
        int la = a.length();
        int lb = b.length();
        int min = Math.min(la, lb);
        int diff = Math.abs(la - lb);

        int score = min * 10;
        score -= diff * 2;
        if (min < 6) score -= 50;

        return score;
    }

    private record AreaKey(Long areaId, String key) {}

    private static class BestCandidate {
        final boolean found;
        final boolean ambiguous;
        final Long areaId;
        final int bestScore;
        final int secondScore;

        private BestCandidate(boolean found, boolean ambiguous, Long areaId, int bestScore, int secondScore) {
            this.found = found;
            this.ambiguous = ambiguous;
            this.areaId = areaId;
            this.bestScore = bestScore;
            this.secondScore = secondScore;
        }

        static BestCandidate notFound() {
            return new BestCandidate(false, false, null, -1, -1);
        }
    }

    public record MatchResult(
            String mode,
            int scanned,
            int matched,
            int skippedAlreadyTaken,
            int skippedAmbiguous,
            int noMatch,
            boolean dryRun
    ) {}
}
