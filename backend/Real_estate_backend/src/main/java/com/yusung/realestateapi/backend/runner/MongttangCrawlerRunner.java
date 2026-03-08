package com.yusung.realestateapi.backend.runner;

import com.yusung.realestateapi.backend.document.crawler.MongttangNoticeCrawler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class MongttangCrawlerRunner implements CommandLineRunner {

    private final MongttangNoticeCrawler crawler;

    public MongttangCrawlerRunner(MongttangNoticeCrawler crawler) {
        this.crawler = crawler;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Mongttang crawler start");

        // ⚠️ 처음엔 절대 크게 하지 말 것
        //crawler.crawlAllPages(31, 200);

        System.out.println("✅ Mongttang crawler end");
    }
}
