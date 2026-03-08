package com.yusung.realestateapi.backend.area.infra;

import com.yusung.realestateapi.backend.area.domain.ImportLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportLogRepository extends JpaRepository<ImportLog, Long> {
}
