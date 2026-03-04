package com.mygomi.backend.api.controller;

import com.mygomi.backend.api.dto.request.ChatMessageRequestDto;
import com.mygomi.backend.api.dto.response.ChatMessageResponseDto;
import com.mygomi.backend.api.dto.response.ChatRoomResponseDto;
import com.mygomi.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    // 1. 채팅방 생성 (게시글 상세 화면에서 '채팅하기' 누를 때)
    @PostMapping("/api/chat/room")
    public ResponseEntity<Long> createRoom(@RequestParam Long sharePostId, Principal principal) {
        String email = principal.getName();
        Long roomId = chatService.createChatRoom(sharePostId, email);
        return ResponseEntity.ok(roomId);
    }

    // 2. 내 채팅방 목록 조회
    @GetMapping("/api/chat/rooms")
    public ResponseEntity<List<ChatRoomResponseDto>> getMyRooms(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(chatService.getMyChatRooms(email));
    }

    // 3. 특정 채팅방 메시지 내역 조회 (채팅방 입장 시 호출)
    @GetMapping("/api/chat/room/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponseDto>> getMessages(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getMessages(roomId));
    }

    // 4. 메시지 전송 (웹소켓)
    @MessageMapping("/chat/message")
    public void message(ChatMessageRequestDto messageRequest, Principal principal) {
        if (principal == null) {
            log.error("채팅 전송 실패: 인증 정보 없음");
            return;
        }
        String email = principal.getName();
        ChatMessageResponseDto savedMessage = chatService.saveMessage(messageRequest, email);
        messagingTemplate.convertAndSend("/sub/chat/room/" + messageRequest.getRoomId(), savedMessage);
    }
}
