package com.yusung.realestateapi.backend.area.api;

import com.yusung.realestateapi.backend.area.application.AreaQueryService;
import com.yusung.realestateapi.backend.area.dto.AreaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/areas", "/api/areas"}) // 주소 혼선 방지
public class AreaController {

    private final AreaQueryService areaQueryService;

    /**
     * ✅ [통합 검색] 이름/사업자번호 검색 (400 에러 해결)
     */
    @GetMapping("/search")
    public List<AreaDto.SearchItem> search(@RequestParam("q") String query) {
        return areaQueryService.searchAreas(query);
    }

    /**
     * ✅ [통합 상세] 상세정보, 타임라인, 중개사를 한 번에 반환
     */
    @GetMapping("/{areaId}/detail")
    public AreaDto.Detail getDetail(@PathVariable Long areaId) {
        return areaQueryService.getAreaDetail(areaId);
    }

    /**
     * ✅ [목록 조회] 지도 폴리곤 및 리스트 데이터
     */
    @GetMapping
    public Page<AreaDto.Summary> getAreas(Pageable pageable) {
        // 기존 흩어져 있던 목록 조회 로직을 Service 하나로 위임하여 호출
        return areaQueryService.getAreaSummaryList(pageable);
    }
}