package com.yusung.realestateapi.backend.area.application;

import com.yusung.realestateapi.backend.area.domain.AreaV3;
import com.yusung.realestateapi.backend.area.dto.AreaDto;
import com.yusung.realestateapi.backend.area.infra.*;
import com.yusung.realestateapi.backend.integration.cleanup.domain.CleanupCafe;
import com.yusung.realestateapi.backend.integration.cleanup.infra.CleanupCafeRepository;
import com.yusung.realestateapi.backend.integration.cleanup.infra.CleanupProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AreaQueryService {

    private final AreaV3Repository areaRepository;
    private final CleanupProgressRepository cleanupProgressRepository;
    private final AreaProgressRepository areaProgressRepository;
    private final AreaAgentRepository areaAgentRepository;
    private final AgentRepository agentRepository;
    private final CleanupCafeRepository cleanupCafeRepository;

    public List<AreaDto.SearchItem> searchAreas(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        return areaRepository.searchByQuery(query).stream()
                .map(a -> new AreaDto.SearchItem(
                        a.getAreaId(),
                        a.getName(),
                        a.getStage(),
                        a.getCenterLat(),
                        a.getCenterLng()
                ))
                .toList();
    }

    public Page<AreaDto.Summary> getAreaSummaryList(Pageable pageable) {
        return areaRepository.findMatchedAreas(pageable)
                .map(a -> new AreaDto.Summary(
                        a.getAreaId(),
                        a.getName(),
                        a.getStage(),
                        a.getSigunguCd(),
                        a.getPolygonGeojson(),
                        a.getCenterLat(),
                        a.getCenterLng()
                ));
    }

    public AreaDto.Detail getAreaDetail(Long areaId) {
        AreaV3 area = areaRepository.findById(areaId)
                .orElseThrow(() -> new IllegalArgumentException("구역을 찾을 수 없습니다."));

        List<CleanupCafe> cafes = cleanupCafeRepository.findAllByAreaId(areaId);

        CleanupCafe cafe = cafes.stream()
                .findFirst()
                .orElse(null);

        String displayName = area.getName();
        if (cafe != null && cafe.getBusinessName() != null) {
            if (displayName.contains("주택재") || displayName.contains("도시환경")
                    || displayName.contains("사업구역") || displayName.equals("재건축사업구역")) {
                displayName = cafe.getBusinessName();
            }
        }

        String currentStageName = (cafe != null && cafe.getStageName() != null)
                ? cafe.getStageName().trim()
                : area.getStage();

        var timeline = cleanupProgressRepository.findByAreaIdOrderByStepNoAsc(areaId).stream()
                .map(p -> {
                    boolean isCurrentMatch =
                            (currentStageName != null && currentStageName.equals(p.getStepName()));
                    return new AreaDto.TimelineItem(
                            p.getStepName(),
                            p.getEventDate(),
                            isCurrentMatch,
                            p.getEventDate() != null
                    );
                })
                .toList();

        var history = areaProgressRepository.findByAreaIdOrderByDayDescProgressIdDesc(areaId).stream()
                .map(p -> new AreaDto.ProgressItem(
                        p.getDay(),
                        p.getSeNm(),
                        p.getTtl(),
                        p.getDtlCn()
                ))
                .toList();

        var agents = areaAgentRepository.findByAreaIdOrderByIsFeaturedDescAreaAgentIdAsc(areaId).stream()
                .map(link -> {
                    var agent = agentRepository.findById(link.getAgentId()).orElse(null);
                    if (agent == null) {
                        return null;
                    }
                    return new AreaDto.AgentItem(
                            agent.getAgentId(),
                            agent.getName(),
                            agent.getOfficeName(),
                            agent.getPhone(),
                            link.getIsFeatured() == 1
                    );
                })
                .filter(Objects::nonNull)
                .toList();

        return new AreaDto.Detail(
                area.getAreaId(),
                displayName,
                currentStageName,
                area.getSigunguCd(),
                area.getEmdNm(),
                area.getPolygonGeojson(),
                timeline,
                history,
                agents
        );
    }
}