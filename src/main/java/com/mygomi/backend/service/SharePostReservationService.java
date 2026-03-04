package com.mygomi.backend.service;

import com.mygomi.backend.api.dto.response.ReservationStatusResponseDto;
import com.mygomi.backend.domain.chat.ChatRoom;
import com.mygomi.backend.domain.chat.ChatRoomReservationAgreement;
import com.mygomi.backend.domain.share.SharePost;
import com.mygomi.backend.domain.share.ShareStatus;
import com.mygomi.backend.domain.user.User;
import com.mygomi.backend.repository.ChatRoomRepository;
import com.mygomi.backend.repository.ChatRoomReservationAgreementRepository;
import com.mygomi.backend.repository.SharePostRepository;
import com.mygomi.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SharePostReservationService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomReservationAgreementRepository agreementRepository;
    private final SharePostRepository sharePostRepository;
    private final UserRepository userRepository;

    /**
     * 예약 상태 조회 (채팅방 진입 시 "내가/상대가 동의했는지", "게시글 상태" 반환).
     * 같은 roomId에 대해 게시글 주인·나눔 신청자 누가 조회해도 방 단위로 동일한 값 반환.
     * @param roomId 채팅방 ID (한 글에 여러 채팅방이 있을 수 있으므로 필수)
     */
    @Transactional(readOnly = true)
    public ReservationStatusResponseDto getStatus(Long postId, Long roomId, Long currentUserId) {
        ChatRoom room = findRoomByRoomIdOrThrow(roomId, postId, currentUserId);
        long roomIdForQuery = room.getId();

        // 1) 방(roomId) 단위 동의 수 — COUNT 쿼리로 항상 DB 기준, 같은 roomId면 글 작성자/나눔 신청자 동일 응답
        long agreedCount = agreementRepository.countByChatRoomId(roomIdForQuery);
        boolean bothAgreed = (agreedCount == 2);
        boolean otherAgreed = bothAgreed;

        // 2) 내 동의 여부 / 상대 동의 여부 (두 명 미만일 때만 상대 여부 필요)
        boolean myAgreed = agreementRepository.existsByChatRoomIdAndUserId(roomIdForQuery, currentUserId);
        if (!bothAgreed) {
            Long otherUserId = getOtherUserId(room, currentUserId);
            otherAgreed = agreementRepository.existsByChatRoomIdAndUserId(roomIdForQuery, otherUserId);
        }

        SharePost post = sharePostRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 게시글이 없습니다."));

        return ReservationStatusResponseDto.builder()
                .postId(postId)
                .postStatus(post.getStatus().name())
                .myAgreed(myAgreed)
                .otherAgreed(otherAgreed)
                .bothAgreed(bothAgreed)
                .build();
    }

    /**
     * 예약 동의 하기. 두 명 모두 동의 시 게시글 상태를 RESERVED로 변경.
     * @param roomId 채팅방 ID (한 글에 여러 채팅방이 있을 수 있으므로 필수)
     */
    @Transactional
    public ReservationStatusResponseDto agree(Long postId, Long roomId, Long currentUserId) {
        ChatRoom room = findRoomByRoomIdOrThrow(roomId, postId, currentUserId);
        SharePost post = room.getSharePost();

        if (post.getStatus() != ShareStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 예약되었거나 나눔이 완료/삭제된 게시글입니다.");
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (agreementRepository.existsByChatRoomIdAndUserId(room.getId(), currentUserId)) {
            return getStatus(postId, roomId, currentUserId);
        }

        agreementRepository.save(ChatRoomReservationAgreement.builder()
                .chatRoom(room)
                .user(user)
                .build());

        List<ChatRoomReservationAgreement> agreements = agreementRepository.findByChatRoomIdWithUser(room.getId());
        if (agreements.size() == 2) {
            post.updateStatus(ShareStatus.RESERVED);
            sharePostRepository.saveAndFlush(post);
        }

        return getStatus(postId, roomId, currentUserId);
    }

    /**
     * roomId로 채팅방 조회. "해당 room의 참여자(buyer 또는 seller)"인 경우에만 조회 성공.
     * 한 번의 쿼리로 참여 여부까지 판단해 403 원인 제거.
     */
    private ChatRoom findRoomByRoomIdOrThrow(Long roomId, Long sharePostId, Long userId) {
        if (!sharePostRepository.existsById(sharePostId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 게시글이 없습니다.");
        }
        ChatRoom room = chatRoomRepository.findByIdAndParticipant(roomId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 채팅방을 찾을 수 없거나 예약 권한이 없습니다."));
        if (!room.getSharePost().getId().equals(sharePostId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 채팅방은 이 게시글의 채팅방이 아닙니다.");
        }
        return room;
    }

    private Long getOtherUserId(ChatRoom room, Long currentUserId) {
        if (room.getBuyer().getId().equals(currentUserId)) {
            return room.getSeller().getId();
        }
        return room.getBuyer().getId();
    }
}
