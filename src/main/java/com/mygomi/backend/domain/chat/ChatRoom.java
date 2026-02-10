package com.mygomi.backend.domain.chat;

import com.mygomi.backend.domain.common.BaseTimeEntity;
import com.mygomi.backend.domain.share.SharePost;
import com.mygomi.backend.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 나눔 게시글 관련 채팅인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "share_post_id")
    private SharePost sharePost;

    // 구매자 (채팅을 건 사람)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    private User buyer;

    // 판매자 (게시글 주인) - 조회의 편의성을 위해 따로 저장
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;

    @Builder
    public ChatRoom(SharePost sharePost, User buyer, User seller) {
        this.sharePost = sharePost;
        this.buyer = buyer;
        this.seller = seller;
    }
}