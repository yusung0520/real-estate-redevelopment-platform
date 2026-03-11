package com.yusung.realestateapi.backend.area.api;

import com.yusung.realestateapi.backend.area.application.PostService;
import com.yusung.realestateapi.backend.area.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * ✅ 블로그 게시글 작성 (텍스트 + 이미지)
     * 이미지는 MultipartFile을 통해 파일 형태로 전달받습니다.
     */
    @PostMapping(value = "/write", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> writePost(
            @RequestPart("title") String title,
            @RequestPart("content") String content,
            @RequestPart("categoryName") String categoryName,
            @RequestPart("agentId") String agentId, // 로그인된 중개사 ID
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        try {
            // 1. 서비스 호출하여 데이터 저장 및 파일 처리
            Long postId = postService.createPost(
                    Long.parseLong(agentId),
                    title,
                    content,
                    categoryName,
                    images
            );

            return ResponseEntity.ok(Map.of(
                    "message", "게시글이 성공적으로 등록되었습니다.",
                    "postId", postId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "게시글 등록 실패: " + e.getMessage()
            ));
        }
    }

    /**
     * ✅ 최신 게시글 목록 조회
     */
    @GetMapping("/list")
    public ResponseEntity<?> getPostList() {
        return ResponseEntity.ok(postService.getAllPosts());
    }
}