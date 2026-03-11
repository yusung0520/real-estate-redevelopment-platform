package com.yusung.realestateapi.backend.area.infra;

import com.yusung.realestateapi.backend.area.domain.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    // ✅ 특정 게시글(postId)에 속한 모든 이미지 리스트 가져오기
    List<PostImage> findByPostPostId(Long postId);
}