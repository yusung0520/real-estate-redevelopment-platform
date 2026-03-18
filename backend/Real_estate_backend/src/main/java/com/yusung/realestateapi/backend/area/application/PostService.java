package com.yusung.realestateapi.backend.area.application;

import com.yusung.realestateapi.backend.area.domain.Agent;
import com.yusung.realestateapi.backend.area.domain.Post;
import com.yusung.realestateapi.backend.area.domain.PostImage;
import com.yusung.realestateapi.backend.area.dto.PostDetailDto;
import com.yusung.realestateapi.backend.area.dto.PostImageDto;
import com.yusung.realestateapi.backend.area.dto.PostSummaryDto;
import com.yusung.realestateapi.backend.area.infra.AgentRepository;
import com.yusung.realestateapi.backend.area.infra.PostImageRepository;
import com.yusung.realestateapi.backend.area.infra.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final AgentRepository agentRepository;
    private final PostImageRepository postImageRepository;

    @Value("${file.upload-dir}")
    private String uploadPath;

    public Long createPost(
            Long agentId,
            String title,
            String content,
            String categoryName,
            String guName,
            List<MultipartFile> images
    ) throws IOException {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("중개사 정보를 찾을 수 없습니다."));

        Post post = Post.builder()
                .agent(agent)
                .areaId(null)
                .title(title)
                .content(content)
                .categoryName(categoryName)
                .guName(guName)
                .build();

        postRepository.save(post);

        if (images != null && !images.isEmpty()) {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            for (MultipartFile image : images) {
                if (image.isEmpty()) continue;

                String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
                File saveFile = new File(uploadPath, fileName);
                image.transferTo(saveFile);

                PostImage postImage = PostImage.builder()
                        .post(post)
                        .imageUrl("/uploads/" + fileName)
                        .build();

                postImageRepository.save(postImage);
            }
        }

        return post.getPostId();
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