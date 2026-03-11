package com.yusung.realestateapi.backend.area.infra;

import com.yusung.realestateapi.backend.area.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // ✅ 카테고리별로 최신순 정렬해서 가져오는 기능을 넣었습니다.
    List<Post> findByCategoryNameOrderByCreatedAtDesc(String categoryName);

    // ✅ 특정 중개사가 쓴 글들만 최신순으로 가져오기
    List<Post> findByAgentAgentIdOrderByCreatedAtDesc(Long agentId);

    // ✅ 모든 글을 최신순으로 가져오기
    List<Post> findAllByOrderByCreatedAtDesc();
}