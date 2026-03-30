package com.yusung.realestateapi.backend.area.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AreaDto {

    public record SearchItem(
            Long areaId,
            String name,
            String stage,
            BigDecimal centerLat,
            BigDecimal centerLng
    ) {}

    public record Detail(
            Long areaId,
            String name,
            String stage,
            String sigunguCd,
            String emdNm,
            String polygonGeojson,
            List<TimelineItem> timeline,
            List<ProgressItem> history,
            List<AgentItem> agents
    ) {}

    public record TimelineItem(
            String label,
            LocalDate date,
            boolean isCurrent,
            boolean done
    ) {}

    public record ProgressItem(
            String day,
            String stepName,
            String title,
            String content
    ) {}

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
            String sigunguCd,
            String polygon,
            BigDecimal centerLat,
            BigDecimal centerLng
    ) {}
}