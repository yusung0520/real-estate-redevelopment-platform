package com.yusung.realestateapi.backend.area.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AreaDto {

    // ✅ 검색창 자동완성용 (검색 시 필요한 최소 정보)
    public record SearchItem(
            Long areaId,
            String name,
            String stage,
            BigDecimal centerLat,
            BigDecimal centerLng
    ) {}

    // ✅ 상세 페이지용 (모든 정보를 한 번에 묶어서 반환)
    public record Detail(
            Long areaId,
            String name,
            String stage,
            String sigunguCd,
            String emdNm,
            String polygonGeojson,
            List<TimelineItem> timeline,  // 타임라인 데이터
            List<ProgressItem> history,    // 상세 진행이력
            List<AgentItem> agents         // 담당 중개사
    ) {}

    // 타임라인 항목
    public record TimelineItem(
            String label,
            LocalDate date,
            boolean isCurrent,
            boolean done
    ) {}

    // 상세 진행이력 (OpenAPI/크롤링 데이터)
    public record ProgressItem(
            String day,
            String stepName,
            String title,
            String content
    ) {}

    // 중개사 항목
    public record AgentItem(
            Long agentId,
            String name,
            String officeName,
            String phone,
            boolean featured
    ) {}

    public record Summary(
            Long areaId,
            String name,
            String stage,
            String polygon) {}
}