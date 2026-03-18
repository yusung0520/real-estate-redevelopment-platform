package com.yusung.realestateapi.backend.area.dto;

import com.yusung.realestateapi.backend.area.domain.PostImage;
import lombok.Getter;

@Getter
public class PostImageDto {

    private final Long imageId;
    private final String imageUrl;

    public PostImageDto(PostImage postImage) {
        this.imageId = postImage.getImageId();
        this.imageUrl = postImage.getImageUrl();
    }
}