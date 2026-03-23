package com.yusung.realestateapi.backend.area.api;

import com.yusung.realestateapi.backend.area.application.PostService;
import com.yusung.realestateapi.backend.area.dto.PostWriteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/write")
    public ResponseEntity<?> writePost(@RequestBody PostWriteRequest request) {
        try {
            Long postId = postService.createPost(
                    request.getAgentId(),
                    request.getTitle(),
                    request.getContentHtml(),
                    request.getCategoryName(),
                    request.getGuName()
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

    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long postId,
            @RequestBody PostWriteRequest request
    ) {
        try {
            postService.updatePost(
                    postId,
                    request.getTitle(),
                    request.getContentHtml(),
                    request.getCategoryName(),
                    request.getGuName()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "게시글이 성공적으로 수정되었습니다.",
                    "postId", postId
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "게시글 수정 실패: " + e.getMessage()
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

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        try {
            postService.deletePost(postId);

            return ResponseEntity.ok(Map.of(
                    "message", "게시글이 성공적으로 삭제되었습니다.",
                    "postId", postId
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "게시글 삭제 실패: " + e.getMessage()
            ));
        }
    }
}