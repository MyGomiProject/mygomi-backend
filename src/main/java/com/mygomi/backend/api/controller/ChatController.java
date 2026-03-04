package com.mygomi.backend.api.controller;

import com.mygomi.backend.api.dto.request.ChatMessageRequestDto;
import com.mygomi.backend.api.dto.response.ChatMessageResponseDto;
import com.mygomi.backend.api.dto.response.ChatRoomResponseDto;
import com.mygomi.backend.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Chat", description = "채팅 관련 API (채팅방 생성, 목록 조회, 메시지 내역)")
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

<<<<<<< HEAD
    // 1. 채팅방 생성 (게시글 상세 화면에서 '채팅하기' 누를 때)
=======
    // --- HTTP API (채팅방 관리) ---

    @Operation(summary = "채팅방 생성", description = "나눔 게시글(sharePostId)에 대한 채팅방을 생성합니다. 이미 존재하는 방이면 해당 방의 ID를 반환합니다.")
>>>>>>> f66f16c56fb2959d9d5fa9c657da3ebcad2222e4
    @PostMapping("/api/chat/room")
    public ResponseEntity<Long> createRoom(@RequestParam Long sharePostId, Principal principal) {
        String email = principal.getName();
        Long roomId = chatService.createChatRoom(sharePostId, email);
        return ResponseEntity.ok(roomId);
    }

    @Operation(summary = "내 채팅방 목록 조회", description = "로그인한 사용자가 참여 중인 모든 채팅방 목록을 최신순으로 조회합니다.")
    @GetMapping("/api/chat/rooms")
    public ResponseEntity<List<ChatRoomResponseDto>> getMyRooms(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(chatService.getMyChatRooms(email));
    }

    @Operation(summary = "채팅 메시지 내역 조회", description = "특정 채팅방의 과거 메시지 내역을 전체 조회합니다.")
    @GetMapping("/api/chat/room/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponseDto>> getMessages(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getMessages(roomId));
    }

<<<<<<< HEAD
    // 4. 메시지 전송 (웹소켓)
=======
    // --- WebSocket (실시간 메시지) ---

    // 4. 메시지 전송
    @Operation(summary = "[Socket] 메시지 전송", description = "WebSocket을 통해 메시지를 전송합니다. (HTTP 호출 아님)")
>>>>>>> f66f16c56fb2959d9d5fa9c657da3ebcad2222e4
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
