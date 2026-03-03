-- 예약 동의: 채팅방 단위로 작성자·나눔 신청자 둘 다 동의 시 해당 게시글을 RESERVED로 변경
CREATE TABLE chat_room_reservation_agreement (
    id BIGSERIAL PRIMARY KEY,
    chat_room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    agreed_at TIMESTAMP DEFAULT NOW(),

    CONSTRAINT fk_agreement_chat_room FOREIGN KEY (chat_room_id) REFERENCES chat_room (id),
    CONSTRAINT fk_agreement_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_chat_room_user UNIQUE (chat_room_id, user_id)
);

CREATE INDEX idx_agreement_chat_room ON chat_room_reservation_agreement (chat_room_id);
