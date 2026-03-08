package com.yusung.realestateapi.backend.area.infra;

import com.yusung.realestateapi.backend.area.domain.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, Long> {
}
