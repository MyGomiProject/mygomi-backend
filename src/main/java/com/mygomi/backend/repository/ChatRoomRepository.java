package com.mygomi.backend.repository;

import com.mygomi.backend.domain.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 이미 존재하는 채팅방인지 확인 (게시글 ID와 구매자 ID로 조회)
    Optional<ChatRoom> findBySharePostIdAndBuyerId(Long sharePostId, Long buyerId);

    // 내 채팅방 목록 조회 (내가 구매자이거나 판매자인 경우 모두 포함, sharePost 포함해 N+1 방지)
    @Query("SELECT cr FROM ChatRoom cr JOIN FETCH cr.buyer JOIN FETCH cr.seller JOIN FETCH cr.sharePost WHERE cr.buyer.id = :userId OR cr.seller.id = :userId")
    List<ChatRoom> findAllByUserId(@Param("userId") Long userId);

    // 해당 게시글에 대해 현재 사용자가 참여한 채팅방 (유일)
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.sharePost.id = :sharePostId AND (cr.buyer.id = :userId OR cr.seller.id = :userId)")
    Optional<ChatRoom> findBySharePostIdAndUserId(@Param("sharePostId") Long sharePostId, @Param("userId") Long userId);

    /** roomId로 채팅방 조회 + 현재 사용자가 참여자인 경우만 반환 (buyer/seller/sharePost fetch로 403 원인 제거) */
    @Query("SELECT cr FROM ChatRoom cr JOIN FETCH cr.buyer JOIN FETCH cr.seller JOIN FETCH cr.sharePost WHERE cr.id = :roomId AND (cr.buyer.id = :userId OR cr.seller.id = :userId)")
    Optional<ChatRoom> findByIdAndParticipant(@Param("roomId") Long roomId, @Param("userId") Long userId);
}