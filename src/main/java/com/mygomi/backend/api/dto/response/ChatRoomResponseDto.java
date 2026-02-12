package com.mygomi.backend.api.dto.response;

import com.mygomi.backend.domain.chat.ChatRoom;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "채팅방 목록 응답 DTO")
public class ChatRoomResponseDto {

    @Schema(description = "채팅방 ID", example = "1")
    private Long roomId;

    @Schema(description = "관련 나눔 게시글 제목", example = "이케아 의자 나눔합니다")
    private String postTitle;

    @Schema(description = "대화 상대방 닉네임", example = "나눔천사")
    private String opponentNickname; // 대화 상대방 닉네임

    public ChatRoomResponseDto(ChatRoom chatRoom, Long myUserId) {
        this.roomId = chatRoom.getId();
        this.postTitle = chatRoom.getSharePost().getTitle();

        // 내가 구매자면 판매자 닉네임을, 내가 판매자면 구매자 닉네임을 보여줌
        if (chatRoom.getBuyer().getId().equals(myUserId)) {
            this.opponentNickname = chatRoom.getSeller().getNickname();
        } else {
            this.opponentNickname = chatRoom.getBuyer().getNickname();
        }
    }
}