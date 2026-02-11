package com.mygomi.backend.service;

import com.mygomi.backend.api.dto.request.ChatMessageRequestDto;
import com.mygomi.backend.api.dto.response.ChatMessageResponseDto;
import com.mygomi.backend.api.dto.response.ChatRoomResponseDto;
import com.mygomi.backend.domain.chat.ChatMessage;
import com.mygomi.backend.domain.chat.ChatRoom;
import com.mygomi.backend.domain.share.SharePost;
import com.mygomi.backend.domain.user.User;
import com.mygomi.backend.repository.ChatMessageRepository;
import com.mygomi.backend.repository.ChatRoomRepository;
import com.mygomi.backend.repository.SharePostRepository;
import com.mygomi.backend.repository.UserRepository; // 유저 리포지토리 필요
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SharePostRepository sharePostRepository;
    private final UserRepository userRepository;

    // 1. 채팅방 생성 (또는 이미 있으면 조회)
    @Transactional
    public Long createChatRoom(Long sharePostId, String buyerEmail) {
        SharePost sharePost = sharePostRepository.findById(sharePostId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        User buyer = userRepository.findByEmail(buyerEmail) // 닉네임 대신 이메일로 찾는 게 안전합니다
                .orElseThrow(() -> new IllegalArgumentException("구매자를 찾을 수 없습니다."));

        // 판매자 찾기 (게시글의 userId로 조회)
        User seller = userRepository.findById(sharePost.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("판매자를 찾을 수 없습니다."));

        // 이미 존재하는 방인지 확인
        return chatRoomRepository.findBySharePostIdAndBuyerId(sharePostId, buyer.getId())
                .map(ChatRoom::getId)
                .orElseGet(() -> {
                    ChatRoom chatRoom = ChatRoom.builder()
                            .sharePost(sharePost)
                            .buyer(buyer)
                            .seller(seller)
                            .build();
                    return chatRoomRepository.save(chatRoom).getId();
                });
    }

    // 2. 메시지 저장
    @Transactional
    public ChatMessageResponseDto saveMessage(ChatMessageRequestDto requestDto, String userEmail) {
        ChatRoom chatRoom = chatRoomRepository.findById(requestDto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        // 토큰에서 뽑은 이메일로 유저를 확실하게 찾음! (닉네임 중복 걱정 X, 사칭 걱정 X)
        User sender = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // ★ [보안 패치] 대화 참여자가 아니면 에러 발생 ★
        if (!chatRoom.getBuyer().getId().equals(sender.getId()) &&
                !chatRoom.getSeller().getId().equals(sender.getId())) {
            throw new IllegalArgumentException("이 채팅방에 참여할 권한이 없습니다.");
        }

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .message(requestDto.getMessage())
                .build();

        chatMessageRepository.save(chatMessage);
        return new ChatMessageResponseDto(chatMessage);
    }

    // 3. 내 채팅방 목록 조회
    public List<ChatRoomResponseDto> getMyChatRooms(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자 찾기 실패"));

        return chatRoomRepository.findAllByUserId(user.getId()).stream()
                .map(chatRoom -> new ChatRoomResponseDto(chatRoom, user.getId()))
                .collect(Collectors.toList());
    }

    // 4. 채팅방 메시지 내역 조회
    public List<ChatMessageResponseDto> getMessages(Long roomId) {
        return chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId).stream()
                .map(ChatMessageResponseDto::new)
                .collect(Collectors.toList());
    }
}