package com.yusung.realestateapi.backend.integration.cleanup.infra;

import com.yusung.realestateapi.backend.integration.cleanup.domain.CleanupProgress;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CleanupProgressRepository extends JpaRepository<CleanupProgress, Long> {

    // ✅ 단계명은 바뀔 수 있으므로 stepNo로 찾는 이 메서드가 가장 중요합니다.
    Optional<CleanupProgress> findByAreaIdAndStepNo(Long areaId, Integer stepNo);

    Optional<CleanupProgress> findByAreaIdAndStepName(Long areaId, String stepName);

    List<CleanupProgress> findByAreaIdOrderByStepNoAsc(Long areaId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update CleanupProgress p set p.current=false where p.areaId=:areaId and p.current=true")
    int clearCurrent(@Param("areaId") Long areaId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        INSERT INTO cleanup_progresses
            (area_id, step_no, step_name, event_date, is_current, source_url, updated_at)
        VALUES
            (:areaId, :stepNo, :stepName, :eventDate, :isCurrent, :sourceUrl, NOW())
        ON DUPLICATE KEY UPDATE
            step_name  = VALUES(step_name),
            event_date = VALUES(event_date),
            is_current = VALUES(is_current),
            source_url = VALUES(source_url),
            updated_at = NOW()
        """, nativeQuery = true)
    void upsert(
            @Param("areaId") Long areaId,
            @Param("stepNo") Integer stepNo,
            @Param("stepName") String stepName,
            @Param("eventDate") LocalDate eventDate,
            @Param("isCurrent") boolean isCurrent,
            @Param("sourceUrl") String sourceUrl
    );
}