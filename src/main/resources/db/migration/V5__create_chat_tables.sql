-- 1. 채팅방 테이블 (chat_room)
CREATE TABLE chat_room (
                           id BIGSERIAL PRIMARY KEY,
                           share_post_id BIGINT NOT NULL,
                           buyer_id BIGINT NOT NULL,
                           seller_id BIGINT NOT NULL,
                           created_at TIMESTAMP DEFAULT NOW(),
                           updated_at TIMESTAMP DEFAULT NOW(),

                           CONSTRAINT fk_chat_share_post FOREIGN KEY (share_post_id) REFERENCES share_posts (id),
                           CONSTRAINT fk_chat_buyer FOREIGN KEY (buyer_id) REFERENCES users (id),
                           CONSTRAINT fk_chat_seller FOREIGN KEY (seller_id) REFERENCES users (id)
);

-- 2. 채팅 메시지 테이블 (chat_message)
CREATE TABLE chat_message (
                              id BIGSERIAL PRIMARY KEY,
                              chat_room_id BIGINT NOT NULL,
                              sender_id BIGINT NOT NULL,
                              message TEXT NOT NULL,
                              created_at TIMESTAMP DEFAULT NOW(),
                              updated_at TIMESTAMP DEFAULT NOW(),

                              CONSTRAINT fk_message_chat_room FOREIGN KEY (chat_room_id) REFERENCES chat_room (id),
                              CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES users (id)
);