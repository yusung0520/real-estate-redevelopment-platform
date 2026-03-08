package com.yusung.realestateapi.backend.area.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "area_events")
public class AreaEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "area_id", nullable = false)
    private Long areaId;

    @Column(name = "document_id")
    private Long documentId;

    // ✅ 테이블에 존재 + NOT NULL
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    // ✅ 테이블에 존재 + NOT NULL
    @Column(name = "event_label", nullable = false, length = 100)
    private String eventLabel;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "detected_text", length = 255)
    private String detectedText;

    // ✅ 테이블에 존재 + NOT NULL + 기본값 0.50
    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal confidence;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // ✅ 너가 추가한 컬럼
    @Column(name = "stage_key", length = 30)
    private String stageKey;

    @Column(name = "stage_label", length = 50)
    private String stageLabel;

    // ✅ STEP 1-1: 공식 이벤트 여부 (true인 것만 서비스에 노출할 예정)
    @Column(name = "is_official", nullable = false)
    private Boolean isOfficial = false;

}
