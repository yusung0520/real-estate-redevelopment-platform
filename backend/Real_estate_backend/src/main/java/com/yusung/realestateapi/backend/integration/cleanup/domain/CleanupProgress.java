package com.yusung.realestateapi.backend.integration.cleanup.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "cleanup_progresses",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cleanup_progress_area_step",
                columnNames = {"area_id", "step_no"}
        )
)
@Getter @Setter
@Builder // ✅ 추가: CleanupProgress.builder() 사용 가능
@NoArgsConstructor // ✅ JPA용
@AllArgsConstructor // ✅ Builder용
public class CleanupProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "area_id", nullable = false)
    private Long areaId;

    @Column(name = "step_no", nullable = false)
    private Integer stepNo;

    @Column(name = "step_name", nullable = false, length = 100)
    private String stepName;

    @Column(name = "event_date")
    private LocalDate eventDate;

    // ✅ 필드명이 'current'이면 Lombok은 setCurrent()와 isCurrent()를 생성합니다.
    @Column(name = "is_current", nullable = false)
    private boolean current;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}