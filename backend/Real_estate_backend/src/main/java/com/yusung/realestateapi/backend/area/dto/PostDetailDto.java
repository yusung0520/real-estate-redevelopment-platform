package com.yusung.realestateapi.backend.area.dto;

import com.yusung.realestateapi.backend.area.domain.Post;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostDetailDto {

    private final Long postId;
    private final Long agentId;
    private final String agentName;
    private final String title;
    private final String content;
    private final String categoryName;
    private final String guName;
    private final LocalDateTime createdAt;
    private final List<PostImageDto> images;

    public PostDetailDto(Post post, List<PostImageDto> images) {
        this.postId = post.getPostId();
        this.agentId = post.getAgent().getAgentId();
        this.agentName = post.getAgent().getName();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.categoryName = post.getCategoryName();
        this.guName = post.getGuName();
        this.createdAt = post.getCreatedAt();
        this.images = images;
    }
}