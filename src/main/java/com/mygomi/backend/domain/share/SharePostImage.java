package com.mygomi.backend.domain.share;

import com.mygomi.backend.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "share_post_images")
public class SharePostImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "share_post_id", nullable = false)
    private SharePost sharePost;

    @Column(nullable = false, length = 500)
    private String imageUrl;  // S3/Cloudinary URL

    @Column(name = "display_order")
    private Integer displayOrder = 0;  // 이미지 순서 (0부터 시작, 0이 썸네일)

    @Builder
    public SharePostImage(SharePost sharePost, String imageUrl, Integer displayOrder) {
        this.sharePost = sharePost;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }

    // 연관관계 편의 메서드 (SharePost에서 호출)
    protected void assignPost(SharePost post) {
        this.sharePost = post;
    }

    public void updateOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isThumbnail() {
        return this.displayOrder == 0;
    }
}
