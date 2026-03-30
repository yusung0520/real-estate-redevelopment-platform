package com.yusung.realestateapi.backend.area.dto;

import com.yusung.realestateapi.backend.area.domain.Agent;
import com.yusung.realestateapi.backend.area.domain.Post;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostDetailDto {

    private final Long postId;
    private final Long agentId;
    private final String agentName;
    private final String agentPhone;
    private final String kakaoOpenchatUrl;
    private final String title;
    private final String content;
    private final String categoryName;
    private final String sigunguCd;
    private final LocalDateTime createdAt;
    private final List<PostImageDto> images;

    public PostDetailDto(Post post, List<PostImageDto> images) {
        Agent agent = post.getAgent();

        this.postId = post.getPostId();
        this.agentId = agent != null ? agent.getAgentId() : null;
        this.agentName = agent != null ? agent.getName() : null;
        this.agentPhone = agent != null ? agent.getPhone() : null;
        this.kakaoOpenchatUrl = agent != null ? agent.getKakaoOpenchatUrl() : null;
        this.title = post.getTitle();
        this.content = post.getContent();
        this.categoryName = post.getCategoryName();
        this.sigunguCd = post.getSigunguCd();
        this.createdAt = post.getCreatedAt();
        this.images = images;
    }
}