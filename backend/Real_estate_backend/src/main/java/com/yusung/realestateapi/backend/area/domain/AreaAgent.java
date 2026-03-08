package com.yusung.realestateapi.backend.area.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "area_agents",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_area_agents",
                        columnNames = {"area_id", "agent_id"}
                )
        }
)
@Getter
@NoArgsConstructor
public class AreaAgent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "area_agent_id")
    private Long areaAgentId;

    @Column(name = "area_id", nullable = false)
    private Long areaId;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    // tinyint(0/1) → Integer로 받는 게 안전
    @Column(name = "is_featured")
    private Integer isFeatured;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
