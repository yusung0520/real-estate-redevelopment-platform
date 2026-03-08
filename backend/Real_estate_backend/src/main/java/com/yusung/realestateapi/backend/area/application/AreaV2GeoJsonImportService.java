package com.yusung.realestateapi.backend.area.application;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AreaV2GeoJsonImportService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper om = new ObjectMapper();

    public ImportResult importFromPath(String filePathOrClasspath, String sourceVersion, boolean dryRun) throws Exception {
        InputStream is = openAsStream(filePathOrClasspath);
        return importStream(is, filePathOrClasspath, sourceVersion, dryRun);
    }

    private InputStream openAsStream(String filePathOrClasspath) throws Exception {
        // resources/data/... 같은 classpath 경로 지원
        // 예) "data/seoul_redevelopment_clean_3305_4326.geojson"
        if (!filePathOrClasspath.contains(":") && !filePathOrClasspath.startsWith("\\") && !filePathOrClasspath.startsWith("/")) {
            ClassPathResource r = new ClassPathResource(filePathOrClasspath);
            if (r.exists()) return r.getInputStream();
        }
        // 그 외는 로컬 파일 경로로 취급
        Path p = Path.of(filePathOrClasspath);
        if (!Files.exists(p)) throw new IllegalArgumentException("파일이 존재하지 않습니다: " + filePathOrClasspath);
        return Files.newInputStream(p);
    }

    private ImportResult importStream(InputStream is, String pathLabel, String sourceVersion, boolean dryRun) throws Exception {
        final String upsertSql = """
            INSERT INTO areas_v2 (present_sn, name, polygon_geojson, source_version)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
              name = VALUES(name),
              polygon_geojson = VALUES(polygon_geojson),
              source_version = VALUES(source_version)
            """;

        int totalRead = 0;
        int skippedNoPresentSn = 0;

        List<Object[]> batch = new ArrayList<>(1000);
        JsonFactory factory = new JsonFactory();

        try (is; JsonParser p = factory.createParser(is)) {

            if (p.nextToken() != JsonToken.START_OBJECT) {
                throw new IllegalArgumentException("GeoJSON 최상위가 JSON Object가 아닙니다.");
            }

            while (p.nextToken() != JsonToken.END_OBJECT) {
                String field = p.getCurrentName();
                if (field == null) continue;

                p.nextToken(); // value로 이동

                if ("features".equals(field) && p.currentToken() == JsonToken.START_ARRAY) {

                    while (p.nextToken() != JsonToken.END_ARRAY) {
                        Map<String, Object> feature = om.readValue(p, Map.class);

                        Map<String, Object> props = safeMap(feature.get("properties"));
                        Map<String, Object> geom = safeMap(feature.get("geometry"));

                        String presentSn = firstNonBlank(
                                asString(props.get("PRESENT_SN")),
                                asString(props.get("present_sn")),
                                asString(props.get("presentSn"))
                        );

                        if (presentSn == null || presentSn.isBlank()) {
                            skippedNoPresentSn++;
                            continue;
                        }

                        String name = firstNonBlank(
                                asString(props.get("NAME")),
                                asString(props.get("name")),
                                asString(props.get("AREA_NM")),
                                asString(props.get("area_nm"))
                        );

                        String geomJson = om.writeValueAsString(geom); // geometry만 저장

                        batch.add(new Object[]{presentSn, name, geomJson, sourceVersion});
                        totalRead++;

                        if (batch.size() >= 1000) {
                            flush(upsertSql, batch, dryRun);
                            batch.clear();
                        }
                    }
                } else {
                    p.skipChildren();
                }
            }
        }

        if (!batch.isEmpty()) flush(upsertSql, batch, dryRun);

        log.info("[areas_v2 import] path={} totalRead={} skipped(no present_sn)={} dryRun={} sourceVersion={}",
                pathLabel, totalRead, skippedNoPresentSn, dryRun, sourceVersion);

        return new ImportResult(pathLabel, sourceVersion, dryRun, totalRead, skippedNoPresentSn);
    }

    private void flush(String sql, List<Object[]> batch, boolean dryRun) {
        if (dryRun) {
            log.info("[areas_v2 import][dryRun] would upsert {} rows", batch.size());
            return;
        }
        jdbcTemplate.batchUpdate(sql, batch);
        log.info("[areas_v2 import] upserted {} rows", batch.size());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> safeMap(Object o) {
        if (o instanceof Map<?, ?> m) return (Map<String, Object>) m;
        return Collections.emptyMap();
    }

    private String asString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private String firstNonBlank(String... candidates) {
        if (candidates == null) return null;
        for (String c : candidates) {
            if (c != null && !c.isBlank()) return c;
        }
        return null;
    }

    public record ImportResult(
            String path,
            String sourceVersion,
            boolean dryRun,
            int totalRead,
            int skippedNoPresentSn
    ) {}
}