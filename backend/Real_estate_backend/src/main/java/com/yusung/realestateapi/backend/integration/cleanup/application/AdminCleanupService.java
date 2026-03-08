package com.yusung.realestateapi.backend.integration.cleanup.application;

import com.yusung.realestateapi.backend.area.infra.AreaV3Repository; // ✅ V3로 단일화
import com.yusung.realestateapi.backend.integration.cleanup.crawler.*;
import com.yusung.realestateapi.backend.integration.cleanup.domain.CleanupCafe;
import com.yusung.realestateapi.backend.integration.cleanup.infra.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCleanupService {

    private final CleanupCafeRepository cafeRepo;
    private final CleanupProgressRepository progressRepo;
    private final CleanupCafeMainCrawler cafeMainCrawler;
    private final CleanupProgressCrawler progressCrawler;
    private final CleanupBizListCrawler listCrawler;
    private final CleanupProgressUpsertService upsertService;
    private final AreaV3Repository areaRepo; // ✅ V3 타입 사용

    /**
     * ✅ [통합 임포트] 특정 카페 URL을 기반으로 수집/업데이트
     */
    @Transactional
    public void importByCafeUrl(Long areaId, String cafeUrl) throws Exception {
        var main = cafeMainCrawler.crawlMain(cafeUrl);
        var progress = progressCrawler.crawlProgress(
                main.executeUrlCafeId(), main.executeUrlCafeLd(), main.mainUrl(), main.cookies()
        );

        if (!progress.ok()) {
            log.warn("[CLEANUP] 수집 실패: areaId={}, url={}", areaId, cafeUrl);
            return;
        }

        CleanupCafe cafe = cafeRepo.findByCafeUrl(cafeUrl).orElseGet(CleanupCafe::new);
        cafe.setAreaId(areaId);
        cafe.setCafeUrl(cafeUrl);
        cafe.setBusinessName(main.businessName());
        cafe.setAreaName(main.areaName());
        cafe.setMainUrl(main.mainUrl());
        cafe.setCafeId(main.executeUrlCafeId());
        cafeRepo.save(cafe);

        List<CleanupProgressUpsertService.Item> items = new ArrayList<>();
        Integer currentStepNo = null;

        for (var s : progress.stages()) {
            Integer stepNo = parseStepNo(s.stepNoRaw());
            if (stepNo == null) continue;

            LocalDate date = parseDate(s.dateRaw());
            items.add(new CleanupProgressUpsertService.Item(stepNo, s.stepName(), date));
            if (s.current()) currentStepNo = stepNo;
        }

        upsertService.upsertAll(areaId, items, main.mainUrl(), currentStepNo);

        if (currentStepNo != null) {
            String currentName = progress.currentStage();
            // ✅ AreaV3Repository를 통해 단계 업데이트
            areaRepo.updateStage(areaId, currentName);
        }

        log.info("[CLEANUP] 수집 완료: areaId={}, stage={}", areaId, progress.currentStage());
    }

    public void updateAllMatchedStages(int limit, int sleepMs) {
        var targets = cafeRepo.findByAreaIdIsNotNull(PageRequest.of(0, limit)).getContent();
        for (CleanupCafe c : targets) {
            try {
                importByCafeUrl(c.getAreaId(), c.getCafeUrl());
                if (sleepMs > 0) Thread.sleep(sleepMs);
            } catch (Exception e) {
                log.error("[BATCH] 실패: cafeUrl={}, areaId={}", c.getCafeUrl(), c.getAreaId(), e);
            }
        }
    }

    public void crawlSeoulList(List<String> signguCodes, int maxPages) {
        for (String code : signguCodes) {
            listCrawler.crawlAllPages(code, 500, maxPages);
        }
    }

    private Integer parseStepNo(String raw) {
        if (raw == null) return null;
        try { return Integer.parseInt(raw.replaceAll("[^0-9]", "")); } catch (Exception e) { return null; }
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String s = raw.replace("/", ".").replace("-", ".").replaceAll("[^0-9.]", "");
        String[] p = s.split("\\.");
        if (p.length != 3) return null;
        try {
            int y = Integer.parseInt(p[0]);
            if (y < 100) y += 2000;
            return LocalDate.of(y, Integer.parseInt(p[1]), Integer.parseInt(p[2]));
        } catch (Exception e) { return null; }
    }
}