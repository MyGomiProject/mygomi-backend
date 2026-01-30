<<<<<<< HEAD
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
=======
-- auto-generated definition
create table IF NOT EXISTS areas
(
    id          bigserial
        primary key,
    region      varchar(50)  not null,
    prefecture  varchar(50)  not null,
    ward        varchar(50)  not null,
    town        varchar(100) not null,
    chome       varchar(50),
    banchi_text varchar(200),
    created_at  timestamp default CURRENT_TIMESTAMP,
    updated_at  timestamp default CURRENT_TIMESTAMP
);

alter table areas
    owner to users;

create index idx_prefecture_ward_town_chome
    on areas (prefecture, ward, town, chome);

-- auto-generated definition
create table IF NOT EXISTS collection_rules
(
    id         bigserial
        primary key,
    area_id    bigint      not null
        references areas
            on delete cascade,
    waste_type varchar(50) not null,
    rule_type  varchar(20) not null,
    weekdays   varchar(50),
    nth_weeks  varchar(50),
    note       text,
    created_at timestamp default CURRENT_TIMESTAMP,
    updated_at timestamp default CURRENT_TIMESTAMP
);

alter table collection_rules
    owner to users;

create index idx_area_waste_type
    on collection_rules (area_id, waste_type);

create index idx_waste_type
    on collection_rules (waste_type);

-- auto-generated definition
create table IF NOT EXISTS user_addresses
(
    id          bigserial
        primary key,
    user_id     bigint not null
        references users
            on delete cascade,
    area_id     bigint
                       references areas
                           on delete set null,
    prefecture  varchar(50),
    ward        varchar(50),
    town        varchar(100),
    chome       varchar(50),
    banchi_text varchar(200),
    is_primary  boolean   default false,
    lat         numeric(10, 8),
    lng         numeric(11, 8),
    created_at  timestamp default CURRENT_TIMESTAMP,
    updated_at  timestamp default CURRENT_TIMESTAMP
);

alter table user_addresses
    owner to users;

create index idx_user_addresses_user_id
    on user_addresses (user_id);

create index idx_user_addresses_area_id
    on user_addresses (area_id);

create index idx_user_addresses_user_primary
    on user_addresses (user_id, is_primary);

-- auto-generated definition
create table IF NOT EXISTS users
(
    id         bigserial
        primary key,
    email      varchar(255)                                    not null
        unique,
    password   varchar(255)                                    not null,
    nickname   varchar(100)                                    not null,
    role       varchar(20) default 'USER'::character varying   not null
        constraint users_role_check
            check ((role)::text = ANY ((ARRAY ['USER'::character varying, 'ADMIN'::character varying])::text[])),
    status     varchar(20) default 'ACTIVE'::character varying not null
        constraint users_status_check
            check ((status)::text = ANY
                   ((ARRAY ['ACTIVE'::character varying, 'SUSPENDED'::character varying, 'DELETED'::character varying])::text[])),
    created_at timestamp   default CURRENT_TIMESTAMP,
    updated_at timestamp   default CURRENT_TIMESTAMP
);

alter table users
    owner to users;

create index idx_users_email
    on users (email);

create index idx_users_status
    on users (status);

>>>>>>> develop
