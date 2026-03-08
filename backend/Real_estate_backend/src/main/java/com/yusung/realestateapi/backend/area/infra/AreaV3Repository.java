package com.yusung.realestateapi.backend.area.infra;

import com.yusung.realestateapi.backend.area.domain.AreaV3;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface AreaV3Repository extends JpaRepository<AreaV3, Long> {

    Optional<AreaV3> findByPresentSn(String presentSn);

    /**
     * ✅ [매칭된 구역 조회]
     * 클린업 카페 정보가 존재하는 구역만 필터링하여 가져옵니다.
     */
    @Query("""
        SELECT a FROM AreaV3 a 
        WHERE EXISTS (SELECT 1 FROM CleanupCafe c WHERE c.areaId = a.areaId)
    """)
    Page<AreaV3> findMatchedAreas(Pageable pageable);

    /**
     * ✅ 단계 업데이트 쿼리
     */
    @Modifying
    @Transactional
    @Query("UPDATE AreaV3 a SET a.stage = :stage WHERE a.areaId = :areaId")
    void updateStage(@Param("areaId") Long areaId, @Param("stage") String stage);

    /**
     * ✅ 활성 구역 조회 (stage가 존재하는 구역들)
     */
    @Query("SELECT a FROM AreaV3 a WHERE a.stage IS NOT NULL")
    List<AreaV3> findActiveAreas();

    /**
     * ✅ 통합 검색 쿼리
     */
    @Query(value = """
        SELECT a.* FROM areas_v3 a
        LEFT JOIN cleanup_cafes c ON a.area_id = c.area_id
        WHERE a.name LIKE %:query% 
           OR c.business_name LIKE %:query%
        LIMIT 20
        """, nativeQuery = true)
    List<AreaV3> searchByQuery(@Param("query") String query);

    /**
     * ✅ 정식 명칭(조합 명칭) 우선 조회
     */
    @Query(value = """
        SELECT a.*, 
               COALESCE(NULLIF(c.business_name, ''), a.name) as display_name
        FROM areas_v3 a
        LEFT JOIN cleanup_cafes c ON a.area_id = c.area_id
        WHERE a.area_id = :areaId
        """, nativeQuery = true)
    Optional<AreaV3> findByIdWithBusinessName(@Param("areaId") Long areaId);

    List<AreaV3> findAllByPolygonGeojsonIsNotNull();
}