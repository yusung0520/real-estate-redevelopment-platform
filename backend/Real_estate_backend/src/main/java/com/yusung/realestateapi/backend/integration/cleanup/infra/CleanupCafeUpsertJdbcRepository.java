package com.yusung.realestateapi.backend.integration.cleanup.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CleanupCafeUpsertJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public int batchUpsert(List<CafeRow> rows) {
        if (rows == null || rows.isEmpty()) return 0;

        // ✅ stage_name 컬럼 추가
        String sql = """
            INSERT INTO cleanup_cafes
            (cafe_url, area_name, bsns_pk, main_url, stage_name, signgu_code, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, NOW())
            AS new
            ON DUPLICATE KEY UPDATE
              area_name   = new.area_name,
              bsns_pk     = COALESCE(new.bsns_pk, cleanup_cafes.bsns_pk),
              main_url    = new.main_url,
              stage_name  = new.stage_name, -- ✅ 단계 정보 업데이트
              signgu_code = new.signgu_code,
              updated_at  = NOW()
            """;

        jdbcTemplate.batchUpdate(sql, rows, 500, (ps, r) -> {
            ps.setString(1, r.cafeUrl());
            ps.setString(2, r.areaName());
            ps.setString(3, r.bsnsPk());
            ps.setString(4, r.mainUrl());
            ps.setString(5, r.stageName()); // ✅ 파라미터 매핑
            ps.setString(6, r.signguCode());
        });

        return rows.size();
    }

    public record CafeRow(
            String cafeUrl,
            String areaName,
            String bsnsPk,
            String mainUrl,
            String stageName, // ✅ frameUrl 대신 stageName으로 변경하여 의미 명확화
            String signguCode,
            String officialBizName
    ) {}
}