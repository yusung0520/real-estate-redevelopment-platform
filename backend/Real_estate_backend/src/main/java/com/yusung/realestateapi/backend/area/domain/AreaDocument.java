package com.yusung.realestateapi.backend.area.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "area_documents")
public class AreaDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "area_id", nullable = false)
    private Long areaId;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "match_type", nullable = false, length = 30)
    private String matchType;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal confidence;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
