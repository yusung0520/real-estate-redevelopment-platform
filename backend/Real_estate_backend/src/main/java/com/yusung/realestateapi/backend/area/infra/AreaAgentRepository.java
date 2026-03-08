package com.yusung.realestateapi.backend.area.infra;

import com.yusung.realestateapi.backend.area.domain.AreaAgent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AreaAgentRepository extends JpaRepository<AreaAgent, Long> {

    List<AreaAgent> findByAreaIdOrderByIsFeaturedDescAreaAgentIdAsc(Long areaId);
}
