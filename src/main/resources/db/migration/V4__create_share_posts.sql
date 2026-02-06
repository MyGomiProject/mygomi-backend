
-- 1. 게시글 테이블
CREATE TABLE share_posts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,

    title VARCHAR(200) NOT NULL,
    description TEXT,

    view_count INTEGER NOT NULL DEFAULT 0,  -- 조회수 포함

    category VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',

    -- 위치 정보 (지도 핵심 기능)
    prefecture VARCHAR(50),
    ward VARCHAR(50),
    town VARCHAR(100),
    address VARCHAR(200),
    lat DOUBLE PRECISION NOT NULL,
    lng DOUBLE PRECISION NOT NULL,

    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 제약조건
    CONSTRAINT fk_share_posts_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT chk_share_posts_status
        CHECK (status IN ('OPEN', 'RESERVED', 'COMPLETED', 'DELETED')),
        -- RESERVED 빼려면: CHECK (status IN ('OPEN', 'COMPLETED', 'DELETED'))

    CONSTRAINT chk_share_posts_category
        CHECK (category IN ('FURNITURE', 'ELECTRONICS', 'CLOTHING', 'BOOKS',
                           'KITCHENWARE', 'SPORTS', 'ETC')),

    CONSTRAINT chk_share_posts_view_count CHECK (view_count >= 0)
);

-- 2. 게시글 이미지 테이블
CREATE TABLE share_post_images (
    id BIGSERIAL PRIMARY KEY,
    share_post_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_share_post_images_post FOREIGN KEY (share_post_id)
        REFERENCES share_posts(id) ON DELETE CASCADE
);

-- ========================================
-- 인덱스 (성능 최적화)
-- ========================================

-- 작성자 조회
CREATE INDEX idx_share_posts_user_id ON share_posts(user_id);

-- 상태별 최신순 조회
CREATE INDEX idx_share_posts_status_created ON share_posts(status, created_at DESC);

-- 지도 반경 검색 (핵심!)
CREATE INDEX idx_share_posts_lat_lng ON share_posts(lat, lng);

-- 인기글 조회 (조회수 높은 순)
CREATE INDEX idx_share_posts_view_count ON share_posts(view_count DESC)
    WHERE status = 'OPEN';

-- 이미지 순서
CREATE INDEX idx_share_post_images_post_id ON share_post_images(share_post_id, display_order);

-- ========================================
-- 트리거 (updated_at 자동 갱신)
-- ========================================

CREATE TRIGGER update_share_posts_updated_at
    BEFORE UPDATE ON share_posts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_share_post_images_updated_at
    BEFORE UPDATE ON share_post_images
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ========================================
-- 테스트 데이터
-- ========================================

/*
-- 유저 데이터 존재유무 유의
INSERT INTO share_posts (user_id, title, description, category, prefecture, ward, town, lat, lng, view_count, status) VALUES
-- 나눔 중 (OPEN)
(1, '냉장고 무료 나눔', '이사 가면서 쓰던 냉장고입니다. 상태 좋아요!', 'ELECTRONICS', '도쿄도', '아라카와구', '히가시닛포리', 35.729, 139.771, 15, 'OPEN'),
(1, '책상 드립니다', '깨끗한 책상 무료로 드려요. 직접 가져가셔야 합니다.', 'FURNITURE', '도쿄도', '아라카와구', '히가시닛포리', 35.728, 139.775, 8, 'OPEN'),
(7, '전자레인지 나눔', '잘 작동하는 전자레인지입니다', 'ELECTRONICS', '도쿄도', '아라카와구', '히가시닛포리', 35.729, 139.772, 23, 'OPEN'),

-- 예약 중 (RESERVED) - 선택사항
(7, '책 무더기 나눔', '소설책 10권 정도. 예약 중입니다', 'BOOKS', '도쿄도', '아라카와구', '히가시닛포리', 35.730, 139.770, 12, 'RESERVED'),

-- 나눔 완료 (COMPLETED)
(1, '운동화 나눔', '한 번만 신은 새 운동화. 나눔 완료되었습니다', 'CLOTHING', '도쿄도', '시나가와구', '에바라', 35.610, 139.710, 42, 'COMPLETED');
*/
-- ========================================
-- 확인 쿼리
-- ========================================

-- 1. 테이블 확인
-- SELECT table_name FROM information_schema.tables
-- WHERE table_schema = 'public' AND table_name LIKE 'share%';

-- 2. 나눔 중인 게시글만 조회
-- SELECT id, title, view_count, status FROM share_posts WHERE status = 'OPEN';

-- 3. 인기 게시글 TOP 3 (조회수 순)
-- SELECT id, title, view_count FROM share_posts
-- WHERE status = 'OPEN'
-- ORDER BY view_count DESC
-- LIMIT 3;

-- 4. 반경 5km 검색 (조회수 포함)
-- SELECT id, title, view_count,
--        (6371 * acos(cos(radians(35.729)) * cos(radians(lat)) *
--        cos(radians(lng) - radians(139.771)) + sin(radians(35.729)) *
--        sin(radians(lat)))) AS distance_km
-- FROM share_posts
-- WHERE status = 'OPEN'
-- HAVING distance_km <= 5.0
-- ORDER BY distance_km;