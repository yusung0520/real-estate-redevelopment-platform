//package com.yusung.realestateapi.backend.pipeline.runner;
//
//import com.yusung.realestateapi.backend.document.crawler.OpenGovSanctionCrawler;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//@Profile("dev")
//@RequiredArgsConstructor
//public class OpenGovCrawlerRunner implements CommandLineRunner {
//
//    private final OpenGovSanctionCrawler crawler;
//
//    @Override
//    public void run(String... args) {
//        crawler.crawlByKeywords(
//                List.of("정비구역 지정 고시", "조합설립인가", "사업시행인가", "관리처분인가"),
//                2 // 키워드당 2페이지 정도만 먼저 테스트
//        );
//    }
//}
