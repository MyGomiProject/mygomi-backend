# 마이고미(MAIGOMI) — 프론트/백엔드 순차 개발 로드맵 (주니어 팀용)

목표
- 프론트엔드(React)와 백엔드(Spring Boot)가 **어떤 순서로**, **무엇을**, **어떤 기술로** 개발해야 하는지 단계별로 정리합니다.
- “지금 당장 개발을 시작할 수 있는 수준”으로 **작업 단위(에픽/스토리)**, **필수 API**, **검수 기준**, **리스크/대안**까지 포함합니다.

---

## 0. 공통 전제(팀이 먼저 합의해야 하는 것)

### 0.1 MVP 정의(최소 출시 버전)
MVP는 아래 3가지만 “완성도 있게” 작동하면 됩니다.
1) **지역 설정 → 수거 캘린더 표시** (REQ-02)
2) **품목 검색 → 버리는 방법 안내** (REQ-03, 04)
3) **나눔 게시판 CRUD + 사진 업로드 + 기본 지역 필터** (REQ-08)

> 채팅/지도/알림은 MVP 이후 단계에서 붙이는 것이 일정 리스크가 낮습니다.

### 0.2 개발 방식
- **백엔드가 API 계약을 먼저 정의(OpenAPI/Swagger)** → 프론트는 Mock 데이터로 UI 병렬 개발 → 백엔드 완성 후 실연동
- 데이터(오타구 등)는 “엑셀 → CSV → Seed” 방식으로 초기 적재 자동화

### 0.3 환경 통일(주니어 팀 생산성 핵심)
- Docker Compose로 로컬 DB/Redis(선택)/MinIO(선택) 통일
- `.env.example` 제공 후 개인별 `.env`로 운영

---

## 1. 단계별 개발 로드맵(Front/Back 순차 + 병렬 포인트 포함)

아래는 **Phase 0 ~ Phase 4**로 진행합니다.
각 Phase마다:
- 백엔드(Back) 먼저: DB/Entity/Service/API 제공
- 프론트(Front) 다음: 화면/상태관리/UX 구현
- 병렬 가능: 디자인 시스템/공통 컴포넌트, 문서화, 테스트

---

## Phase 0 — 프로젝트 세팅 & 협업 기반 (1주 내 목표)

### Back: 프로젝트 골격
**필요 기술**
- Spring Boot, Spring Security(초기에는 미적용 가능), JPA, Flyway/Liquibase(권장)
- Swagger(OpenAPI)
- Lombok(선택), Validation(javax/jakarta)
- Test: JUnit5

**필수 작업**
- 패키지 구조 확정 (예시)
    - `domain/` (entity, repository)
    - `service/`
    - `api/` (controller, dto)
    - `security/`
    - `config/`
- 공통 응답 포맷 정의
    - 성공: `{ data, meta }`
    - 실패: `{ errorCode, message, details }`
- DB 마이그레이션 도구 설정(Flyway 권장)
- Swagger UI로 API 스펙 자동 노출

**검수 기준**
- 로컬에서 `./gradlew test` 통과
- Swagger 접속 가능, 샘플 엔드포인트 1개 응답

---

### Front: 프로젝트 골격
**필요 기술**
- React + TypeScript
- Router(react-router), API Client(axios), 상태관리(React Query 권장)
- ESLint/Prettier, Husky + lint-staged(권장)

**필수 작업**
- 페이지 라우팅 구조
    - `/login`, `/signup`
    - `/onboarding/location`
    - `/calendar`
    - `/items/search`
    - `/share`
- 디자인 기준(간단)
    - 모바일 우선(Responsive), 아이콘 기반(외국인 배려)
- API 모듈 분리
    - `api/auth.ts`, `api/collection.ts`, `api/items.ts`, `api/share.ts`

**검수 기준**
- 라우팅 이동 정상, 더미 화면 4~5개 렌더링
- Mock 데이터로 컴포넌트 렌더링 가능

---

## Phase 1 — 인증/사용자 & 지역설정 (REQ-01, REQ-02의 기반)

### Back: 인증/사용자(REQ-01)
**필요 기술**
- Spring Security
- JWT(권장) 또는 Session(단순)
- BCryptPasswordEncoder
- Validation

**필수 기능**
- 회원가입/로그인/로그아웃
- 내 정보 조회
- 내 주소 CRUD (대표 주소 1개 지정)

**필수 API**
- `POST /api/auth/signup`
- `POST /api/auth/login` → JWT 발급
- `GET /api/users/me`
- `POST /api/users/me/addresses`
- `GET /api/users/me/addresses`
- `PUT /api/users/me/addresses/{id}`
- `PATCH /api/users/me/addresses/{id}/primary`

**검수 기준**
- 이메일 중복 불가
- 로그인 후 토큰으로 보호 API 접근 가능
- 대표 주소 지정 시 1개만 유지

**리스크 & 대안**
- JWT 갱신(Refresh Token)은 MVP에서 생략 가능(만료 짧게 + 재로그인)

---

### Front: 로그인/온보딩(REQ-01, REQ-02 기반)
**필요 기술**
- Form handling: React Hook Form(권장)
- React Query로 API 캐싱
- 토큰 저장: httpOnly 쿠키(권장) 또는 localStorage(간단)

**필수 화면**
- 회원가입/로그인 화면
- 지역 설정(구/町/丁目/번지 입력) 온보딩
- 내 주소 목록/대표 변경 UI(간단)

**검수 기준**
- 가입→로그인→주소 등록→대표 지정까지 UX 흐름 완성

---

## Phase 2 — 지역 기반 수거 캘린더(REQ-02) 핵심 완성

### Back: 수거 규칙 조회 + 캘린더 이벤트 생성
**필요 기술**
- JPA + 인덱싱(areas 탐색)
- 날짜 계산 로직(핵심)
- DTO 설계(FullCalendar event format 대응)

**필수 데이터**
- `areas` + `collection_rules` Seed(오타구 일부라도)
- 최소: 오타구 1~2개 동/町, 1~2주차 패턴 포함

**필수 기능**
1) 사용자의 주소로 해당 `area`를 찾기
2) `collection_rules`를 조회하기
3) 특정 기간(from~to)의 **실제 날짜 이벤트**로 변환해서 내려주기

**필수 API**
- `GET /api/collection/rules?addressId=...`
- `GET /api/collection/calendar?addressId=...&from=YYYY-MM-DD&to=YYYY-MM-DD`
    - 응답 예:
        - `[{ date: "2026-01-22", wasteType: "BURNABLE" }, ...]`

**날짜 변환 로직(백엔드 권장 이유)**
- 프론트에서 처리하면:
    - 사용자 디바이스/타임존/로케일 차이로 버그가 늘어남
    - 규칙이 복잡해질수록 프론트가 비대해짐
- 백엔드에서 처리하면:
    - 규칙 해석이 한 군데에 모여 유지보수 쉬움
    - 테스트로 신뢰도 확보 가능

**검수 기준**
- “2,4주 금” 규칙이 달력에서 정확한 날짜로 표시
- 주소를 바꾸면 캘린더 이벤트가 즉시 변경

---

### Front: 캘린더 UI + 필터
**필요 기술**
- FullCalendar
- wasteType별 아이콘/라벨 매핑
- 모바일 UI: 월/주 보기 전환, 오늘로 이동

**필수 화면/기능**
- 대표 주소 기준 캘린더 렌더링
- wasteType 필터(가연/불연/플라/자원)

**검수 기준**
- 대표 주소 변경 시 캘린더 재요청/재렌더링
- 월 전환 시 from/to가 바뀌며 API 호출

---

## Phase 3 — 품목 안내/검색(REQ-03, REQ-04) (수정 부분)
자기 위치(구) 기반 필터링 기능을 백엔드와 프론트에 추가

### Back: 품목 사전 + 검색
**필수 API (추가사항)**
   - `GET /api/items/search?q=...&ward=...` (추가: 사용자 위치의 '구' 정보를 파라미터로 받아 해당 지역의 특이사항이나 분류 우선순위 반영)
   
**필요 기술**
- JPA + 인덱스
- (가능하면) Full Text Search(Postgres tsvector 등) / 초기에는 LIKE 검색으로 시작

**필수 데이터**
- `items` + `item_aliases` Seed(최소 200~500개부터 시작 가능)
- 한국어 <-> 일본어 변환 흔들림 대응

**필수 API**
- `GET /api/items/search?q=...`
- `GET /api/items/{id}`

**검수 기준**
- ~~하지 않음:동일 의미 검색어(별칭)로도 결과 노출~~
- 응답에 `wasteType`, `description`, `주의사항` 포함

---

### Front: 검색 UX
**필요 기술**
- Debounce 검색(입력 300ms 후 호출)
- ~~하지 않음: 최근 검색어 저장(local)~~
- 결과 없음(empty state) UX

**필수 화면/기능**
- 상단 검색창 + 추천 키워드(자주 버리는 것) + `지역 기반 자동 필터 (내 주소의 '구' 내 물품 검색)`
- 결과 리스트(아이콘 + 분류 + 간단 요약)
- 상세 화면(배출 방법/주의사항/관련 링크)

**검수 기준**
- 입력 즉시 “너무 잦은 호출” 없이 자연스러운 검색 경험
- 결과 클릭 시 상세로 이동

---

## Phase 4 — 나눔 커뮤니티(REQ-08) → 채팅(REQ-09) → 지도(REQ-10)

### 4-A) 나눔 게시판(REQ-08)

#### Back: 게시판 CRUD + 이미지 업로드
**필요 기술**
- 게시글 CRUD
- 이미지 저장: 1안) S3 2안) Cloudinary 3안) 로컬/MinIO(개발용)
- 권한 체크(작성자만 수정/삭제)

**필수 API**
- `POST /api/share-posts` (multipart: 텍스트 + 이미지)
- `GET /api/share-posts?ward=&status=&page=`
- `GET /api/share-posts/{id}`
- `PUT /api/share-posts/{id}`
- `DELETE /api/share-posts/{id}` (soft delete)
- `POST /api/uploads/images` (옵션: 업로드 분리 시)

**검수 기준**
- 게시글 등록/조회/수정/삭제 정상
- 이미지 1~5장 업로드 가능, 썸네일 노출

#### Front: 게시판 화면
**필요 기술**
- 리스트 무한스크롤(옵션) 또는 페이지네이션
- 이미지 프리뷰/업로드 UI
- 지역 필터(대표 주소 기반 자동 필터)

**필수 화면/기능**
- 게시글 리스트(상태 OPEN/RESERVED/COMPLETED)
- 글쓰기(사진 첨부)
- 상세 화면(연락 버튼 → 채팅 준비)
- 내 글 관리(옵션)

**검수 기준**
- 모바일에서 글쓰기 UX 불편함 최소화(사진/입력)

---

### 4-B) 1:1 채팅(REQ-09) (수정 부분)
복잡도를 낮추기 위해 사진 전송 기능을 우선 순위에서 제외했습니다.

#### Back: WebSocket 또는 Firebase 선택
**추천 선택 기준**
- 팀이 Spring만으로 끝내고 싶다: **WebSocket(STOMP)**
- 실시간/푸시/확장 쉽게 가고 싶다: **Firebase**

**(WebSocket 선택 시) 필요 기술**
- Spring WebSocket + STOMP
- 메시지 저장(chat_messages)
- 인증 토큰 기반 접속

**필수 기능**
- 방 생성/조회
- 메시지 전송/수신 (제한: 초기 버전에서는 텍스트 메시지만 지원, 사진 기능은 우선 삭제)
- 채팅방 목록(마지막 메시지/시간)

**검수 기준**
- 상대방이 실시간 수신
- 새로고침 후에도 대화 기록 조회 가능

#### Front: 채팅 UI
**필요 기술**
- WebSocket client
- 스크롤 하단 고정/새 메시지 표시
- 이미지 메시지(후순위)

**필수 화면/기능**
- 이미지 메시지(후순위)~~ → 삭제: 초기 MVP에서 제외

**검수 기준**
- 메시지 지연/중복 없이 대화 가능

---

### 4-C) 위치 기반 나눔 지도(REQ-10)

#### Back
- `share_posts`에 `lat/lng` 저장
- `GET /api/share-posts/nearby?lat=&lng=&radiusKm=` 제공

#### Front: 지도 SDK 연동(Google Maps/Leaflet)

**필수 화면/기능**
- 핀(Pin) 커스텀 렌더링: 기본 마커 대신 아이콘 등을 저장해서 가져오는 형식으로 디자인을 확실히 다르게 구현
- 핀 클릭 시 미리보기 카드 + 상세 이동

**검수 기준**
- 내 주변 반경 내 게시글만 지도에 표시

---

## Phase 5 — 알림(REQ-11, REQ-12)

### Back: 스케줄러 + 발송 로그
**필요 기술**
- Quartz 또는 Spring Scheduler
- Web Push API 또는 FCM
- 발송 로그(`notifications`)

**필수 기능**
- 수거일 알림: 전날 저녁/당일 아침
- 키워드 알림: 게시글 생성 시 매칭 트리거

**검수 기준**
- 설정된 시간에 발송
- 로그에 남고 읽음 처리 가능

### Front: 알림 설정/리스트
- 알림 권한 요청 플로우
- 설정 화면(시간 선택, on/off)
- 알림 목록(미읽음 강조)

---

## 2. 프론트/백이 “같이” 맞춰야 하는 계약(실패가 잦은 포인트)

### 2.1 WasteType 코드 표준(강제)
- 백엔드 enum: `BURNABLE`, `NON_BURNABLE`, `PLASTIC`, `CAN_BOTTLE`, `PAPER`
- 프론트는 이 값을 기준으로:
    - 라벨(한/일/영), 아이콘, 색상(선택)을 매핑

### 2.2 캘린더 이벤트 응답 포맷
FullCalendar에 바로 먹히는 형태로 합의하면 프론트 작업량이 줄어듭니다.
- 예: `{ id, title, start, allDay, extendedProps: { wasteType } }`

### 2.3 에러 코드 규약
- `AUTH_001`(토큰 만료), `AREA_404`(지역 매칭 실패) 등
- 프론트는 코드에 따라 UX를 다르게 처리 가능

---

## 3. 병렬 개발 전략(주니어 팀 일정 지키는 핵심)

### 3.1 Mock API 먼저
- Swagger 스펙이 나오면 프론트는 MSW(Mock Service Worker)로 병렬 구현
- 백엔드는 API 완성 후 MSW 제거

### 3.2 Seed 데이터 자동화
- 엑셀을 수동으로 DB에 넣으면 반드시 삐끗합니다.
- “CSV Export → Import 스크립트 → 재실행 가능”이 정답

### 3.3 QA 체크리스트 운영
- 각 Phase별로 “검수 기준”을 Notion/이슈 템플릿으로 관리
- 릴리즈 전 확인 항목을 최소 10개로 고정

---

## 4. 기능별 권장 담당 분배(예시)

- Backend A: Auth/User/Address + Security
- Backend B: Area/CollectionRule/Calendar API + Seed
- Front A: Auth/Onboarding/Settings
- Front B: Calendar UI + Item Search UI
- 공동: Share Posts(게시판) CRUD, 이미지 업로드 파이프라인

---

## 5. “초기부터 넣으면 망가지는” 요소(후순위로 밀기)

- 완벽한 다국어(i18n) 전체 적용
  ~~지도에서 사진 핀(커스텀 렌더링) 고도화~~ (→ 지도 핀 디자인 차별화는 필수 구현으로 상향 조정)
- Refresh Token + 다중 디바이스 세션 관리

> 먼저 “지역→캘린더”, “검색→가이드”, “나눔 게시판” 3개를 확실히 만들고 확장합니다.

---

## 6. 최종 산출물(Phase별 데모 기준)

- Phase 1 데모: 로그인 + 주소 등록/대표 설정
- Phase 2 데모: 주소 기반 캘린더(월 전환, 필터)
- Phase 3 데모: 품목 검색(별칭 포함) + 상세 안내
- Phase 4 데모: 나눔 게시글 + 이미지 + 지역 필터
- Phase 5 데모: 알림 설정 + 발송 로그 확인

---

## 부록: 각 Phase에 필요한 체크리스트(요약)

### Backend 체크리스트
- [ ] Flyway로 테이블 버전 관리
- [ ] Swagger로 API 계약 공개
- [ ] 캘린더 규칙 해석 테스트 작성
- [ ] 업로드 URL 저장 방식 확정(S3/Cloudinary)

### Frontend 체크리스트
- [ ] React Query로 API 상태 표준화
- [ ] FullCalendar 연동(월/주)
- [ ] 검색 Debounce + Empty State
- [ ] 게시판 이미지 업로드 UX

---

원하면 다음 문서를 추가로 만들어 줄 수 있습니다.
- “Phase 2 캘린더 규칙 해석 로직” 상세 설계서(주차/요일/예외 처리)
- “API 명세서(요청/응답 JSON 샘플)” 전체 버전
- “작업 티켓(이슈) 템플릿” REQ-ID 기반으로 바로 쪼개진 형태
