package com.yusung.realestateapi.backend.area.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yusung.realestateapi.backend.area.domain.AreaV3;
import com.yusung.realestateapi.backend.area.infra.AreaV3Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class AreaV3ImportService {

    private final AreaV3Repository areaV3Repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        int skippedNoCenter = 0;

        for (JsonNode feature : features) {
            JsonNode props = feature.path("properties");
            JsonNode geom = feature.path("geometry");

            String presentSn = props.path("PRESENT_SN").asText(null);
            String name = props.path("DGM_NM").asText(null);
            String wtnncSn = props.path("WTNNC_SN").asText(null);
            String ntfcSn = props.path("NTFC_SN").asText(null);

            String sigunguCd = props.path("sigungu_cd").asText(null);
            if (sigunguCd == null || sigunguCd.isBlank()) {
                sigunguCd = props.path("SIGNGU_SE").asText(null);
            }

            if (presentSn == null || presentSn.isBlank()) {
                skipped++;
                skippedNull++;
                continue;
            }

            if (presentSn.startsWith("NULLUQ")) {
                skipped++;
                skippedNullUq++;
                continue;
            }

            if (sigunguCd == null || sigunguCd.isBlank()) {
                skipped++;
                skippedNoSigungu++;
                continue;
            }

            String geomJson = objectMapper.writeValueAsString(geom);

            LatLngCenter center = calculateCenterFromGeometry(geom);
            if (center == null) {
                skipped++;
                skippedNoCenter++;
                continue;
            }

            var existingOpt = areaV3Repository.findByPresentSn(presentSn);

            AreaV3 entity = existingOpt.orElseGet(() -> AreaV3.builder()
                    .presentSn(presentSn)
                    .build());

            entity.setName(name);
            entity.setSigunguCd(sigunguCd);
            entity.setWtnncSn(wtnncSn);
            entity.setNtfcSn(ntfcSn);
            entity.setPolygonGeojson(geomJson);
            entity.setCenterLat(center.lat());
            entity.setCenterLng(center.lng());

            areaV3Repository.save(entity);

            if (existingOpt.isPresent()) {
                updated++;
            } else {
                inserted++;
            }
        }

        return "DONE - inserted: " + inserted
                + ", updated: " + updated
                + ", skipped: " + skipped
                + " (null_present_sn=" + skippedNull
                + ", nulluq_present_sn=" + skippedNullUq
                + ", no_sigungu_cd=" + skippedNoSigungu
                + ", no_center=" + skippedNoCenter
                + ")";
    }

    private LatLngCenter calculateCenterFromGeometry(JsonNode geom) {
        if (geom == null || geom.isMissingNode()) {
            return null;
        }

        String type = geom.path("type").asText();
        JsonNode coordinates = geom.path("coordinates");

        if (coordinates.isMissingNode() || coordinates.isEmpty()) {
            return null;
        }

        double minLat = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        double minLng = Double.POSITIVE_INFINITY;
        double maxLng = Double.NEGATIVE_INFINITY;

        if ("Polygon".equals(type)) {
            JsonNode outerRing = coordinates.get(0);
            MinMax minMax = extractMinMaxFromRing(outerRing);
            if (minMax == null) {
                return null;
            }

            minLat = minMax.minLat();
            maxLat = minMax.maxLat();
            minLng = minMax.minLng();
            maxLng = minMax.maxLng();

        } else if ("MultiPolygon".equals(type)) {
            boolean found = false;

            for (JsonNode polygonNode : coordinates) {
                if (polygonNode == null || polygonNode.isEmpty()) {
                    continue;
                }

                JsonNode outerRing = polygonNode.get(0);
                MinMax minMax = extractMinMaxFromRing(outerRing);
                if (minMax == null) {
                    continue;
                }

                found = true;
                minLat = Math.min(minLat, minMax.minLat());
                maxLat = Math.max(maxLat, minMax.maxLat());
                minLng = Math.min(minLng, minMax.minLng());
                maxLng = Math.max(maxLng, minMax.maxLng());
            }

            if (!found) {
                return null;
            }
        } else {
            return null;
        }

        double centerLat = (minLat + maxLat) / 2.0;
        double centerLng = (minLng + maxLng) / 2.0;

        return new LatLngCenter(
                BigDecimal.valueOf(centerLat).setScale(7, RoundingMode.HALF_UP),
                BigDecimal.valueOf(centerLng).setScale(7, RoundingMode.HALF_UP)
        );
    }

    private MinMax extractMinMaxFromRing(JsonNode ring) {
        if (ring == null || ring.isEmpty()) {
            return null;
        }

        double minLat = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        double minLng = Double.POSITIVE_INFINITY;
        double maxLng = Double.NEGATIVE_INFINITY;

        boolean found = false;

        for (JsonNode point : ring) {
            if (point == null || point.size() < 2) {
                continue;
            }

            double lng = point.get(0).asDouble();
            double lat = point.get(1).asDouble();

            found = true;
            minLat = Math.min(minLat, lat);
            maxLat = Math.max(maxLat, lat);
            minLng = Math.min(minLng, lng);
            maxLng = Math.max(maxLng, lng);
        }

        if (!found) {
            return null;
        }

        return new MinMax(minLat, maxLat, minLng, maxLng);
    }

    private record LatLngCenter(BigDecimal lat, BigDecimal lng) {}
    private record MinMax(double minLat, double maxLat, double minLng, double maxLng) {}
}