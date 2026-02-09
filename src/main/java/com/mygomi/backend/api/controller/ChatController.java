package com.mygomi.backend.api.controller;

import com.mygomi.backend.api.dto.request.ChatMessageRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;

    // /pub/chat/message 주소로 메시지를 보내면 이 메서드가 실행됨
    @MessageMapping("/chat/message")
    public void message(ChatMessageRequestDto message) {
        // 받은 메시지를 /sub/chat/room/{roomId} 를 구독 중인 사람들에게 배달
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    }
}