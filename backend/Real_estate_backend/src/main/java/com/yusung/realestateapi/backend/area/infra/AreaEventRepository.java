package com.yusung.realestateapi.backend.area.infra;

import com.yusung.realestateapi.backend.area.domain.AreaEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDate;

public interface AreaEventRepository extends JpaRepository<AreaEvent, Long> {

    // ✅ 삭제된 DTO(AreaEventDocumentRow) 참조를 제거하고 기본 엔티티 리스트를 반환하도록 수정
    List<AreaEvent> findByAreaIdOrderByEventDateDesc(Long areaId);

    // ✅ 공식 이벤트(Official) 조회
    List<AreaEvent> findByAreaIdInAndIsOfficialTrueOrderByAreaIdAscEventDateDescIdDesc(List<Long> areaIds);

    // ✅ 중복 체크 로직들
    boolean existsByAreaIdAndEventTypeAndEventDateAndDocumentId(
            Long areaId, String eventType, LocalDate eventDate, Long documentId
    );

    boolean existsByAreaIdAndDocumentIdAndStageKey(
            Long areaId,
            Long documentId,
            String stageKey
    );

    // ✅ 단계별 최초 발생일 조회 (통계/타임라인용)
    @Query("""
        select e.stageKey, e.stageLabel, min(e.eventDate)
        from AreaEvent e
        where e.areaId = :areaId
          and e.eventType = 'STAGE'
          and e.stageKey is not null
          and e.eventDate is not null
        group by e.stageKey, e.stageLabel
    """)
    List<Object[]> findStageMinDates(@Param("areaId") Long areaId);

    boolean existsByAreaIdAndIsOfficialTrue(Long areaId);
}