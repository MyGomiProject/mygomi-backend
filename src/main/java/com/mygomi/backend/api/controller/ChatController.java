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
@Tag(name = "Chat", description = "Chat APIs (room creation, room list, message history)")
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    // --- HTTP API (chat room management) ---
    @Operation(
            summary = "Create chat room",
            description = "Creates a chat room for sharePostId. If already exists, returns existing room ID."
    )
    @PostMapping("/api/chat/room")
    public ResponseEntity<Long> createRoom(@RequestParam Long sharePostId, Principal principal) {
        String email = principal.getName();
        Long roomId = chatService.createChatRoom(sharePostId, email);
        return ResponseEntity.ok(roomId);
    }

    @Operation(
            summary = "Get my chat rooms",
            description = "Returns all rooms the current user participates in, ordered by latest activity."
    )
    @GetMapping("/api/chat/rooms")
    public ResponseEntity<List<ChatRoomResponseDto>> getMyRooms(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(chatService.getMyChatRooms(email));
    }

    @Operation(
            summary = "Get message history",
            description = "Returns all historical messages for a specific room."
    )
    @GetMapping("/api/chat/room/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponseDto>> getMessages(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getMessages(roomId));
    }

    // --- WebSocket (real-time messaging) ---
    @Operation(
            summary = "[Socket] Send message",
            description = "Sends a chat message over WebSocket. Not an HTTP endpoint."
    )
    @MessageMapping("/chat/message")
    public void message(ChatMessageRequestDto messageRequest, Principal principal) {
        if (principal == null) {
            log.error("Chat send failed: missing authentication principal");
            return;
        }
        String email = principal.getName();
        ChatMessageResponseDto savedMessage = chatService.saveMessage(messageRequest, email);
        messagingTemplate.convertAndSend("/sub/chat/room/" + messageRequest.getRoomId(), savedMessage);
    }
}
