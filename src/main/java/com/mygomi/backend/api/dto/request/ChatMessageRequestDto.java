package com.mygomi.backend.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "채팅 메시지 전송 요청 DTO")
public class ChatMessageRequestDto {

    @Schema(description = "채팅방 ID", example = "1")
    private Long roomId;     // 나중에 Post ID를 넣을 자리
    // private String sender;   // 보낸 사람 닉네임 또는 이메일, 토큰으로 식별하므로 필요 없음

    @Schema(description = "전송할 메시지 내용", example = "안녕하세요, 물건 아직 있나요?")
    private String message;  // 채팅 내용
}