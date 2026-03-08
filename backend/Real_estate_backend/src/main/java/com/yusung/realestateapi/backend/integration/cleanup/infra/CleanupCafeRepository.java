package com.yusung.realestateapi.backend.integration.cleanup.infra;

import com.yusung.realestateapi.backend.integration.cleanup.domain.CleanupCafe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface CleanupCafeRepository extends JpaRepository<CleanupCafe, Long> {

    // ✅ 중복된 area_id가 있을 때 서버 에러를 방지하기 위해 List로 조회합니다.
    List<CleanupCafe> findAllByAreaId(Long areaId);

    // 기존 메서드들 유지
    Optional<CleanupCafe> findFirstByAreaIdOrderByIdDesc(Long areaId);
    Optional<CleanupCafe> findByAreaId(Long areaId);
    Optional<CleanupCafe> findByCafeUrl(String cafeUrl);

    Page<CleanupCafe> findByAreaIdIsNotNull(Pageable pageable);
    Page<CleanupCafe> findByAreaIdIsNotNullAndCafeUrlIsNotNull(Pageable pageable);
    Page<CleanupCafe> findByAreaIdIsNull(Pageable pageable);
    Page<CleanupCafe> findByAreaIdIsNullAndAreaNameIsNotNull(Pageable pageable);
    Page<CleanupCafe> findByAreaIdIsNullOrderByIdAsc(Pageable pageable);
    Page<CleanupCafe> findByCafeIdIsNotNull(Pageable pageable);

    List<CleanupCafe> findAllByAreaIdIsNull();

    @Query("select distinct c.areaId from CleanupCafe c where c.areaId is not null")
    List<Long> findDistinctAreaIdsInUse();

    boolean existsByAreaId(Long areaId);

    @Query("select c from CleanupCafe c where c.cafeId is null and c.mainUrl is not null")
    List<CleanupCafe> findAllNeedCafeId();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update CleanupCafe c set c.cafeId = :cafeId where c.id = :id")
    int updateCafeId(@Param("id") Long id, @Param("cafeId") Long cafeId);
}