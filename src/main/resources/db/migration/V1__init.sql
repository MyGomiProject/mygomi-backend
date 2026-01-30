-- 1. 사용자 테이블
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100),
    role VARCHAR(20) DEFAULT 'USER',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 지역 테이블 (수거 규칙 매칭용)
CREATE TABLE areas (
    id BIGSERIAL PRIMARY KEY,
    region VARCHAR(50),
    prefecture VARCHAR(50),
    ward VARCHAR(50),
    town VARCHAR(100),
    chome VARCHAR(50),
    banchi_text VARCHAR(200)
);

-- 3. 사용자 주소 테이블
-- (주의: 이미 NUMERIC으로 만들어졌으므로 이 파일은 그대로 둬야 합니다)
CREATE TABLE user_addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    area_id BIGINT REFERENCES areas(id),
    prefecture VARCHAR(50),
    ward VARCHAR(50),
    town VARCHAR(100),
    chome VARCHAR(50),
    banchi_text VARCHAR(200),
    is_primary BOOLEAN DEFAULT false,
    lat NUMERIC(10, 8),
    lng NUMERIC(11, 8),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);