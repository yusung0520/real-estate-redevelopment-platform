package com.yusung.realestateapi.backend.document.infra;

import com.yusung.realestateapi.backend.document.domain.SourceDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface SourceDocumentRepository extends JpaRepository<SourceDocument, Long> {

    Optional<SourceDocument> findBySourceAndDetailUrl(String source, String detailUrl);
    boolean existsBySourceAndDetailUrl(String source, String detailUrl);

    // ✅ contentText가 비어있는 것부터 채우기
    List<SourceDocument> findTop50BySourceAndContentTextIsNullOrderByIdDesc(String source);

    @Query(value = "SELECT * FROM source_documents ORDER BY published_date DESC, id DESC LIMIT :limit", nativeQuery = true)
    List<SourceDocument> findTopNOrderByPublishedDateDesc(@Param("limit") int limit);
}
