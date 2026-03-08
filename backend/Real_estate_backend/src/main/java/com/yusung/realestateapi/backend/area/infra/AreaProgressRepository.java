package com.yusung.realestateapi.backend.area.infra;

import com.yusung.realestateapi.backend.area.domain.AreaProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AreaProgressRepository extends JpaRepository<AreaProgress, Long> {

    boolean existsByAreaIdAndDayAndSeCdAndDtlPrcsCd(Long areaId, String day, String seCd, String dtlPrcsCd);

    // (기존) 진행이력 조회 (최신 날짜부터)
    List<AreaProgress> findByAreaIdOrderByDayDesc(Long areaId);

    // (기존) 최신 1건 (day만 기준이라 같은 day에서 애매할 수 있음)
    Optional<AreaProgress> findFirstByAreaIdOrderByDayDesc(Long areaId);

    // ✅ (추가 추천) 같은 day에 여러 건이 있어도 항상 "제일 마지막 insert"가 최신으로 잡히게
    List<AreaProgress> findByAreaIdOrderByDayDescProgressIdDesc(Long areaId);

    // ✅ (추가 추천) latest 확정 버전
    Optional<AreaProgress> findFirstByAreaIdOrderByDayDescProgressIdDesc(Long areaId);
}
