package com.yusung.realestateapi.backend.area.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @Column(name = "area_id")
    private Long areaId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "sigungu_cd", length = 10)
    private String sigunguCd;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Post(Agent agent, Long areaId, String title, String content, String categoryName, String sigunguCd) {
        this.agent = agent;
        this.areaId = areaId;
        this.title = title;
        this.content = content;
        this.categoryName = categoryName;
        this.sigunguCd = sigunguCd;
    }

    public void update(String title, String content, String categoryName, String sigunguCd) {
        this.title = title;
        this.content = content;
        this.categoryName = categoryName;
        this.sigunguCd = sigunguCd;
    }
}