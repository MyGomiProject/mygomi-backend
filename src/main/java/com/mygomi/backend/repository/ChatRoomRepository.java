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

    // 내 채팅방 목록 조회 (내가 구매자이거나 판매자인 경우 모두 포함)
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.buyer.id = :userId OR cr.seller.id = :userId")
    List<ChatRoom> findAllByUserId(@Param("userId") Long userId);
}