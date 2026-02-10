package com.mygomi.backend.api.dto.response;

import com.mygomi.backend.domain.chat.ChatMessage;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class ChatMessageResponseDto {
    private Long messageId;
    private String senderEmail;
    private String senderNickname;
    private String message;
    private String sendTime;

    public ChatMessageResponseDto(ChatMessage chatMessage) {
        this.messageId = chatMessage.getId();
        this.senderEmail = chatMessage.getSender().getEmail();
        this.senderNickname = chatMessage.getSender().getNickname();
        this.message = chatMessage.getMessage();
        this.sendTime = chatMessage.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}