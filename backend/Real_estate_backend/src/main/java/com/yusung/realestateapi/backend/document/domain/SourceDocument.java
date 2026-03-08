package com.yusung.realestateapi.backend.document.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "source_documents")
public class SourceDocument {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(length = 100)
    private String org;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "published_date")
    private LocalDate publishedDate;

    @Column(name = "detail_url", nullable = false, length = 1000)
    private String detailUrl;

    @Lob
    @Column(name = "content_text")
    private String contentText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

