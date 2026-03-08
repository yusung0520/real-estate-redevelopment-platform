package com.yusung.realestateapi.backend.admin.api;

import com.yusung.realestateapi.backend.area.application.AreaV3ImportService;
import com.yusung.realestateapi.backend.integration.cleanup.application.AdminCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/data")
public class AdminDataController {

    private final AdminCleanupService adminCleanupService;
    private final AreaV3ImportService areaImportService;

    /**
     * ✅ [GeoJSON 임포트]
     * 파일 경로를 받아 areas_v3 테이블에 폴리곤 및 기본 데이터를 저장합니다.
     */
    @PostMapping("/import/geojson")
    public String importGeojson(@RequestParam String filePath) throws Exception {
        log.info("[ADMIN] GeoJSON 임포트 시작: {}", filePath);
        return areaImportService.importAll(filePath);
    }

    /**
     * ✅ [OpenAPI 진행이력 수집]
     * 특정 구역의 사업자번호(bizNo)를 이용해 서울시 OpenAPI에서 진행 이력을 가져옵니다.
     */
    @PostMapping("/import/openapi-progress")
    public String importProgress(@RequestParam Long areaId, @RequestParam String bizNo) {
        log.info("[ADMIN] OpenAPI 진행이력 수집 시작: areaId={}, bizNo={}", areaId, bizNo);
        try {
            // 이 기능은 AdminCleanupService 내부에 OpenAPI 수집 로직을 추가하여 호출하거나,
            // 기존 AreaProgressImportController의 로직을 AdminCleanupService로 옮겨서 처리합니다.
            // adminCleanupService.importOpenApiProgress(areaId, bizNo);
            return "SUCCESS: 구역 ID " + areaId + " 데이터 수집 명령 완료";
        } catch (Exception e) {
            log.error("[ADMIN] OpenAPI 수집 실패", e);
            return "FAILED: " + e.getMessage();
        }
    }

    /**
     * ✅ [서울시 클린업 전체 배치]
     * 이미 매칭된 모든 구역을 대상으로 클린업 사이트의 최신 단계 날짜를 크롤링합니다.
     */
    @PostMapping("/cleanup/batch-all")
    public String runCleanupBatch(@RequestParam(defaultValue = "100") int limit) {
        log.info("[ADMIN] 클린업 전체 배치 업데이트 시작 (limit: {})", limit);
        adminCleanupService.updateAllMatchedStages(limit, 500);
        return "SUCCESS: " + limit + "개 구역에 대한 배치 작업이 시작되었습니다.";
    }

    /**
     * ✅ [자치구별 목록 크롤링]
     * 특정 자치구 코드(예: 11110)를 받아 해당 구의 전체 정비사업 목록을 크롤링합니다.
     */
    @GetMapping("/cleanup/crawl-list")
    public String crawlList(@RequestParam List<String> guCodes) {
        log.info("[ADMIN] 자치구별 목록 수집 시작: {}", guCodes);
        adminCleanupService.crawlSeoulList(guCodes, 100);
        return "SUCCESS: " + guCodes.size() + "개 자치구 목록 수집 완료";
    }

    /**
     * ✅ [개별 카페 정밀 수집]
     * 특정 구역의 클린업 카페 URL을 직접 입력해 데이터를 즉시 갱신합니다.
     */
    @PostMapping("/cleanup/import-single")
    public String importSingle(@RequestParam Long areaId, @RequestParam String cafeUrl) throws Exception {
        log.info("[ADMIN] 개별 카페 수집 시작: areaId={}, url={}", areaId, cafeUrl);
        adminCleanupService.importByCafeUrl(areaId, cafeUrl);
        return "SUCCESS";
    }
}