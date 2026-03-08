package com.yusung.realestateapi.backend.integration.cleanup.crawler;

import java.util.List;
import java.util.Map;

/**
 * businessName 필드를 추가하여 컴파일 에러를 해결합니다.
 */
public record CleanupCafeMainResult(
        String inputCafeUrl,
        String normalizedCafeUrl,
        String areaName,      // 지번 주소 (예: 성수동1가...)
        String businessName,  // ✅ 추가: 조합/사업 명칭 (예: 성수제1구역...)
        String mainUrl,
        String executeUrlCafeId,
        String executeUrlCafeLd,

        // 다음 크롤링 단계에서 쓸 수 있게 쿠키도 같이 전달
        Map<String, String> cookies,

        // 디버깅 정보
        int htmlLength,
        String htmlHead,

        // 단계 파싱 결과
        String currentStage,
        List<String> stages
) {}