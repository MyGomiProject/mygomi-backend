-- ============================================
-- 0. 공통 함수 생성 (updated_at 자동 갱신용)
-- ============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';


-- ============================================
-- 1. Users 테이블 (사용자)
-- ============================================
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     email VARCHAR(255) NOT NULL UNIQUE,
                                     password VARCHAR(255) NOT NULL,
                                     nickname VARCHAR(100) NOT NULL,
                                     role VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
                                     status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED')),
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- users 트리거
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- users 인덱스
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);


-- ============================================
-- 2. Areas 테이블 (지역 정보)
-- ============================================
CREATE TABLE IF NOT EXISTS areas (
                                     id BIGSERIAL PRIMARY KEY,
                                     region VARCHAR(50) NOT NULL,
                                     prefecture VARCHAR(50) NOT NULL,
                                     ward VARCHAR(50) NOT NULL,
                                     town VARCHAR(100) NOT NULL,
                                     chome VARCHAR(50),
                                     banchi_text VARCHAR(200),
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- areas 트리거
DROP TRIGGER IF EXISTS update_areas_updated_at ON areas;
CREATE TRIGGER update_areas_updated_at
    BEFORE UPDATE ON areas
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- areas 인덱스
CREATE INDEX IF NOT EXISTS idx_prefecture_ward_town_chome ON areas (prefecture, ward, town, chome);


-- ============================================
-- 3. Collection Rules 테이블 (수거 규칙)
-- ============================================
CREATE TABLE IF NOT EXISTS collection_rules (
                                                id BIGSERIAL PRIMARY KEY,
                                                area_id BIGINT NOT NULL,
                                                waste_type VARCHAR(50) NOT NULL,
                                                rule_type VARCHAR(20) NOT NULL,
                                                weekdays VARCHAR(50),
                                                nth_weeks VARCHAR(50),
                                                note TEXT,
                                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                FOREIGN KEY (area_id) REFERENCES areas(id) ON DELETE CASCADE
);

-- collection_rules 트리거
DROP TRIGGER IF EXISTS update_collection_rules_updated_at ON collection_rules;
CREATE TRIGGER update_collection_rules_updated_at
    BEFORE UPDATE ON collection_rules
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- collection_rules 인덱스
CREATE INDEX IF NOT EXISTS idx_area_waste_type ON collection_rules (area_id, waste_type);
CREATE INDEX IF NOT EXISTS idx_waste_type ON collection_rules (waste_type);


-- ============================================
-- 4. User Addresses 테이블 (사용자 주소)
-- ============================================
CREATE TABLE IF NOT EXISTS user_addresses (
                                              id BIGSERIAL PRIMARY KEY,
                                              user_id BIGINT NOT NULL,
                                              area_id BIGINT,
                                              prefecture VARCHAR(50),
                                              ward VARCHAR(50),
                                              town VARCHAR(100),
                                              chome VARCHAR(50),
                                              banchi_text VARCHAR(200),
                                              is_primary BOOLEAN DEFAULT FALSE,
                                              lat DECIMAL(10, 8),
                                              lng DECIMAL(11, 8),
                                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                              FOREIGN KEY (area_id) REFERENCES areas(id) ON DELETE SET NULL
);

-- user_addresses 트리거
DROP TRIGGER IF EXISTS update_user_addresses_updated_at ON user_addresses;
CREATE TRIGGER update_user_addresses_updated_at
    BEFORE UPDATE ON user_addresses
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- user_addresses 인덱스
CREATE INDEX IF NOT EXISTS idx_user_addresses_user_id ON user_addresses(user_id);
CREATE INDEX IF NOT EXISTS idx_user_addresses_area_id ON user_addresses(area_id);
CREATE INDEX IF NOT EXISTS idx_user_addresses_user_primary ON user_addresses(user_id, is_primary);


-- ============================================
-- 5. Items 테이블 (품목 검색)
-- ============================================
CREATE TABLE IF NOT EXISTS items (
                                     id BIGSERIAL PRIMARY KEY,
                                     name_ko VARCHAR(200) NOT NULL,
                                     name_ja VARCHAR(200),
                                     name_en VARCHAR(200),
                                     waste_type VARCHAR(50) NOT NULL,
                                     description TEXT,
                                     example_keywords VARCHAR(500),
                                     prefecture VARCHAR(50),
                                     ward VARCHAR(50),
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- items 트리거
DROP TRIGGER IF EXISTS update_items_updated_at ON items;
CREATE TRIGGER update_items_updated_at
    BEFORE UPDATE ON items
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- items 인덱스
CREATE INDEX IF NOT EXISTS idx_items_name_ko ON items(name_ko);
CREATE INDEX IF NOT EXISTS idx_items_ward ON items(ward);
CREATE INDEX IF NOT EXISTS idx_items_waste_type ON items(waste_type);