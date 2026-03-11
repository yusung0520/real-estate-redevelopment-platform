package com.yusung.realestateapi.backend.area.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "agents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agent_id")
    private Long agentId;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "office_name", length = 100)
    private String officeName;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "kakao_openchat_url", length = 255)
    private String kakaoOpenchatUrl;

    @Column(name = "role", length = 20)
    private String role;

    @CreationTimestamp // ✅ 저장 시점의 시간이 자동으로 들어갑니다 (수동 저장 필요 없음)
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Agent(String email, String password, String name, String officeName, String phone, String kakaoOpenchatUrl) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.officeName = officeName;
        this.phone = phone;
        this.kakaoOpenchatUrl = kakaoOpenchatUrl;
        this.role = "ROLE_AGENT";
    }
}