package com.mygomi.backend.repository;

import com.mygomi.backend.domain.share.SharePostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SharePostImageRepository extends JpaRepository<SharePostImage, Long> {
    List<SharePostImage> findBySharePostIdOrderByDisplayOrderAsc(Long sharePostId);
    
    void deleteBySharePostId(Long sharePostId);
}
