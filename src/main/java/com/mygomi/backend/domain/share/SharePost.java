package com.mygomi.backend.domain.share;

import com.mygomi.backend.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "share_posts")
public class SharePost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShareCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShareStatus status = ShareStatus.OPEN;

    // 위치 정보
    private String prefecture;
    private String ward;
    private String town;
    private String address;
    
    @Column(nullable = false)
    private Double lat;
    
    @Column(nullable = false)
    private Double lng;

    @OneToMany(mappedBy = "sharePost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SharePostImage> images = new ArrayList<>();

    @Builder
    public SharePost(Long userId, String title, String description, ShareCategory category,
                     String prefecture, String ward, String town, String address,
                     Double lat, Double lng) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.prefecture = prefecture;
        this.ward = ward;
        this.town = town;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.viewCount = 0;
    }

    // ========================================
    // 비즈니스 메서드
    // ========================================

    /**
     * 게시글 수정
     */
    public void update(String title, String description, ShareCategory category) {
        this.title = title;
        this.description = description;
        this.category = category;
    }

    /**
     * 상태 변경
     */
    public void updateStatus(ShareStatus status) {
        this.status = status;
    }

    /**
     * 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * Soft Delete
     */
    public void softDelete() {
        this.status = ShareStatus.DELETED;
    }

    /**
     * 이미지 추가
     */
    public void addImage(SharePostImage image) {
        this.images.add(image);
        image.assignPost(this);
    }

    /**
     * 작성자 확인
     */
    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }
}
