package com.mygomi.backend.repository;

import com.mygomi.backend.domain.share.SharePost;
import com.mygomi.backend.domain.share.ShareStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SharePostRepository extends JpaRepository<SharePost, Long> {
    
    // 기본 조회
    List<SharePost> findByUserId(Long userId);
    
    // 지역별 조회 (구 기준)
    Page<SharePost> findByWardAndStatusOrderByCreatedAtDesc(String ward, ShareStatus status, Pageable pageable);
    
    // 상태별 조회
    Page<SharePost> findByStatusOrderByCreatedAtDesc(ShareStatus status, Pageable pageable);
    
    /**
     * 반경 검색 (Haversine 공식 사용)
     * 지구 반지름: 6371km
     * 
     * @param lat 중심 위도
     * @param lng 중심 경도
     * @param radiusKm 반경 (km)
     * @return 반경 내 게시글 목록
     */
    @Query(value = "SELECT * FROM share_posts WHERE " +
           "status = 'OPEN' AND " +
           "(6371 * acos(cos(radians(:lat)) * cos(radians(lat)) * " +
           "cos(radians(lng) - radians(:lng)) + sin(radians(:lat)) * " +
           "sin(radians(lat)))) <= :radiusKm " +
           "ORDER BY created_at DESC",
           nativeQuery = true)
    List<SharePost> findNearbyPosts(
        @Param("lat") Double lat,
        @Param("lng") Double lng,
        @Param("radiusKm") Double radiusKm
    );
    
    /**
     * 반경 검색 + 카테고리 필터
     */
    @Query(value = "SELECT * FROM share_posts WHERE " +
           "status = 'OPEN' AND " +
           "category = :category AND " +
           "(6371 * acos(cos(radians(:lat)) * cos(radians(lat)) * " +
           "cos(radians(lng) - radians(:lng)) + sin(radians(:lat)) * " +
           "sin(radians(lat)))) <= :radiusKm " +
           "ORDER BY created_at DESC",
           nativeQuery = true)
    List<SharePost> findNearbyPostsByCategory(
        @Param("lat") Double lat,
        @Param("lng") Double lng,
        @Param("radiusKm") Double radiusKm,
        @Param("category") String category
    );
    
    /**
     * 거리 계산과 함께 조회 (정렬용)
     */
    @Query(value = "SELECT *, " +
           "(6371 * acos(cos(radians(:lat)) * cos(radians(lat)) * " +
           "cos(radians(lng) - radians(:lng)) + sin(radians(:lat)) * " +
           "sin(radians(lat)))) AS distance " +
           "FROM share_posts WHERE " +
           "status = 'OPEN' AND " +
           "(6371 * acos(cos(radians(:lat)) * cos(radians(lat)) * " +
           "cos(radians(lng) - radians(:lng)) + sin(radians(:lat)) * " +
           "sin(radians(lat)))) <= :radiusKm " +
           "ORDER BY distance ASC",
           nativeQuery = true)
    List<Object[]> findNearbyPostsWithDistance(
        @Param("lat") Double lat,
        @Param("lng") Double lng,
        @Param("radiusKm") Double radiusKm
    );
}
