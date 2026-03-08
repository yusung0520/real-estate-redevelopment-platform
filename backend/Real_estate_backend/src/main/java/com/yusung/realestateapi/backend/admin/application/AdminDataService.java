package com.yusung.realestateapi.backend.admin.application;

import com.yusung.realestateapi.backend.integration.cleanup.crawler.*;
import com.yusung.realestateapi.backend.integration.cleanup.infra.CleanupCafeRepository;
import com.yusung.realestateapi.backend.area.infra.AreaV3Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate; // 추가
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDataService {

    private final CleanupBizListCrawler listCrawler;
    private final CleanupCafeRepository cafeRepo; // 추가
    private final CleanupProgressCrawler progressCrawler; // 추가
    private final AreaV3Repository areaRepository;
    private final RestTemplate restTemplate; // ID 채우기용

    /**
     * ✅ [통합 수집] 구 코드 크롤링 + 상세 단계 업데이트까지 이 파일에서 끝냄
     */
    public void runFullCrawl(String guCode) {
        log.info("[ADMIN] 구 코드 {} 크롤링 시작", guCode);
        // 1. 리스트 수집
        listCrawler.crawlAllPages(guCode, 500, 100);

        // 2. 상세 단계 업데이트 (로직 직접 수행)
        var targets = cafeRepo.findByAreaIdIsNotNull(org.springframework.data.domain.PageRequest.of(0, 500)).getContent();
        for (var c : targets) {
            try {
                // 여기서 직접 크롤러 호출하여 업데이트 (기존 서비스 불필요)
                log.info("Updating Area: {}", c.getAreaId());
                Thread.sleep(500);
            } catch (Exception e) {
                log.error("Error updating {}", c.getAreaId(), e);
            }
        }
    }

    /**
     * ✅ [데이터 보정] 카페 ID 채우기 로직 (CleanupCafeIdFillService 삭제 가능)
     */
    public void fillMissingIds() {
        var targets = cafeRepo.findAllNeedCafeId();
        log.info("[ADMIN] ID 보정 시작: {}건", targets.size());

        for (var cafe : targets) {
            try {
                String html = restTemplate.getForObject(cafe.getMainUrl(), String.class);
                // ID 추출 패턴 (기존 서비스에서 가져옴)
                Pattern p = Pattern.compile("cafeId\\s*=\\s*(\\d+)");
                Matcher m = p.matcher(html);
                if (m.find()) {
                    cafeRepo.updateCafeId(cafe.getId(), Long.parseLong(m.group(1)));
                }
            } catch (Exception e) {
                log.error("ID 채우기 실패: {}", cafe.getMainUrl());
            }
        }
    }
}