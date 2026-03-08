package com.yusung.realestateapi.backend.integration.cleanup.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CleanupCafeMainFillJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * ✅ A안 핵심: MainFill 대상 조회
     * - main_url은 있는데
     * - cafe_id 또는 frame_url이 비어있는 애들만 뽑는다
     */
    public List<TargetRow> findTargets(int limit) {
        String sql = """
            SELECT id, main_url
            FROM cleanup_cafes
            WHERE main_url IS NOT NULL AND main_url <> ''
              AND (
                   cafe_id IS NULL OR cafe_id = ''
                OR frame_url IS NULL OR frame_url = ''
              )
            ORDER BY id ASC
            LIMIT ?
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new TargetRow(
                        rs.getLong("id"),
                        rs.getString("main_url")
                ), limit);
    }

    /**
     * ✅ 서비스에서 호출하는 이름 그대로 맞춰줌
     * CleanupCafeMainFillService.java 에서 repo.updateCafeMain(...) 쓰고 있으니
     * 레포지토리에 동일한 시그니처로 제공
     */
    public int updateCafeMain(Long id, String cafeId, String frameUrl) {
        String sql = """
            UPDATE cleanup_cafes
            SET cafe_id = ?,
                frame_url = ?,
                updated_at = NOW()
            WHERE id = ?
            """;
        return jdbcTemplate.update(sql, cafeId, frameUrl, id);
    }

    /** ✅ 배치 업데이트(여러 건 한번에) */
    public int[] batchUpdateMainFill(List<MainFillRow> rows) {
        if (rows == null || rows.isEmpty()) return new int[0];

        String sql = """
            UPDATE cleanup_cafes
            SET cafe_id = ?,
                frame_url = ?,
                updated_at = NOW()
            WHERE id = ?
            """;

        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                MainFillRow r = rows.get(i);
                ps.setString(1, r.cafeId());
                ps.setString(2, r.frameUrl());
                ps.setLong(3, r.id());
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
    }

    /** ✅ A안: 대상 조회용 Row */
    public record TargetRow(Long id, String mainUrl) {}

    /** ✅ 배치 업데이트용 Row */
    public record MainFillRow(Long id, String cafeId, String frameUrl) {}
}
