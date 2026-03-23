package com.yusung.realestateapi.backend.area.infra;

import com.yusung.realestateapi.backend.area.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByCategoryNameOrderByCreatedAtDesc(String categoryName);

    List<Post> findByAgentAgentIdOrderByCreatedAtDesc(Long agentId);

    List<Post> findAllByOrderByCreatedAtDesc();
}