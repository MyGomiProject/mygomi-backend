package com.mygomi.backend.api.dto.response;

import com.mygomi.backend.domain.chat.ChatRoom;
import lombok.Getter;

@Getter
public class ChatRoomResponseDto {
    private Long roomId;
    private String postTitle;
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