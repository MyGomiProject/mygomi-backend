-- 한 구매자가 같은 나눔 게시글에 대해 채팅방을 여러 개 만들 수 없도록 유니크 제약 추가
-- (중복 방으로 인한 "2 results" 조회 오류 방지)
-- ※ 이미 (share_post_id, buyer_id) 중복이 있으면 마이그레이션 실패. 수동 정리 후 재실행 필요.
ALTER TABLE chat_room
ADD CONSTRAINT uq_chat_room_share_post_buyer UNIQUE (share_post_id, buyer_id);
