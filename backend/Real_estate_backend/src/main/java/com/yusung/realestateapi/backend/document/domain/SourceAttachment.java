package com.yusung.realestateapi.backend.document.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "source_attachments",
        uniqueConstraints = @UniqueConstraint(name = "uk_doc_file", columnNames = {"document_id", "file_url"})
)
public class SourceAttachment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(length = 500)
    private String fileName;

    @Column(name = "file_url", nullable = false, length = 700)
    private String fileUrl;

    @Column(length = 50)
    private String fileType; // pdf, hwp...

    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() { createdAt = LocalDateTime.now(); }
}
