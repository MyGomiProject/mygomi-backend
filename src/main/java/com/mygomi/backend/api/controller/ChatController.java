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
@RestController // @Controller 대신 RestController 사용 (JSON 반환 위해)
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    // --- HTTP API (채팅방 관리) ---

    // 1. 채팅방 생성 (게시글 상세 화면에서 '채팅하기' 누를 때) (수정됨: 토큰 사용)
    @PostMapping("/api/chat/room")
    public ResponseEntity<Long> createRoom(@RequestParam Long sharePostId, Principal principal) {
        // 토큰이 없거나 만료된 경우 Spring Security가 알아서 403 에러를 뱉지만,
        // 명시적으로 체크할 수도 있습니다.
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

    // --- WebSocket (실시간 메시지) ---

    // 4. 메시지 전송
    @MessageMapping("/chat/message")
    public void message(ChatMessageRequestDto messageRequest, Principal principal) {

        if (principal == null) {
            log.error("채팅 전송 실패: 인증 정보 없음");
            return;
        }

        // principal.getName()은 CustomUserDetailsService에서 설정한 'email'입니다.
        String email = principal.getName();

        // 1. DB에 저장 (이메일을 넘김)
        ChatMessageResponseDto savedMessage = chatService.saveMessage(messageRequest, email);

        // 2. 구독자들에게 전송 (ResponseDto를 보내야 포맷이 깔끔함)
        messagingTemplate.convertAndSend("/sub/chat/room/" + messageRequest.getRoomId(), savedMessage);
    }
}