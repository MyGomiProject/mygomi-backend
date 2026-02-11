-- ========================================
-- 신고 테이블 (게시글 신고 + 잘못된 정보 신고 통합)
-- ========================================

CREATE TABLE reports (
    id BIGSERIAL PRIMARY KEY,

    -- 신고 유형: SHARE_POST(게시글 신고), INFO(잘못된 정보 신고)
    type VARCHAR(20) NOT NULL,

    -- 신고자
    reporter_id BIGINT,
    reporter_email VARCHAR(255),
    email_reply BOOLEAN NOT NULL DEFAULT FALSE,

    -- 신고 대상 게시글 (SHARE_POST 타입일 때만 사용)
    target_post_id BIGINT,

    -- 신고 사유 (SHARE_POST 타입일 때만 사용)
    reason VARCHAR(50),

    -- 신고 내용
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,

    -- 첨부파일 URL (INFO 타입일 때만 사용, PDF/이미지, 10MB 제한)
    attachment_url VARCHAR(500),

    -- 처리 상태
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    -- 관리자 메모
    admin_note TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reports_reporter
        FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE SET NULL,

    CONSTRAINT fk_reports_post
        FOREIGN KEY (target_post_id) REFERENCES share_posts(id) ON DELETE SET NULL,

    CONSTRAINT chk_reports_type
        CHECK (type IN ('SHARE_POST', 'INFO')),

    CONSTRAINT chk_reports_status
        CHECK (status IN ('PENDING', 'IN_REVIEW', 'RESOLVED', 'DISMISSED')),

    CONSTRAINT chk_reports_reason
        CHECK (reason IN ('FAKE_POST', 'OFFENSIVE', 'SPAM', 'PRIVACY', 'COPYRIGHT', 'INAPPROPRIATE', 'OTHER') OR reason IS NULL)
);

-- 인덱스
CREATE INDEX idx_reports_type_status    ON reports(type, status);
CREATE INDEX idx_reports_status_created ON reports(status, created_at DESC);
CREATE INDEX idx_reports_reporter_id    ON reports(reporter_id);
CREATE INDEX idx_reports_post_id        ON reports(target_post_id);

-- updated_at 자동 갱신 트리거
CREATE TRIGGER update_reports_updated_at
    BEFORE UPDATE ON reports
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
