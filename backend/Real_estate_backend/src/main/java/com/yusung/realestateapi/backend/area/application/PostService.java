package com.yusung.realestateapi.backend.area.application;

import com.yusung.realestateapi.backend.area.domain.Agent;
import com.yusung.realestateapi.backend.area.domain.Post;
import com.yusung.realestateapi.backend.area.domain.PostImage;
import com.yusung.realestateapi.backend.area.infra.AgentRepository;
import com.yusung.realestateapi.backend.area.infra.PostImageRepository;
import com.yusung.realestateapi.backend.area.infra.PostRepository;
import lombok.RequiredArgsConstructor;
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

    private final String uploadPath = "C:/realestate_uploads/"; // 💡 사진이 저장될 실제 폴더 경로

    public Long createPost(Long agentId, String title, String content, String categoryName, List<MultipartFile> images) throws IOException {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("중개사 정보를 찾을 수 없습니다."));

        // 1. 게시글 저장
        Post post = Post.builder()
                .agent(agent)
                .title(title)
                .content(content)
                .categoryName(categoryName)
                .build();

        postRepository.save(post);

        // 2. 이미지 파일 처리
        if (images != null) {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) uploadDir.mkdirs(); // 폴더 없으면 생성

            for (MultipartFile image : images) {
                String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
                File saveFile = new File(uploadPath + fileName);
                image.transferTo(saveFile); // 실제 파일 저장

                // DB에 파일 경로 저장
                PostImage postImage = PostImage.builder()
                        .post(post)
                        .imageUrl("/uploads/" + fileName)
                        .build();
                postImageRepository.save(postImage);
            }
        }
        return post.getPostId();
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }
}