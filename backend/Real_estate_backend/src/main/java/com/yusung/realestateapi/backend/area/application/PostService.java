package com.yusung.realestateapi.backend.area.application;

import com.yusung.realestateapi.backend.area.domain.Agent;
import com.yusung.realestateapi.backend.area.domain.Post;
import com.yusung.realestateapi.backend.area.dto.PostDetailDto;
import com.yusung.realestateapi.backend.area.dto.PostImageDto;
import com.yusung.realestateapi.backend.area.dto.PostSummaryDto;
import com.yusung.realestateapi.backend.area.infra.AgentRepository;
import com.yusung.realestateapi.backend.area.infra.PostImageRepository;
import com.yusung.realestateapi.backend.area.infra.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final AgentRepository agentRepository;
    private final PostImageRepository postImageRepository;

    public Long createPost(
            Long agentId,
            String title,
            String contentHtml,
            String categoryName,
            String guName
    ) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("중개사 정보를 찾을 수 없습니다."));

        Post post = Post.builder()
                .agent(agent)
                .areaId(null)
                .title(title)
                .content(contentHtml)
                .categoryName(categoryName)
                .guName(guName)
                .build();

        Post savedPost = postRepository.save(post);
        return savedPost.getPostId();
    }

    public void updatePost(
            Long postId,
            String title,
            String contentHtml,
            String categoryName,
            String guName
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 게시글을 찾을 수 없습니다."));

        post.update(title, contentHtml, categoryName, guName);
    }

    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 게시글을 찾을 수 없습니다."));

        postImageRepository.deleteByPostPostId(postId);
        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public List<PostSummaryDto> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(PostSummaryDto::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PostSummaryDto> getPostsByAgent(Long agentId) {
        return postRepository.findByAgentAgentIdOrderByCreatedAtDesc(agentId)
                .stream()
                .map(PostSummaryDto::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public PostDetailDto getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        List<PostImageDto> imageDtos = postImageRepository.findByPostPostId(postId)
                .stream()
                .map(PostImageDto::new)
                .toList();

        return new PostDetailDto(post, imageDtos);
    }
}