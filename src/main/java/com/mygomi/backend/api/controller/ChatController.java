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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Chat", description = "채팅 관리 API")
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    @Operation(summary = "채팅방 생성", description = "나눔 게시글(sharePostId)에 대한 채팅방을 생성합니다.")
    @PostMapping("/api/chat/room")
    public ResponseEntity<Long> createRoom(@RequestParam Long sharePostId, Principal principal) {
        String email = principal.getName();
        Long roomId = chatService.createChatRoom(sharePostId, email);
        return ResponseEntity.ok(roomId);
    }

    @Operation(summary = "내 채팅방 목록 조회", description = "로그인한 사용자가 참여 중인 채팅방 목록을 조회합니다.")
    @GetMapping("/api/chat/rooms")
    public ResponseEntity<List<ChatRoomResponseDto>> getMyRooms(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(chatService.getMyChatRooms(email));
    }

    @Operation(summary = "채팅 메시지 내역 조회", description = "특정 채팅방의 메시지 내역을 조회합니다.")
    @GetMapping("/api/chat/room/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponseDto>> getMessages(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getMessages(roomId));
    }

    @Operation(summary = "[Socket] 메시지 전송", description = "WebSocket으로 메시지를 전송합니다.")
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
