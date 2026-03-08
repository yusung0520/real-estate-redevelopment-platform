package com.yusung.realestateapi.backend.area.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "import_log")
@Getter @Setter
public class ImportLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="job_name", nullable = false)
    private String jobName;

    @Column(name="source_version")
    private String sourceVersion;

    @Column(name="started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name="finished_at")
    private LocalDateTime finishedAt;

    @Column(name="status", nullable = false)
    private String status; // RUNNING / SUCCESS / FAILED

    @Column(name="total_count")
    private int totalCount;

    @Column(name="inserted_count")
    private int insertedCount;

    @Column(name="updated_count")
    private int updatedCount;

    @Column(name="skipped_count")
    private int skippedCount;

    @Lob
    @Column(name="error_message")
    private String errorMessage;
}
