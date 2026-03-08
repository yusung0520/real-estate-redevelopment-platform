package com.yusung.realestateapi.backend.document.infra;

import com.yusung.realestateapi.backend.document.domain.SourceAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceAttachmentRepository extends JpaRepository<SourceAttachment, Long> {
    boolean existsByDocumentIdAndFileUrl(Long documentId, String fileUrl);
}
