package com.yusung.realestateapi.backend.integration.cleanup.application;

import com.yusung.realestateapi.backend.integration.cleanup.domain.CleanupCafe;
import com.yusung.realestateapi.backend.integration.cleanup.infra.CleanupCafeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupCafeAreaMatchService {

    private final CleanupCafeRepository cafeRepo;
    private final JdbcTemplate jdbcTemplate;

    /**
     * cleanup_cafes.area_id가 null인 애들 중 limit개를 가져와서
     * areas.name 기준으로 자동 매칭 시도.
     */
    @Transactional
    public MatchResult matchBatch(int limit) {

        // 1) areas 전체를 (area_id, name)로 한 번 로드해서 정규화 인덱스 생성
        List<AreaRow> areas = jdbcTemplate.query(
                "select area_id, name from areas",
                (rs, rowNum) -> new AreaRow(rs.getLong("area_id"), rs.getString("name"))
        );

        // normalizedName -> area_id 후보들(동명이인 대비)
        Map<String, List<Long>> areaIndex = new HashMap<>();
        for (AreaRow a : areas) {
            String key = normalize(a.name());
            if (key.isBlank()) continue;
            areaIndex.computeIfAbsent(key, k -> new ArrayList<>()).add(a.areaId());
        }

        // 2) 매칭 대상 cafes (area_id null)
        var page = cafeRepo.findByAreaIdIsNullOrderByIdAsc(PageRequest.of(0, limit));
        List<CleanupCafe> targets = page.getContent();

        int total = targets.size();
        int matched = 0;
        int ambiguous = 0;
        int nohit = 0;

        for (CleanupCafe c : targets) {
            String rawName = nvl(c.getAreaName());
            String key = normalize(rawName);

            if (key.isBlank()) {
                nohit++;
                continue;
            }

            // 2-1) 정규화 exact 매칭
            List<Long> candidateIds = areaIndex.getOrDefault(key, List.of());

            if (candidateIds.size() == 1) {
                c.setAreaId(candidateIds.get(0));
                cafeRepo.save(c);
                matched++;
                continue;
            }

            if (candidateIds.size() >= 2) {
                ambiguous++;
                log.warn("[MATCH] ambiguous exact: cafeId={} raw='{}' key='{}' candidates={}",
                        c.getId(), rawName, key, candidateIds);
                continue;
            }

            // 2-2) 부분 매칭(보수적으로: 결과가 1개일 때만 자동)
            // - 너무 짧으면 오탐 위험이라 skip
            if (key.length() < 3) {
                nohit++;
                continue;
            }

            List<Long> likeIds = jdbcTemplate.query(
                    """
                    select area_id
                    from areas
                    where replace(replace(replace(name,' ',''),'(주)',''),'()','') like ?
                    limit 5
                    """,
                    ps -> ps.setString(1, "%" + rawName.replace(" ", "") + "%"),
                    (rs, rowNum) -> rs.getLong("area_id")
            );

            if (likeIds.size() == 1) {
                c.setAreaId(likeIds.get(0));
                cafeRepo.save(c);
                matched++;
            } else if (likeIds.size() >= 2) {
                ambiguous++;
                log.warn("[MATCH] ambiguous like: cafeId={} raw='{}' key='{}' likeCandidates={}",
                        c.getId(), rawName, key, likeIds);
            } else {
                nohit++;
            }
        }

        log.info("[MATCH] done total={} matched={} ambiguous={} nohit={}", total, matched, ambiguous, nohit);
        return new MatchResult(total, matched, ambiguous, nohit);
    }

    private String normalize(String s) {
        if (s == null) return "";
        String v = s.trim();

        // 공백 제거
        v = v.replace(" ", "");

        // 괄호 제거(간단 버전)
        v = v.replaceAll("\\(.*?\\)", "");

        // 흔한 접미어 제거 (필요하면 더 추가)
        v = v.replace("도시환경정비구역", "");
        v = v.replace("재개발", "");
        v = v.replace("재건축", "");
        v = v.replace("정비구역", "");
        v = v.replace("구역", "");
        v = v.replace("지구", "");

        // 특수문자 제거
        v = v.replaceAll("[^0-9A-Za-z가-힣]", "");

        return v;
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private record AreaRow(Long areaId, String name) {}
    public record MatchResult(int total, int matched, int ambiguous, int nohit) {}
}