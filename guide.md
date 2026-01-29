# 마이고미(MAIGOMI) — 초기 개발 가이드 (Backend 중심: Entity/DB 설계 + 협업 운영)

작성 목적
- “지금부터 개발을 시작한다”는 전제로, **필수 도메인(Entity)와 DB 스키마**, **API/권한 구조**, **협업 규칙**까지 한 번에 정리합니다.
- 주니어 팀 기준으로 “왜 이렇게 나누는지 / 무엇을 먼저 만들지”를 단계적으로 설명합니다.

---

## 0. 제품 요약

- 한 줄 소개: **복잡한 일본 분리수거 규칙을 한눈에, 처치 곤란한 물건은 이웃과 함께.**
- 핵심 축 3가지
    1) **지역 기반 수거 캘린더** (구/동/町 + 번지 범위 단위)
    2) **품목별 분류/검색** (가연/불연/플라/자원 등)
    3) **무료 나눔 커뮤니티 + 채팅 + 알림**

---

## 1. 기술 스택 제안 (React + Spring Boot 기준)

### 1.1 Backend
- Spring Boot 3.x (가능하면) / Spring Security
- JPA(Hibernate)
- DB: **MySQL** (PostgreSQL에서 변경)
- 실시간 채팅: 1안) WebSocket(STOMP) 2안) Firebase
- 알림: Quartz/Spring Scheduler + Web Push(웹) 또는 FCM(모바일/웹)

### 1.2 Frontend
- **React + TypeScript** 시도 후 어려울 경우 React + JS로 빠르게 변경
- 캘린더: FullCalendar
- 지도: Google Maps 또는 OpenStreetMap(Leaflet)

### 1.3 인프라/운영(초기)
- **Docker** (1~2일 투자 시도)
- CI: GitHub Actions(빌드/테스트/린트)

---

## 2. 개발 순서(권장) — “데이터 → 조회 → 서비스 확장 → 커뮤니티” 순

### Phase 1: 기반 공사 (필수)
1) 사용자/권한/로그인(REQ-01)
2) 지역 데이터 모델링 + 수거 캘린더 조회(REQ-02)
3) 품목 분류/검색(REQ-03, 04)

### Phase 2: 커뮤니티 (기존 Phase 2에서 뒤로 밀림)
4) 나눔 게시판 + 이미지(REQ-08)
5) 1:1 채팅(REQ-09)
6) 지도 기반 나눔(REQ-10)

### Phase 3: 생활 편의 확장 (기존 Phase 3에서 앞당김)
7) 대형폐기물 수수료 계산기(REQ-05)
8) 행정 링크(REQ-06)
9) 제보/검수 프로세스(REQ-07)

### Phase 4: 알림
10) 수거일 알림(REQ-11)
11) 키워드 알림(REQ-12)

---

## 3. 도메인 설계(업무 개념) — “테이블을 바로 만들지 말고 개념부터”

### 3.1 핵심 개념(Glossary)
- **Area(지역)**: 도/현 → 구 → 동/町(쵸) → 번지 범위(전역 또는 1-22, 25, 26 같은 표현)
- **CollectionRule(수거 규칙)**: 가연/불연/플라/자원(병캔, 종이 등)별 요일/주차 규칙
- **Item(품목)**: “이 품목은 가연/불연/자원/플라 중 무엇인가?” + 배출 방법 설명
- **SodaiItem(대형폐기물 품목)**: 지자체별 수수료(스티커) 기준
- **SharePost(나눔 게시글)**: **무료나눔**/거래 상태/위치/사진/채팅 연동
- **Report(제보)**: 사용자 제보 → 관리자 검수 → 승인/반려
- **Notification(알림)**: 예약 알림(수거일) + 이벤트 알림(키워드 매칭)

---

## 4. DB 설계 (ERD 수준으로 “필요 테이블” 제안)

### 4.1 사용자/권한

#### 4.1.1 `users`
- 목적: 회원/로그인/프로필/기본 지역설정
- 주요 컬럼
    - `id` (PK)
    - `email` (UNIQUE)
    - `password` (기존 password_hash 삭제)
    - `nickname`
    - `role` (USER, ADMIN)
    - `status` (ACTIVE, SUSPENDED, DELETED)
    - `created_at`, `updated_at`

#### 4.1.2 `user_addresses`
- 목적: “내 거주지(구/동/町/번지)”를 1개 이상 저장 가능하게
- 주요 컬럼
    - `id` (PK)
    - `user_id` (FK -> users)
    - `prefecture` (도/현)
    - `ward` (구)
    - `town` (동/町)
    - `chome` (丁目)
    - `banchi_text` (번지 표현 원문)
    - `is_primary`: 대표 주소 설정 (사용자가 여러 개의 주소를 등록했을 때, 그중 '기본'으로 사용할 주소가 무엇인지를 구분하기 위한 설정)
    - `lat`, `lng`
    - `created_at`, `updated_at`

> 왜 `banchi_text`를 문자열로?  
> 번지 범위가 “1-22, 25, 26”처럼 불규칙합니다. 초기에는 **원문 저장 + 해석 로직**이 가장 안전합니다.

---


---

### 4.2 지역별 수거 규칙(가장 중요)

#### 4.2.1 `areas`
- 목적: 행정 구역을 정규화(검색/필터 안정화)
- 주요 컬럼
    - `id` (PK)
    - `region` (지방: 関東 등)
    - `prefecture` (도/현)
    - `ward` (구)
    - `town` (町)
    - `chome` (丁目)
    - `banchi_text` (번지 원문: 전역/범위)
    - 인덱스: `(prefecture, ward, town, chome)`

> `areas`에는 “규칙이 동일하게 적용되는 최소 단위”를 저장합니다.  
> 예: 오타구 이케가미 1 전역 / 오타구 이케가미 3 1-22,25,26 … 

#### 4.2.2 `collection_rules`
- 목적: `areas` 단위로 수거 타입별 규칙 저장
- 주요 컬럼
    - `id` (PK)
    - `area_id` (FK -> areas)
    - `waste_type` (BURNABLE, NON_BURNABLE, PLASTIC, CAN_BOTTLE, PAPER 등)
    - `rule_type` (WEEKDAY, NTH_WEEKDAY, CUSTOM)
    - `weekdays` (예: `MON,THU` / `WED` / `SAT`)
    - `nth_weeks` (예: `2,4` 혹은 `1,3`)  ※ `rule_type=NTH_WEEKDAY`일 때 사용
    - `note` (예외/특이사항)
    - `created_at`, `updated_at`
- 인덱스
    - `(area_id, waste_type)`
    - `(waste_type)`

> 예시 매핑
> - “월, 목” → `rule_type=WEEKDAY`, `weekdays=MON,THU`
> - “2,4주 금” → `rule_type=NTH_WEEKDAY`, `weekdays=FRI`, `nth_weeks=2,4`

#### 4.2.3 `waste_types` (선택: 코드 테이블)
- 목적: 다국어/아이콘/표시명 관리
- 주요 컬럼
    - `code` (PK) 예: BURNABLE
    - `label_ko`, `label_ja`, `label_en`
    - `icon_key` (프론트 아이콘 매핑)

---

### 4.3 품목 분류/검색

#### 4.3.1 `items`
- 목적: 키워드 검색으로 “이 물건은 어디에 버려요?” 제공
- 주요 컬럼
    - `id` (PK)
    - **`name_ko`** (기본 검색 기준)
    - `name_ja` (선택)
    - `name_en` (선택)
    - `waste_type` (기본 분류)
    - `description` (배출 방법/주의사항)
    - **`example_keywords`**: 콤마로 구분된 태그 (예: 포카리, 스웨트)
    - `created_at`, `updated_at`
- 인덱스: 
  - `(name_ko)`
  - Full-Text(가능하면): `name_ja`, `example_keywords`

> **수정 사항**: 기본 검색 기준이 한국어(`ko`)로 변경됨에 따라 기존 4.3.2. `item_aliases(표기 흔들림에 따른 대응 테이블)` 테이블은 삭제합니다.

---

### 4.4 대형 폐기물(수수료 계산기 + 행정 링크)

#### 4.4.1 `municipal_services`
- 목적: 구별 행정 링크 유지보수 (신고 링크 1개, 안내 링크 1개)
- 주요 컬럼: 
  - `id` (PK)
  - `prefecture
  - `ward`
  - `sodai_apply_url` (신고할 수 있는 연결 페이지)
  - `info_url` (안내 페이지)
  - `note`
  - 인덱스: `(prefecture, ward)`

#### 4.4.2 `sodai_items`
- 목적: 지자체별 품목/수수료(스티커) 기준
- 주요 컬럼
    - `id` (PK)
    - `prefecture`, `ward`
    - `name` (품목명)
    - `fee_yen` (정수)
    - `rule_note` (치수 조건 등)
    - 인덱스: `(prefecture, ward, name)`

> **수정 사항**: `sodai_calc_history` 테이블 삭제

---

### 4.5 사용자 제보/검수(운영)

#### 4.5.1 `reports`
- 목적: 데이터 누락/변경 제보 → 승인 후 반영
- 주요 컬럼
    - `id` (PK)
    - `user_id` (FK)
    - `report_type` (COLLECTION_RULE, ITEM, SERVICE_URL 등)
    - `target_ref` (어떤 데이터인지 식별: area_id/item_id/ward 등)
    - `content` (제보 내용)
    - `status` (PENDING, APPROVED, REJECTED)
    - `admin_comment` (반려 사유 등)
    - `created_at`, `updated_at`
- 인덱스: `(status, report_type)`

#### 4.5.2 `report_audits` (선택)
- 목적: 누가 언제 승인/반려 했는지 이력 관리
- 주요 컬럼
    - `id` (PK)
    - `report_id` (FK)
    - `admin_id` (FK -> users)
    - `action` (APPROVE/REJECT)
    - `comment`
    - `created_at`

---

### 4.6 나눔 커뮤니티(게시판 + 이미지 + 지도)

#### 4.6.1 `share_posts`
- 목적: 나눔 글(무료)
- 주요 컬럼
    - `id` (PK)
    - `user_id` (FK)
    - `title`
    - `content`
    - `category` (FURNITURE, ELECTRONICS, ETC)
    - `status` (OPEN, RESERVED, COMPLETED, DELETED)
    - `prefecture`, `ward` (지역 필터)
    - `town` (선택)
    - `lat`, `lng` (지도 핀)
    - `thumbnail_url` (선택)
    - `created_at`, `updated_at`
- 인덱스
    - `(ward, status, created_at)`
    - `(user_id, created_at)`

#### 4.6.2 `share_post_images`
- 목적: 사진 다중 업로드
- 주요 컬럼
    - `id` (PK)
    - `post_id` (FK -> share_posts)
    - `image_url`
    - `sort_order`

#### 4.6.3 `share_post_tags` (선택)
- 목적: 키워드 알림 매칭용 태그
- 주요 컬럼
    - `id` (PK)
    - `post_id` (FK)
    - `tag` (예: 냉장고, 책상)
- 인덱스: `(tag)`

---

### 4.7 1:1 채팅

> 채팅은 구현 선택지가 많습니다. DB는 “대화방/메시지”만 정리해도 충분합니다.

#### 4.7.1 `chat_rooms`
- 목적: 게시글 기반 1:1 대화방
- 주요 컬럼
    - `id` (PK)
    - `post_id` (FK -> share_posts)
    - `buyer_id` (FK -> users)  ※ “받는 사람”
    - `seller_id` (FK -> users) ※ “올린 사람”
    - `status` (ACTIVE, CLOSED)
    - `created_at`, `updated_at`
- 제약
    - `(post_id, buyer_id, seller_id)` UNIQUE (중복 방 방지)

#### 4.7.2 `chat_messages`
- 목적: 메시지 저장
- 주요 컬럼
    - `id` (PK)
    - `room_id` (FK -> chat_rooms)
    - `sender_id` (FK -> users)
    - `message_type` (TEXT, IMAGE, SYSTEM)
    - `content`
    - `created_at`
- 인덱스: `(room_id, created_at)`

---

### 4.8 알림(수거일 + 키워드)

#### 4.8.1 `notification_settings`
- 목적: 사용자 알림 설정
- 주요 컬럼
    - `id` (PK)
    - `user_id` (FK)
    - `collection_alert_enabled` (bool)
    - `collection_alert_time` (예: 20:00 / 08:00)
    - `keyword_alert_enabled` (bool)
    - `created_at`, `updated_at`

#### 4.8.2 `keyword_subscriptions`
- 목적: 관심 키워드 목록
- 주요 컬럼
    - `id` (PK)
    - `user_id` (FK)
    - `keyword` (인덱스)
    - `created_at`
- 인덱스: `(keyword)`, `(user_id)`

#### 4.8.3 `notifications`
- 목적: 발송 로그/읽음 처리
- 주요 컬럼
    - `id` (PK)
    - `user_id` (FK)
    - `type` (COLLECTION, KEYWORD, CHAT 등)
    - `title`
    - `body`
    - `link_url` (앱 내 이동)
    - `sent_at`
    - `read_at` (NULL이면 미읽음)
- 인덱스: `(user_id, sent_at)`

#### 4.8.4 `push_tokens` (웹/앱)
- 목적: Web Push/FCM 토큰 저장
- 주요 컬럼
    - `id` (PK)
    - `user_id` (FK)
    - `platform` (WEB, ANDROID, IOS)
    - `token`
    - `created_at`, `updated_at`
- 제약: `(platform, token)` UNIQUE

---

## 5. Entity 설계 팁 (주니어가 자주 실수하는 지점)

### 5.1 “문자열 원문 보존” 원칙
- 번지/주차 규칙처럼 복잡한 공공 데이터는 파싱 실수 위험이 큽니다.
- 따라서:
    - 1) **원문 컬럼 보존(`banchi_text`, `note`)**
    - 2) 서비스에서 해석(파싱) 결과를 계산해 사용
    - 3) 오류 발견 시 원문 기준으로 되돌리기 쉬움

### 5.2 Status/Soft Delete
- 게시글/유저는 삭제보다 **상태값 관리**가 안전합니다.
- 나중에 분쟁/운영 대응이 쉬워집니다.

### 5.3 인덱스는 “조회 패턴” 기준
- 캘린더: `(prefecture, ward, town, chome)`로 `areas` 탐색이 많음
- 게시판: `(ward, status, created_at)`로 리스트 조회가 많음
- 채팅: `(room_id, created_at)`로 최신 메시지 페이징

---



## 6. API 설계 (주요 수정)


### 6.1 인증/유저
- `POST /api/auth/signup`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/users/me`
- `PUT /api/users/me`
- `POST /api/users/me/addresses`
- `PUT /api/users/me/addresses/{id}`
- `DELETE /api/users/me/addresses/{id}` (soft delete or remove)

### 6.2 수거 정보
- `GET /api/areas/search?prefecture=東京都&ward=大田区&town=池上&chome=1&banchi=...`
- `GET /api/collection/calendar?addressId=...&from=2026-01-01&to=2026-01-31`
- `GET /api/collection/rules?areaId=...`

### 6.3 품목 검색
- `GET /api/items/search?q=...&ward=...` (자기 위치에 해당하는 구 안에서 물품 검색 기능 추가)
- `GET /api/items/{id}`

### 6.4 대형폐기물
- `GET /api/municipal-services?prefecture=...&ward=...` (신고 링크 및 안내 링크 제공)

### 6.5 제보
- `POST /api/reports`
- `GET /api/reports/mine`
- (ADMIN) `GET /api/admin/reports?status=PENDING`
- (ADMIN) `POST /api/admin/reports/{id}/approve`
- (ADMIN) `POST /api/admin/reports/{id}/reject`

### 6.6 나눔 게시판
- `POST /api/share-posts`
- `GET /api/share-posts?ward=大田区&status=OPEN&page=...`
- `GET /api/share-posts/{id}`
- `PUT /api/share-posts/{id}`
- `DELETE /api/share-posts/{id}`


### 6.7 채팅
- [cite_start]**채팅 내 사진 전송 기능은 우선 삭제**하고 텍스트 기반 웹소켓 통신에 집중합니다.
- `POST /api/chat/rooms` (postId + 상대 정보로 방 생성/조회)
- `GET /api/chat/rooms`
- `GET /api/chat/rooms/{id}/messages?page=...`
- WebSocket: `/ws` + `/topic/rooms/{id}` + `/app/messages`

### 6.8 알림
- `POST /api/push/tokens`
- `PUT /api/notification-settings`
- `POST /api/keywords` (키워드 등록)
- `DELETE /api/keywords/{id}`
- `GET /api/notifications`

---

## 7. 요구사항(REQ) ↔ 테이블 매핑(검증에 도움이 됨)

- REQ-01 회원가입/로그인
    - `users`
- REQ-02 지역 기반 캘린더
    - `user_addresses`, `areas`, `collection_rules`, `waste_types`
- REQ-03 품목별 분류 안내
    - `items` (+ `item_aliases`)
- REQ-04 쓰레기 유형 검색
    - `items`, `item_aliases` (검색 인덱스)
- REQ-05 수수료 계산기
    - `sodai_items` (+ `sodai_calc_history`)
- REQ-06 행정 서비스 연계
    - `municipal_services`
- REQ-07 제보/검수
    - `reports`, `report_audits`
- REQ-08 나눔 게시판
    - `share_posts`, `share_post_images`, `share_post_tags`
- REQ-09 채팅
    - `chat_rooms`, `chat_messages`
- REQ-10 나눔 지도
    - `share_posts(lat,lng)`
- REQ-11 수거일 알림
    - `notification_settings`, `notifications`, `push_tokens`
- REQ-12 키워드 알림
    - `keyword_subscriptions`, `notifications`
- REQ-13 반응형 UI
    - 프론트 기준(백엔드는 이미지/응답 페이징 최적화)

---

## 8. 협업을 위한 운영 규칙(주니어 팀 필수)

### 8.1 Git 브랜치 전략(가볍게)
- `main`: 배포 가능한 안정 브랜치
- `develop`: 통합 테스트 브랜치(선택)
- `feature/REQ-xx-짧은설명`: 기능 브랜치
- 커밋 메시지 규칙 예시
    - `feat: REQ-02 add collection calendar api`
    - `fix: handle banchi parsing edge case`

### 8.2 이슈/PR 템플릿(강추)
- Issue: REQ-ID 연결, 화면/기능 정의, 완료 조건(AC)
- PR: 무엇을 했는지 + 테스트 방법 + 스크린샷(프론트) + API 샘플(백)

### 8.3 API 문서화
- Swagger/OpenAPI를 “필수”로 두기
- 엔드포인트가 늘수록 말로 공유하면 무조건 깨집니다.

### 8.4 데이터 관리 (Seed Data)
- **통합 관리**: docs에 더미데이터를 넣었던 것처럼, 초기 데이터를 한 파일로 관리하여 DB가 망가져도 즉시 복구 가능하도록(`npm run` 등 활용) 구성합니다.
- 원칙
    - (1) 엑셀 원본은 `data/raw/`에 버전 관리(혹은 공유드라이브)
    - (2) DB Seed 스크립트는 `data/seed/`에 관리
    - (3) 파싱 로직은 항상 **재실행 가능(idempotent)** 하게
- 권장 형태
    - CSV로 내보내기 → Spring Batch/간단 Import 스크립트로 적재
    - 적재 실패 행은 별도 로그 파일로 남기기

### 8.5 역할 분담 예시(REQ 기준)
- Backend
    - 인증/권한, 지역조회/캘린더 API, 게시판/채팅/알림 API
- Frontend
    - 캘린더/지도 UI, 게시판/채팅 UI, 알림 설정 UI
- Data
    - 지역 데이터 정제/적재 자동화, 품목 사전 구축

---

## 9. 초기에 반드시 정해야 하는 “정규화 규칙”(데이터 품질)

### 9.1 요일 표준
- DB에는 `MON,TUE,WED,THU,FRI,SAT,SUN`만 저장
- 화면 표시만 한국어/일본어로 변환

### 9.2 주차 표준
- `1,2,3,4,5` 형태로 저장
- “제2,4주” 같은 원문은 `note`에 보관 가능

### 9.3 지역명 및 언어 표준
- **표준**: 모든 데이터와 가이드는 **한국어 기준**으로 다시 작성합니다.
- 일본어 표기 흔들림 대응 로직을 포함합니다.

---

## 10. 다음 액션(바로 개발 들어가기 체크리스트)

- [ ] `areas` + `collection_rules` 테이블 생성
- [ ] 오타구(大田区) 데이터 일부를 DB에 Seed
- [ ] “내 주소 설정 → 해당 area 찾기 → 규칙 조회” API 1개 완성
- [ ] FullCalendar에 월간 표시까지 연결
- [ ] 게시판은 CRUD부터(이미지는 S3/Cloudinary 등 외부 저장소로 URL만 저장)

---

## 11. UI/UX 및 기타 세부 요구사항 (신규)

- [cite_start]**캘린더**: 특정 날짜 클릭 시 노출되던 '오늘 버릴 것' 표시 기능을 삭제합니다.
- [cite_start]**지도 핀**: 커스텀 렌더링을 통해 아이콘 등을 저장해서 가져오는 형식으로 디자인을 확실히 차별화합니다.
- [cite_start]**검색 편의**: 동일 의미 검색어(별칭) 대응을 강화하되, 최근 검색어 저장 기능은 삭제합니다.


---

## 부록 A. Entity 클래스 설계(추천 규칙)

- JPA 연관관계는 “필요한 곳만” 양방향 사용
    - 예: `User` → `Address`는 OneToMany 가능
    - `SharePost` → `Images`는 OneToMany
- `@Enumerated(EnumType.STRING)`로 enum 저장(가독성/안전성)
- BaseEntity에 `createdAt/updatedAt` 공통 처리

---

## 부록 B. 테스트 전략(최소)
- Repository 테스트: `areas` 탐색, `collection_rules` 조회
- Service 테스트: 번지/주차 규칙 해석 로직(엣지 케이스)
- API 테스트: 주소 설정 후 캘린더 응답이 기대 규칙과 동일한지

---

원하면, 다음 단계로
1) 위 스키마를 기준으로 **ERD(텍스트/mermaid)** 형태로 정리하거나
2) Spring Boot 기준으로 **Entity 클래스 골격 + DDL**까지 한 번에 만들어 줄 수 있습니다.

