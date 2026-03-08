package com.yusung.realestateapi.backend.area.infra;

import com.yusung.realestateapi.backend.area.domain.AreaDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AreaDocumentRepository extends JpaRepository<AreaDocument, Long> {

    boolean existsByAreaIdAndDocumentId(Long areaId, Long documentId);

    @Query(value = "SELECT * FROM area_documents ORDER BY id DESC LIMIT :limit", nativeQuery = true)
    List<AreaDocument> findTopNOrderByIdDesc(@Param("limit") int limit);
}

