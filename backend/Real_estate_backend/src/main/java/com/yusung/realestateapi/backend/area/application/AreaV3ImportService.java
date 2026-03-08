package com.yusung.realestateapi.backend.area.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yusung.realestateapi.backend.area.domain.AreaV3;
import com.yusung.realestateapi.backend.area.infra.AreaV3Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AreaV3ImportService {

    private final AreaV3Repository areaV3Repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * ✅ 권장: importAll은 트랜잭션으로 묶어주는 게 보통 더 안정적임
     * (단, 데이터가 너무 크면 batch 설정을 따로 하는 게 더 좋긴 함)
     */
    @Transactional
    public String importAll(String filePath) throws Exception {
        String json = Files.readString(Path.of(filePath));
        JsonNode root = objectMapper.readTree(json);
        JsonNode features = root.path("features");

        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        int skippedNull = 0;
        int skippedNullUq = 0;
        int skippedNoSigungu = 0;

        for (JsonNode feature : features) {
            JsonNode props = feature.path("properties");
            JsonNode geom = feature.path("geometry");

            String presentSn = props.path("PRESENT_SN").asText(null);
            String name = props.path("DGM_NM").asText(null);
            String wtnncSn = props.path("WTNNC_SN").asText(null);
            String ntfcSn  = props.path("NTFC_SN").asText(null);

            String sigunguCd = props.path("sigungu_cd").asText(null);
            if (sigunguCd == null || sigunguCd.isBlank()) {
                sigunguCd = props.path("SIGNGU_SE").asText(null);
            }

            // 1) present_sn 비어있으면 스킵
            if (presentSn == null || presentSn.isBlank()) {
                skipped++; skippedNull++;
                continue;
            }

            // 2) NULLUQ... 스킵
            if (presentSn.startsWith("NULLUQ")) {
                skipped++; skippedNullUq++;
                continue;
            }

            // 3) 구코드 비어있으면 스킵 (너는 구코드 기반 매칭이라 스킵 유지 추천)
            if (sigunguCd == null || sigunguCd.isBlank()) {
                skipped++; skippedNoSigungu++;
                continue;
            }

            String geomJson = objectMapper.writeValueAsString(geom);

            // ✅ 업서트
            var existingOpt = areaV3Repository.findByPresentSn(presentSn);

            AreaV3 entity = existingOpt.orElseGet(() -> AreaV3.builder()
                    .presentSn(presentSn)
                    .build()
            );

            entity.setName(name);
            entity.setSigunguCd(sigunguCd);
            entity.setWtnncSn(wtnncSn);
            entity.setNtfcSn(ntfcSn);
            entity.setPolygonGeojson(geomJson);

            areaV3Repository.save(entity);

            if (existingOpt.isPresent()) updated++;
            else inserted++;
        }

        return "DONE - inserted: " + inserted
                + ", updated: " + updated
                + ", skipped: " + skipped
                + " (null_present_sn=" + skippedNull
                + ", nulluq_present_sn=" + skippedNullUq
                + ", no_sigungu_cd=" + skippedNoSigungu
                + ")";
    }
}