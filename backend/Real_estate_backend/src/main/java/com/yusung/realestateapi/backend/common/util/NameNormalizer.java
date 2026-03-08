package com.yusung.realestateapi.backend.common.util;

import java.text.Normalizer;

public class NameNormalizer {

    private NameNormalizer() {}

    public static String norm(String s) {
        if (s == null) return "";

        String x = s.trim();

        // 1) 괄호/가칭 제거
        x = x.replace("(가칭)", "");
        x = x.replaceAll("\\((가칭|예정|추정|임시)\\)", ""); // 혹시 있을 경우

        // 2) 흔한 꼬리표 제거(필요하면 계속 추가)
        x = x.replace("재건축정비사업", "")
                .replace("재개발정비사업", "")
                .replace("도시환경정비사업", "")
                .replace("가로주택정비사업", "")
                .replace("소규모재건축", "")
                .replace("소규모", "")
                .replace("정비사업", "")
                .replace("조합", "");

        // 3) “제3구역” → “3구역”
        x = x.replaceAll("제\\s*(\\d+)\\s*구역", "$1구역");

        // 4) 공백/특수문자 제거(한글/영문/숫자만 남김)
        x = x.replaceAll("\\s+", "");
        x = x.replaceAll("[^0-9a-zA-Z가-힣]", "");

        // 5) 유니코드 정규화(혹시 모를 문자 형태 차이 방지)
        x = Normalizer.normalize(x, Normalizer.Form.NFC);

        return x.toLowerCase();
    }
}