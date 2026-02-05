# Postman API 테스트 가이드

## 기본 설정
- **Base URL**: `http://localhost:8080`
- **Content-Type**: `application/json` (모든 요청에 자동 설정)

---

## 1. 인증 API (`/api/auth`)

### 1-1. 회원가입
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/auth/signup`
- **Headers**: 
  - `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "email": "test@example.com",
  "password": "password123",
  "nickname": "테스트유저"
}
```
- **예상 응답** (200 OK):
```json
{
  "data": "회원가입이 완료되었습니다.",
  "meta": {
    "timestamp": "2026-01-15T10:30:00"
  }
}
```

### 1-2. 로그인
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/auth/login`
- **Headers**: 
  - `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "email": "test@example.com",
  "password": "password123"
}
```
- **예상 응답** (200 OK):
```json
{
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1
  },
  "meta": {
    "timestamp": "2026-01-15T10:30:00"
  }
```
- **중요**: 응답에서 `accessToken`을 복사해서 다음 인증이 필요한 API에 사용하세요!

---

## 2. 주소 관리 API (`/api/user-addresses`)

### 2-1. 주소 등록/수정
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/user-addresses`
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {accessToken}` ← 로그인에서 받은 토큰 입력
- **Body** (raw JSON):
```json
{
  "prefecture": "도쿄도",
  "ward": "아라카와",
  "town": "히가시닛포리",
  "chome": "6",
  "banchi": "22-24",
  "lat": 35.7325,
  "lng": 139.7733,
  "isPrimary": true
}
```
- **예상 응답** (200 OK):
```json
{
  "id": 1,
  "fullAddress": "도쿄도 아라카와 히가시닛포리 6 22-24",
  "isPrimary": true,
  "areaId": 123
}
```

### 2-2. 주소 조회
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/user-addresses`
- **Headers**: 
  - `Authorization: Bearer {accessToken}` ← 로그인에서 받은 토큰 입력
- **Body**: 없음
- **예상 응답** (200 OK):
```json
[
  {
    "id": 1,
    "fullAddress": "도쿄도 아라카와 히가시닛포리 6 22-24",
    "isPrimary": true,
    "areaId": 123
  }
]
```

---

## 3. 수거 일정 API (`/api/schedules`)

### 3-1. 월간 수거 일정 조회
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/schedules?year=2026&month=1`
- **Query Parameters** (선택사항):
  - `year`: 연도 (예: 2026) - 없으면 현재 연도
  - `month`: 월 (예: 1) - 없으면 현재 월
- **Headers**: 
  - `Authorization: Bearer {accessToken}` ← 로그인에서 받은 토큰 입력
- **Body**: 없음
- **예상 응답** (200 OK):
```json
[
  {
    "date": "2026-01-15",
    "wasteName": "타는 쓰레기",
    "wasteType": "BURNABLE",
    "note": "연말연시 휴무"
  },
  {
    "date": "2026-01-20",
    "wasteName": "재활용품",
    "wasteType": "RECYCLABLE",
    "note": null
  }
]
```

---

## Postman 사용 팁

### 1. 환경 변수 설정 (권장)
Postman에서 Environment를 만들어서 토큰을 자동으로 관리하세요:

1. Postman 우측 상단에서 "Environments" 클릭
2. "+" 버튼으로 새 Environment 생성
3. Variables 추가:
   - `baseUrl`: `http://localhost:8080`
   - `accessToken`: (로그인 후 수동으로 업데이트)
4. URL에 `{{baseUrl}}/api/auth/login` 형식으로 사용
5. Authorization 헤더에 `Bearer {{accessToken}}` 사용

### 2. 테스트 순서
1. 먼저 회원가입 (`POST /api/auth/signup`)
2. 로그인 (`POST /api/auth/login`) → `accessToken` 저장
3. 주소 등록 (`POST /api/user-addresses`) → 토큰 필요
4. 주소 조회 (`GET /api/user-addresses`) → 토큰 필요
5. 일정 조회 (`GET /api/schedules`) → 토큰 필요

### 3. 현재 보안 설정
- **현재는 모든 API가 인증 없이 접근 가능합니다** (테스트용)
- SecurityConfig에서 `permitAll()` 설정되어 있음
- 하지만 JWT 토큰을 보내도 정상 작동하므로, 프론트엔드 개발 시 토큰을 포함하는 것을 권장합니다

---

## 에러 응답 예시

### 400 Bad Request (유효성 검사 실패)
```json
{
  "timestamp": "2026-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "이메일은 필수입니다."
}
```

### 401 Unauthorized (인증 실패)
```json
{
  "timestamp": "2026-01-15T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "로그인 정보가 없습니다."
}
```

### 404 Not Found
```json
{
  "timestamp": "2026-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "사용자를 찾을 수 없습니다."
}
```

