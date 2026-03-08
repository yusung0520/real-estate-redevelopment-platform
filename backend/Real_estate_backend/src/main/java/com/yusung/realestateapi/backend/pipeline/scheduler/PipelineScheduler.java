package com.yusung.realestateapi.backend.pipeline.scheduler;

import com.yusung.realestateapi.backend.document.crawler.MongttangNoticeCrawler;
import com.yusung.realestateapi.backend.pipeline.job.AreaDocumentMatchService;
import com.yusung.realestateapi.backend.pipeline.job.AreaEventBuildService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class PipelineScheduler {

    private final MongttangNoticeCrawler crawler;
    private final AreaDocumentMatchService matchService;
    private final AreaEventBuildService eventBuildService;

    // ✅ 겹침 방지(단일 서버용 간단 락)
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Value("${pipeline.crawl.from:1}")
    private int crawlFrom;

    @Value("${pipeline.crawl.to:10}")
    private int crawlTo;

    @Value("${pipeline.match.limit:500}")
    private int matchLimit;

    @Value("${pipeline.match.onlyActive:true}")
    private boolean onlyActive;

    @Value("${pipeline.events.limit:800}")
    private int eventLimit;

    // ✅ prod 기본: 하루 2회
    @Scheduled(cron = "${pipeline.cron:0 10 6,18 * * *}")
    public void runPipeline() {
        if (!running.compareAndSet(false, true)) {
            log.warn("[PIPELINE] skipped (already running)");
            return;
        }

        long t0 = System.currentTimeMillis();
        log.info("[PIPELINE] start");

        try {
            crawler.crawlAllPages(crawlFrom, crawlTo);
            log.info("[PIPELINE] crawl done. pages={}~{}", crawlFrom, crawlTo);

            int matched = matchService.matchRecentDocuments(matchLimit, onlyActive);
            log.info("[PIPELINE] match done. inserted={}", matched);

            int events = eventBuildService.buildEventsFromMatchedDocs(eventLimit);
            log.info("[PIPELINE] events done. inserted={}", events);

            log.info("[PIPELINE] end. tookMs={}", (System.currentTimeMillis() - t0));
        } catch (Exception e) {
            log.error("[PIPELINE] FAILED", e);
        } finally {
            running.set(false);
        }
    }
}
