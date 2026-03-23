package com.yusung.realestateapi.backend.area.infra;

import com.yusung.realestateapi.backend.area.domain.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    List<PostImage> findByPostPostId(Long postId);

    void deleteByPostPostId(Long postId);
}