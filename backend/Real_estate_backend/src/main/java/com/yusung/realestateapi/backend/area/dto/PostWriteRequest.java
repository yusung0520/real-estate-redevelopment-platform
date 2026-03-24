package com.yusung.realestateapi.backend.area.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostWriteRequest {
    private Long agentId;
    private String title;
    private String sigunguCd;
    private String categoryName;
    private String contentHtml;
}