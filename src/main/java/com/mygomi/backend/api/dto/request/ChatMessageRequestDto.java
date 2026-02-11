package com.mygomi.backend.api.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRequestDto {
    private Long roomId;     // 나중에 Post ID를 넣을 자리
    // private String sender;   // 보낸 사람 닉네임 또는 이메일, 토큰으로 식별하므로 필요 없음
    private String message;  // 채팅 내용
}