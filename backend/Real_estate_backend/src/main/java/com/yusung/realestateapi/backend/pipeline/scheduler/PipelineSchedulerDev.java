package com.yusung.realestateapi.backend.pipeline.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class PipelineSchedulerDev {

    private final PipelineScheduler scheduler;

    @Value("${pipeline.dev.enabled:false}") // ✅ 기본 false로
    private boolean enabled;

    @Scheduled(fixedDelayString = "${pipeline.dev.fixedDelayMs:60000}")
    public void run() {
        if (!enabled) return; // ✅ 꺼져있으면 아무것도 안함
        log.info("[PIPELINE-DEV] tick");
        scheduler.runPipeline();
    }
}
