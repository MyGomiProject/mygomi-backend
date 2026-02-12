package com.mygomi.backend.api.dto.response;

import com.mygomi.backend.domain.chat.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@Schema(description = "채팅 메시지 상세 정보 DTO")
public class ChatMessageResponseDto {

    @Schema(description = "메시지 고유 ID", example = "100")
    private Long messageId;

    @Schema(description = "보낸 사람 이메일 (본인 확인용)", example = "user@example.com")
    private String senderEmail;

    @Schema(description = "보낸 사람 닉네임", example = "나눔받고싶어요")
    private String senderNickname;

    @Schema(description = "메시지 내용", example = "네, 가능합니다!")
    private String message;

    @Schema(description = "보낸 시간 (yyyy-MM-dd HH:mm)", example = "2026-02-12 14:30")
    private String sendTime;

    public ChatMessageResponseDto(ChatMessage chatMessage) {
        this.messageId = chatMessage.getId();
        this.senderEmail = chatMessage.getSender().getEmail();
        this.senderNickname = chatMessage.getSender().getNickname();
        this.message = chatMessage.getMessage();
        this.sendTime = chatMessage.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}