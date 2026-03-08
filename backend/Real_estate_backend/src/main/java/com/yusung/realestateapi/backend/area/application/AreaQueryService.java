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

    /**
     * [통합 검색]
     */
    public List<AreaDto.SearchItem> searchAreas(String query) {
        if (query == null || query.isBlank()) return List.of();

        return areaRepository.searchByQuery(query).stream()
                .map(a -> new AreaDto.SearchItem(
                        a.getAreaId(),
                        a.getName(),
                        a.getStage(),
                        a.getCenterLat(),
                        a.getCenterLng()))
                .toList();
    }

    /**
     * [목록 조회]
     */
    public Page<AreaDto.Summary> getAreaSummaryList(Pageable pageable) {
        return areaRepository.findMatchedAreas(pageable)
                .map(a -> new AreaDto.Summary(
                        a.getAreaId(),
                        a.getName(),
                        a.getStage(),
                        a.getPolygonGeojson()
                ));
    }

    /**
     * [통합 상세 조회]
     * 중복 에러 방지 및 가짜 이름 보정 로직 포함
     */
    public AreaDto.Detail getAreaDetail(Long areaId) {
        // 1. 폴리곤 기본 정보 조회 (areas_v3)
        AreaV3 area = areaRepository.findById(areaId)
                .orElseThrow(() -> new IllegalArgumentException("구역을 찾을 수 없습니다."));

        // ✅ 2. 중복 에러 방지 (findAllByAreaId로 리스트 조회)
        List<CleanupCafe> cafes = cleanupCafeRepository.findAllByAreaId(areaId);

        // ✅ 3. 중복 데이터 중 '진짜' 고르기
        // CleanupCafe 엔티티의 시군구 필드명이 불확실하므로, 에러 방지를 위해 stream 로직을 안전하게 수정합니다.
        CleanupCafe cafe = cafes.stream()
                .findFirst() // 우선순위 필터링이 필요 없다면 첫 번째 데이터를 가져옵니다.
                .orElse(null);

        // ✅ 4. 이름 보정 로직 (몽땅의 가짜 이름 대신 클린업의 진짜 조합명을 사용)
        String displayName = area.getName();
        if (cafe != null && cafe.getBusinessName() != null) {
            // 이름이 '주택재건축', '도시환경', '사업구역' 등 공통 명칭인 경우 클린업의 구체적인 이름으로 교체
            if (displayName.contains("주택재") || displayName.contains("도시환경") ||
                    displayName.contains("사업구역") || displayName.equals("재건축사업구역")) {
                displayName = cafe.getBusinessName();
            }
        }

        // ✅ 5. 실시간 '현재 단계(stage_name)' 가져오기
        String currentStageName = (cafe != null && cafe.getStageName() != null)
                ? cafe.getStageName().trim()
                : area.getStage();

        // 6. 타임라인 조립 (자동 매칭 로직 포함)
        var timeline = cleanupProgressRepository.findByAreaIdOrderByStepNoAsc(areaId).stream()
                .map(p -> {
                    boolean isCurrentMatch = (currentStageName != null && currentStageName.equals(p.getStepName()));
                    return new AreaDto.TimelineItem(
                            p.getStepName(),
                            p.getEventDate(),
                            isCurrentMatch,
                            p.getEventDate() != null
                    );
                })
                .toList();

        // 7. 진행이력 조립
        var history = areaProgressRepository.findByAreaIdOrderByDayDescProgressIdDesc(areaId).stream()
                .map(p -> new AreaDto.ProgressItem(
                        p.getDay(),
                        p.getSeNm(),
                        p.getTtl(),
                        p.getDtlCn()
                ))
                .toList();

        // 8. 중개사 정보 조립
        var agents = areaAgentRepository.findByAreaIdOrderByIsFeaturedDescAreaAgentIdAsc(areaId).stream()
                .map(link -> {
                    var agent = agentRepository.findById(link.getAgentId()).orElse(null);
                    if (agent == null) return null;
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

        // ✅ 9. 보정된 displayName을 사용하여 최종 결과 반환
        return new AreaDto.Detail(
                area.getAreaId(),
                displayName,      // 👈 '주택재건축사업' 대신 '진짜 조합 이름'이 전달됨
                currentStageName, // 👈 클린업 기준 최신 단계가 전달됨
                area.getSigunguCd(),
                area.getEmdNm(),
                area.getPolygonGeojson(),
                timeline,
                history,
                agents
        );
    }
}