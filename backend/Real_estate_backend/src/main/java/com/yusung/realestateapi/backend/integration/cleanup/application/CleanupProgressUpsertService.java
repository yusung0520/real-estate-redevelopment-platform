package com.yusung.realestateapi.backend.integration.cleanup.application;

import com.yusung.realestateapi.backend.integration.cleanup.domain.CleanupProgress;
import com.yusung.realestateapi.backend.integration.cleanup.infra.CleanupProgressRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CleanupProgressUpsertService {

    private final CleanupProgressRepository repo;

    public CleanupProgressUpsertService(CleanupProgressRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void upsertAll(Long areaId, List<Item> items, String sourceUrl, Integer currentStepNoOrNull) {
        // 1) 이번 수집에서 current를 확정할 수 있으면 먼저 전체 current false로 밀어버림
        if (currentStepNoOrNull != null) {
            repo.clearCurrent(areaId);
        }

        for (Item it : items) {
            CleanupProgress p = repo.findByAreaIdAndStepNo(areaId, it.stepNo)
                    .orElseGet(CleanupProgress::new);

            p.setAreaId(areaId);
            p.setStepNo(it.stepNo);
            p.setStepName(it.stepName);
            p.setEventDate(it.eventDate);
            p.setSourceUrl(sourceUrl);

            // 2) current는 "확정된 currentStepNo" 기준으로만 true
            if (currentStepNoOrNull != null) {
                p.setCurrent(Objects.equals(it.stepNo, currentStepNoOrNull));
            } else {
                // current 못 잡는 날은 기존값 유지(혹은 false로 통일하고 싶으면 여기서 false)
                // p.setCurrent(false);
            }

            repo.save(p);
        }
    }

    public record Item(Integer stepNo, String stepName, LocalDate eventDate) {}

    // 날짜 파싱 유틸(필요하면 여기 쓰면 됨)
    public static LocalDate parseDateOrNull(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isBlank()) return null;

        s = s.replace(".", "-").replace("/", "-");
        // "2023-05-10" 형태만 남기기(필요하면 더 강화 가능)
        if (!s.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) return null;

        String[] parts = s.split("-");
        int y = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        int d = Integer.parseInt(parts[2]);
        return LocalDate.of(y, m, d);
    }
}
