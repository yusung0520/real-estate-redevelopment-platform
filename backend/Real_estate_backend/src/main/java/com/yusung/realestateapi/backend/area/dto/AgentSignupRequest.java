package com.yusung.realestateapi.backend.area.dto; // ✅ 기존 AreaDto와 같은 위치

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AgentSignupRequest {
    private String email;
    private String password;
    private String name;
    private String officeName;
    private String phone;
    private String kakaoOpenchatUrl;
}