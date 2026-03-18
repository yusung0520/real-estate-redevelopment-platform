package com.yusung.realestateapi.backend.area.api;

import com.yusung.realestateapi.backend.area.application.PostService;
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

    @PostMapping(value = "/write", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> writePost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("categoryName") String categoryName,
            @RequestParam("agentId") Long agentId,
            @RequestParam("guName") String guName,
            @RequestParam(value = "images", required = false) List<MultipartFile> images
    ) {
        try {
            Long postId = postService.createPost(
                    agentId,
                    title,
                    content,
                    categoryName,
                    guName,
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

    @GetMapping("/list")
    public ResponseEntity<?> getPostList() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyPosts(@RequestParam("agentId") Long agentId) {
        return ResponseEntity.ok(postService.getPostsByAgent(agentId));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostDetail(@PathVariable Long postId) {
        try {
            return ResponseEntity.ok(postService.getPostDetail(postId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }
}