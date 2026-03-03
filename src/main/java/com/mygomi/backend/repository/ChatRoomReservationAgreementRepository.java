package com.mygomi.backend.repository;

import com.mygomi.backend.domain.chat.ChatRoomReservationAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomReservationAgreementRepository extends JpaRepository<ChatRoomReservationAgreement, Long> {

    List<ChatRoomReservationAgreement> findByChatRoomId(Long chatRoomId);

    /** 방 단위 예약 상태 조회용: User 함께 로드해 동일 응답 보장 */
    @Query("SELECT a FROM ChatRoomReservationAgreement a JOIN FETCH a.user WHERE a.chatRoom.id = :chatRoomId")
    List<ChatRoomReservationAgreement> findByChatRoomIdWithUser(@Param("chatRoomId") Long chatRoomId);

    Optional<ChatRoomReservationAgreement> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    /** 방 단위: 해당 참여자가 해당 방에서 동의했는지 — 같은 roomId면 누가 조회해도 동일 판단 */
    @Query("SELECT (COUNT(a) > 0) FROM ChatRoomReservationAgreement a WHERE a.chatRoom.id = :chatRoomId AND a.user.id = :userId")
    boolean existsByChatRoomIdAndUserId(@Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);

    /** 방 단위 동의 인원 수 — 같은 roomId면 글 작성자/나눔 신청자 동일한 bothAgreed 계산용 */
    @Query("SELECT COUNT(a) FROM ChatRoomReservationAgreement a WHERE a.chatRoom.id = :chatRoomId")
    long countByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}
