package com.yusung.realestateapi.backend.integration.cleanup.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "cleanup_cafes")
@Getter @Setter
@NoArgsConstructor
public class CleanupCafe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "area_id", unique = true)
    private Long areaId;

    @Column(name = "cafe_url", nullable = false, length = 50)
    private String cafeUrl;

    @Column(name = "cafe_id", length = 50)
    private String cafeId;

    @Column(name = "bsns_pk", length = 50)
    private String bsnsPk;

    // ✅ DB에 있는 signgu_code 추가
    @Column(name = "signgu_code")
    private Integer signgu_code;

    @Column(name = "area_name", length = 200)
    private String areaName;

    // ✅ DB에 있는 area_name_norm 추가
    @Column(name = "area_name_norm")
    private String areaNameNorm;

    @Column(name = "main_url", length = 500)
    private String mainUrl;

    @Column(name = "frame_url", length = 500)
    private String frameUrl;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    @Column(name = "stage_name")
    private String stageName;

    // ✅ 비즈니스 네임 (정상 매핑 확인)
    @Column(name = "business_name")
    private String businessName;

    @Column(name = "official_biz_name")
    private String officialBizName;
}