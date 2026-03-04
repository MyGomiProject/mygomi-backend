# 내 담당 로직 암기마스터 (신고게시판 포함)

목표: 아래 5가지를 면접/인수인계 때 끊김 없이 말할 수 있게 만드는 암기본

1. 회원가입/로그인 (JWT)
2. 사용자 주소 기반 지도 API
3. 게시글 저장/수정/삭제 + 사진 저장
4. 지도 API와 게시판 데이터 연결
5. 신고게시판(관리자 처리 포함)

---

## A. 30초 전체 스크립트

`회원가입/로그인으로 JWT 발급 -> JWT에서 사용자 식별 -> 사용자의 대표 주소(lat/lng) 기반으로 주변 게시글 조회 -> 게시글은 CRUD와 이미지 업로드를 지원 -> 신고는 reports에 쌓고, 게시글 DTO에 신고여부를 집계해서 보여주며, 관리자는 기각 또는 게시글 soft delete + 신고 일괄 처리한다.`

---

## B. 기능별 암기 포인트

## 1) 회원가입/로그인 (JWT)

### 핵심 흐름

- `POST /api/auth/signup`
  - 이메일 중복 체크
  - 비밀번호 BCrypt 암호화
  - 기본 권한 `Role.USER` 저장

- `POST /api/auth/login`
  - `AuthenticationManagerBuilder`로 인증
  - `JwtTokenProvider.generateToken(authentication)`으로 Access Token 발급
  - 응답: `accessToken + userId`

### JWT 동작

- 토큰 claim에 `auth`(권한) 저장
- `JwtAuthenticationFilter`가 `Authorization: Bearer ...` 파싱
- 유효하면 `SecurityContext`에 인증 객체 주입

### 주의

- 현재 `SecurityConfig`가 `anyRequest().permitAll()`이라 API 권한이 실제로 강제되지 않음
- 즉, JWT 파싱은 되지만 접근제어는 열려 있는 상태

### 암기 문장

`회원가입은 중복체크+암호화+USER권한, 로그인은 인증 후 JWT 발급, 필터는 Bearer 토큰을 SecurityContext에 넣는다.`

---

## 2) 사용자 주소 기반 지도 API

### 주소 저장 흐름

- `POST /api/user-addresses`
  - 사용자 식별
  - 주소 문자열 정리(chome 정제 등)
  - Area 매핑
  - Geocoding으로 `lat/lng` 변환
  - 대표주소(`isPrimary`) 처리
  - 저장

### 지도 조회 흐름

- `GET /api/share-posts/nearby/me`
  - 로그인 사용자 대표주소 조회 (`AddressService.getPrimaryAddress`)
  - 대표주소 좌표를 `SharePostService.getNearbyPosts(lat,lng,...)`에 전달
  - 반경 내 게시글 반환

### 암기 문장

`대표주소를 좌표로 들고 있고, nearby/me는 내 대표주소 좌표를 기준으로 주변 게시글을 조회한다.`

---

## 3) 게시글 저장/수정/삭제 + 사진 저장

### 생성

- `POST /api/share-posts` (multipart)
  - `request`(JSON) + `images`(파일)
  - 생성 시 사용자 대표주소의 `prefecture/ward/town/lat/lng`를 게시글에 복사 저장

### 수정

- `PUT /api/share-posts/{id}`
  - 작성자 검증 후 제목/내용/카테고리 수정

### 상태 변경

- `PATCH /api/share-posts/{id}/status`
  - `OPEN/RESERVED/COMPLETED` 변경

### 삭제

- `DELETE /api/share-posts/{id}`
  - 물리삭제가 아니라 `softDelete()` -> `ShareStatus.DELETED`

### 이미지 저장

- 서버 로컬 `.../uploads/` 경로에 파일 저장
- 파일명은 `UUID + originalName`
- 게시글 이미지 URL은 `/uploads/{savedFileName}`

### 암기 문장

`게시글은 작성자 검증 후 수정/삭제되고, 삭제는 soft delete이며, 이미지는 로컬 uploads에 UUID 파일명으로 저장한다.`

---

## 4) 지도 API가 게시판 데이터를 반환하도록 연결

### 핵심 포인트

- `SharePostRepository.findNearbyPosts(...)`에서 Haversine(반경 km) 쿼리로 게시글 조회
- 조건: `status='OPEN'`
- `SharePostService.getNearbyPosts(...)`가 거리 정렬/페이지 슬라이스/DTO 변환 수행

### 즉답 템플릿

`지도용 API는 별도 지도 테이블을 쓰지 않고 share_posts를 좌표 기반으로 직접 조회해서 반환한다.`

---

## 5) 신고게시판 (내 담당 확장 포함)

### 신고 접수

- `POST /api/reports/share-posts/{postId}`
  - `ReportType.SHARE_POST`로 저장
- `POST /api/reports/info`
  - 잘못된 정보 신고 + 첨부파일(최대 10MB, 이미지/PDF)

### 관리자 조회/처리

- `GET /api/reports/admin` 목록
- `GET /api/reports/admin/{reportId}` 상세
- `PATCH /api/reports/admin/{reportId}/status` 단건 상태 변경

### 관리자 일괄 액션 (중요)

- `PATCH /api/reports/admin/share-posts/{postId}/dismiss`
  - 해당 글의 `PENDING/IN_REVIEW` 신고를 `DISMISSED` 일괄 변경
  - 의미: 문제없음(정상 복귀)

- `PATCH /api/reports/admin/share-posts/{postId}/delete`
  - 게시글 `softDelete(DELETED)`
  - 동시에 `PENDING/IN_REVIEW` 신고를 `RESOLVED` 일괄 변경
  - 의미: 문제있음(게시글 제거)

### 프론트 표시용 신고 상태

- `SharePostResponseDto`에:
  - `reported`
  - `pendingReportCount`

- 계산 기준:
  - `type=SHARE_POST`
  - `status IN (PENDING, IN_REVIEW)`

- 규칙:
  - `pendingReportCount > 0` => `reported=true`
  - `pendingReportCount == 0` => `reported=false`

### 암기 문장

`신고여부는 플래그 컬럼이 아니라 집계값이다. 기각하면 DISMISSED, 문제면 게시글 DELETED + 신고 RESOLVED다.`

---

## C. 엔드포인트만 빠르게 외우기

- Auth
  - `POST /api/auth/signup`
  - `POST /api/auth/login`

- Address
  - `POST /api/user-addresses`
  - `GET /api/user-addresses`
  - `PATCH /api/user-addresses/{addressId}/primary`
  - `DELETE /api/user-addresses/{addressId}`

- SharePost
  - `POST /api/share-posts`
  - `GET /api/share-posts/{id}`
  - `GET /api/share-posts`
  - `GET /api/share-posts/me`
  - `PUT /api/share-posts/{id}`
  - `PATCH /api/share-posts/{id}/status`
  - `DELETE /api/share-posts/{id}`
  - `GET /api/share-posts/nearby/me`

- Report
  - `POST /api/reports/share-posts/{postId}`
  - `POST /api/reports/info`
  - `GET /api/reports/admin`
  - `GET /api/reports/admin/{reportId}`
  - `PATCH /api/reports/admin/{reportId}/status`
  - `PATCH /api/reports/admin/share-posts/{postId}/dismiss`
  - `PATCH /api/reports/admin/share-posts/{postId}/delete`

---

## D. 면접/공유용 5문장 버전

1. `JWT 기반 인증을 붙여 회원가입/로그인을 구현하고, 로그인 시 access token을 발급했다.`
2. `사용자 대표 주소를 좌표로 관리해 nearby API가 내 위치 기준 게시글을 조회하도록 바꿨다.`
3. `게시글 CRUD와 이미지 업로드를 구현했고, 삭제는 soft delete로 처리했다.`
4. `지도 API가 실제 share_posts 데이터를 반경 검색(Haversine)으로 반환하도록 연결했다.`
5. `신고게시판은 신고 집계 기반 reported 표시와 관리자 일괄 처리(기각/삭제)를 구현했다.`

