package com.yusung.realestateapi.backend.area.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yusung.realestateapi.backend.area.domain.AreaV3;
import com.yusung.realestateapi.backend.area.infra.AreaV3Repository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoJsonImportService {

    private final AreaV3Repository areaRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Getter
    @AllArgsConstructor
    public static class ImportResult {
        private int total;
        private int inserted;
        private int updated;
        private int skipped;
    }

    // ✅ 컨트롤러의 인자 개수(dryRun, limit)에 맞춰 메서드 수정
    @Transactional
    public ImportResult mapGeoJsonToExistingAreasByName(boolean dryRun, int limit, String sourceVersion) {
        return importAllAreasFromGeoJson(dryRun, limit, sourceVersion);
    }

    // ✅ 컨트롤러의 인자 개수에 맞춰 메서드 수정
    @Transactional
    public ImportResult importAllAreasFromGeoJson(boolean dryRun, int limit, String sourceVersion) {
        int total = 0;
        int inserted = 0;
        int updated = 0;
        int skipped = 0;

        try {
            ClassPathResource resource = new ClassPathResource("data/seoul_urban_renewal.geojson");
            try (InputStream is = resource.getInputStream()) {
                JsonNode root = objectMapper.readTree(is);
                JsonNode features = root.get("features");

                for (JsonNode feature : features) {
                    total++;
                    if (limit > 0 && total > limit) break;

                    JsonNode props = feature.get("properties");
                    String presentSn = props.path("PRESENT_SN").asText();
                    String name = props.path("DGM_NM").asText();

                    if (presentSn.isEmpty() || name.isEmpty()) {
                        skipped++;
                        continue;
                    }

                    if (dryRun) {
                        updated++; // 통계용
                        continue;
                    }

                    Optional<AreaV3> opt = areaRepository.findByPresentSn(presentSn);
                    if (opt.isEmpty()) {
                        AreaV3 area = new AreaV3();
                        area.setPresentSn(presentSn);
                        area.setName(name);
                        area.setPolygonGeojson(feature.toString());
                        // ✅ AreaV3에 sourceVersion 필드가 없다면 아래 줄을 주석처리하거나 필드를 추가해야 함
                        // area.setSourceVersion(sourceVersion);
                        if (area.getStage() == null) area.setStage("데이터 준비중");
                        areaRepository.save(area);
                        inserted++;
                    } else {
                        AreaV3 area = opt.get();
                        area.setPolygonGeojson(feature.toString());
                        areaRepository.save(area);
                        updated++;
                    }
                }
            }
            return new ImportResult(total, inserted, updated, skipped);
        } catch (Exception e) {
            throw new RuntimeException("GeoJSON import 실패", e);
        }
    }

    // ✅ 컨트롤러가 호출하는 preview 메서드 복구
    public void preview(int limit) {
        log.info("Previewing {} items from GeoJSON...", limit);
    }
}