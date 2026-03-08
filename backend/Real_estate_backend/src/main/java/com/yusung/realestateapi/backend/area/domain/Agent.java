package com.yusung.realestateapi.backend.area.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "agents")
@Getter
@NoArgsConstructor
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agent_id")
    private Long agentId;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "office_name", length = 100)
    private String officeName;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "kakao_openchat_url", length = 255)
    private String kakaoOpenchatUrl;

    @Column(name = "intro", length = 255)
    private String intro;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
