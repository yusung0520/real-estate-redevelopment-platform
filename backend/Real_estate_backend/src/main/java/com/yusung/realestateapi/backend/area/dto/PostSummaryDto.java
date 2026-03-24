package com.yusung.realestateapi.backend.area.dto;

import com.yusung.realestateapi.backend.area.domain.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostSummaryDto {

    private final Long postId;
    private final Long agentId;
    private final String title;
    private final String categoryName;
    private final String sigunguCd;
    private final LocalDateTime createdAt;

    public PostSummaryDto(Post post) {
        this.postId = post.getPostId();
        this.agentId = post.getAgent().getAgentId();
        this.title = post.getTitle();
        this.categoryName = post.getCategoryName();
        this.sigunguCd = post.getSigunguCd();
        this.createdAt = post.getCreatedAt();
    }
}