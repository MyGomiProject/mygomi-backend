-- user_addresses 테이블의 lat 컬럼 타입을 DOUBLE PRECISION으로 변경
ALTER TABLE user_addresses
    ALTER COLUMN lat TYPE DOUBLE PRECISION USING lat::DOUBLE PRECISION;

-- user_addresses 테이블의 lng 컬럼 타입을 DOUBLE PRECISION으로 변경
ALTER TABLE user_addresses
    ALTER COLUMN lng TYPE DOUBLE PRECISION USING lng::DOUBLE PRECISION;