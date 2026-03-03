# MyGomi Backend API 참조 문서

프론트엔드 개발자를 위한 백엔드 API 전체 목록 및 사용 가이드입니다.

## 📋 목차

1. [기본 정보](#기본-정보)
2. [인증 API](#1-인증-api)
3. [사용자 정보 API](#2-사용자-정보-api)
4. [주소 관리 API](#3-주소-관리-api)
5. [수거 일정 API](#4-수거-일정-api)
6. [TypeScript 인터페이스](#typescript-인터페이스)
7. [에러 처리](#에러-처리)

---

## 기본 정보

- **Base URL**: `http://localhost:8080`
- **인증 방식**: JWT Bearer Token
- **Content-Type**: `application/json`
- **CORS**: `http://localhost:3000` 허용

### 인증 헤더 형식
```
Authorization: Bearer {accessToken}
```

---

## 1. 인증 API

### 1.1 회원가입

**엔드포인트**
```
POST /api/auth/signup
```

**인증 필요**: ❌

**요청 Body**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "사용자닉네임"
}
```

**응답 (200 OK)**
```json
{
  "data": "회원가입이 완료되었습니다.",
  "meta": {
    "timestamp": "2026-01-15T10:30:00"
  }
}
```

**에러 응답**
- `400 Bad Request`: 유효성 검사 실패 (이메일 형식 오류, 필수 필드 누락 등)

---

### 1.2 로그인

**엔드포인트**
```
POST /api/auth/login
```

**인증 필요**: ❌

**요청 Body**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**응답 (200 OK)**
```json
{
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1
  },
  "meta": {
    "timestamp": "2026-01-15T10:30:00"
  }
}
```

**에러 응답**
- `401 Unauthorized`: 이메일 또는 비밀번호가 일치하지 않음

**중요**: 응답의 `accessToken`을 저장하여 이후 인증이 필요한 API 호출 시 사용하세요.

---

## 2. 사용자 정보 API

### 2.1 현재 사용자 정보 조회

**엔드포인트**
```
GET /api/users/me
```

**인증 필요**: ✅

**요청 Headers**
```
Authorization: Bearer {accessToken}
```

**응답 (200 OK)**
```json
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "사용자닉네임",
  "role": "USER",
  "createdAt": "2026-01-15T10:30:00",
  "addresses": [
    {
      "id": 1,
      "fullAddress": "도쿄도 아라카와 히가시닛포리 6 22-24",
      "isPrimary": true,
      "areaId": 123,
      "lat": 35.7325,
      "lng": 139.7733
    }
  ]
}
```

**에러 응답**
- `401 Unauthorized`: 토큰이 없거나 유효하지 않음

---

### 2.2 닉네임 수정

**엔드포인트**
```
PATCH /api/users/me/nickname
```

**인증 필요**: ✅

**요청 Headers**
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**요청 Body**
```json
{
  "nickname": "새로운닉네임"
}
```

**응답 (200 OK)**
```json
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "새로운닉네임",
  "role": "USER",
  "createdAt": "2026-01-15T10:30:00",
  "addresses": [...]
}
```

**에러 응답**
- `400 Bad Request`: 닉네임이 비어있음
- `401 Unauthorized`: 토큰이 없거나 유효하지 않음

---

### 2.3 비밀번호 변경

**엔드포인트**
```
PATCH /api/users/me/password
```

**인증 필요**: ✅

**요청 Headers**
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**요청 Body**
```json
{
  "currentPassword": "oldPassword123",
  "newPassword": "newPassword456"
}
```

**응답 (200 OK)**
```json
"비밀번호가 변경되었습니다."
```

**에러 응답**
- `400 Bad Request`: 현재 비밀번호 또는 새 비밀번호가 비어있음
- `401 Unauthorized`: 현재 비밀번호가 일치하지 않음 또는 토큰이 유효하지 않음

---

## 3. 주소 관리 API

### 3.1 주소 등록/수정

**엔드포인트**
```
POST /api/user-addresses
```

**인증 필요**: ✅

**요청 Headers**
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**요청 Body**
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

**응답 (200 OK)**
```json
{
  "id": 1,
  "fullAddress": "도쿄도 아라카와 히가시닛포리 6 22-24",
  "isPrimary": true,
  "areaId": 123,
  "lat": 35.7325,
  "lng": 139.7733
}
```

**에러 응답**
- `401 Unauthorized`: 토큰이 없거나 유효하지 않음

**참고**: 최초 등록이면 생성, 이미 있으면 수정됩니다.

---

### 3.2 주소 조회

**엔드포인트**
```
GET /api/user-addresses
```

**인증 필요**: ✅

**요청 Headers**
```
Authorization: Bearer {accessToken}
```

**응답 (200 OK)**
```json
[
  {
    "id": 1,
    "fullAddress": "도쿄도 아라카와 히가시닛포리 6 22-24",
    "isPrimary": true,
    "areaId": 123,
    "lat": 35.7325,
    "lng": 139.7733
  },
  {
    "id": 2,
    "fullAddress": "도쿄도 시부야 시부야 1 2-3",
    "isPrimary": false,
    "areaId": 456,
    "lat": 35.6580,
    "lng": 139.7016
  }
]
```

**에러 응답**
- `401 Unauthorized`: 토큰이 없거나 유효하지 않음

---

### 3.3 대표 주소 변경

**엔드포인트**
```
PATCH /api/user-addresses/{addressId}/primary
```

**인증 필요**: ✅

**요청 Headers**
```
Authorization: Bearer {accessToken}
```

**Path Parameters**
- `addressId` (Long): 대표 주소로 설정할 주소 ID

**응답 (200 OK)**
```json
{
  "id": 2,
  "fullAddress": "도쿄도 시부야 시부야 1 2-3",
  "isPrimary": true,
  "areaId": 456,
  "lat": 35.6580,
  "lng": 139.7016
}
```

**에러 응답**
- `401 Unauthorized`: 토큰이 없거나 유효하지 않음
- `404 Not Found`: 주소를 찾을 수 없음

---

### 3.4 주소 삭제

**엔드포인트**
```
DELETE /api/user-addresses/{addressId}
```

**인증 필요**: ✅

**요청 Headers**
```
Authorization: Bearer {accessToken}
```

**Path Parameters**
- `addressId` (Long): 삭제할 주소 ID

**응답 (200 OK)**
```json
"주소가 삭제되었습니다."
```

**에러 응답**
- `400 Bad Request`: 대표 주소가 1개만 남은 경우 삭제 불가
- `401 Unauthorized`: 토큰이 없거나 유효하지 않음
- `404 Not Found`: 주소를 찾을 수 없음

---

## 4. 수거 일정 API

### 4.1 월간 수거 일정 조회

**엔드포인트**
```
GET /api/schedules?year={year}&month={month}
```

**인증 필요**: ✅

**요청 Headers**
```
Authorization: Bearer {accessToken}
```

**Query Parameters** (선택사항)
- `year` (Integer): 조회할 연도 (기본값: 현재 연도)
- `month` (Integer): 조회할 월 (기본값: 현재 월)

**예시**
```
GET /api/schedules?year=2026&month=1
GET /api/schedules  (현재 년/월 조회)
```

**응답 (200 OK)**
```json
{
  "data": [
    {
      "id": "1",
      "title": "가연성 쓰레기",
      "start": "2026-01-15",
      "allDay": true,
      "extendedProps": {
        "wasteType": "BURNABLE"
      }
    },
    {
      "id": "2",
      "title": "플라스틱",
      "start": "2026-01-20",
      "allDay": true,
      "extendedProps": {
        "wasteType": "PLASTIC"
      }
    }
  ],
  "meta": {
    "timestamp": "2026-01-15T10:30:00"
  }
}
```

**에러 응답**
- `401 Unauthorized`: 토큰이 없거나 유효하지 않음

**참고**: FullCalendar 형식으로 반환됩니다.

---

## TypeScript 인터페이스

### 공통 타입

```typescript
// 공통 응답 형식
interface CommonResponse<T> {
  data: T;
  meta: {
    timestamp: string;
  };
}

// 역할 타입
type Role = "USER" | "ADMIN";
```

### 인증 관련

```typescript
// 회원가입 요청
interface SignupRequest {
  email: string;
  password: string;
  nickname: string;
}

// 로그인 요청
interface LoginRequest {
  email: string;
  password: string;
}

// 토큰 응답
interface TokenResponse {
  accessToken: string;
  userId: number;
}

// 로그인 응답
interface LoginResponse extends CommonResponse<TokenResponse> {}
```

### 사용자 관련

```typescript
// 사용자 정보 응답
interface UserResponse {
  id: number;
  email: string;
  nickname: string;
  role: Role;
  createdAt: string;  // ISO 8601 형식
  addresses: AddressResponse[];
}

// 닉네임 수정 요청
interface UpdateNicknameRequest {
  nickname: string;
}

// 비밀번호 변경 요청
interface UpdatePasswordRequest {
  currentPassword: string;
  newPassword: string;
}
```

### 주소 관련

```typescript
// 주소 등록/수정 요청
interface AddressRequest {
  prefecture: string;  // 도/부/현
  ward: string;        // 구/시
  town: string;        // 동/정
  chome: string;       // 쵸메
  banchi: string;      // 번지/건물명
  lat: number;         // 위도
  lng: number;         // 경도
  isPrimary: boolean;  // 대표 주소 여부
}

// 주소 응답
interface AddressResponse {
  id: number;
  fullAddress: string;
  isPrimary: boolean;
  areaId: number | null;
  lat: number;
  lng: number;
}
```

### 일정 관련

```typescript
// 수거 일정 응답
interface ScheduleResponse {
  id: string;
  title: string;
  start: string;  // "YYYY-MM-DD" 형식
  allDay: boolean;
  extendedProps: {
    wasteType: string;  // "BURNABLE", "PLASTIC" 등
  };
}

// 일정 조회 응답
interface ScheduleListResponse extends CommonResponse<ScheduleResponse[]> {}
```

---

## 에러 처리

### 에러 응답 형식

```typescript
interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path?: string;
}
```

### HTTP 상태 코드

- `200 OK`: 요청 성공
- `400 Bad Request`: 잘못된 요청 (유효성 검사 실패 등)
- `401 Unauthorized`: 인증 실패 (토큰 없음, 만료, 잘못된 비밀번호 등)
- `404 Not Found`: 리소스를 찾을 수 없음
- `500 Internal Server Error`: 서버 오류

### 에러 처리 예시

```typescript
async function apiRequest<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const token = localStorage.getItem('accessToken');
  
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`http://localhost:8080${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const error: ErrorResponse = await response.json();
    
    // 401 에러 시 로그인 페이지로 리다이렉트
    if (response.status === 401) {
      localStorage.removeItem('accessToken');
      window.location.href = '/login';
      throw new Error('인증이 필요합니다.');
    }
    
    throw new Error(error.message || `API Error: ${response.status}`);
  }

  return response.json();
}
```

---

## API 사용 예시

### 1. 로그인 및 토큰 저장

```typescript
const login = async (email: string, password: string) => {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, password }),
  });

  if (!response.ok) {
    throw new Error('로그인 실패');
  }

  const result = await response.json();
  localStorage.setItem('accessToken', result.data.accessToken);
  localStorage.setItem('userId', result.data.userId.toString());
  
  return result.data;
};
```

### 2. 사용자 정보 조회

```typescript
const getCurrentUser = async () => {
  const token = localStorage.getItem('accessToken');
  
  const response = await fetch('http://localhost:8080/api/users/me', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error('사용자 정보 조회 실패');
  }

  return await response.json();
};
```

### 3. 주소 등록

```typescript
const addAddress = async (address: AddressRequest) => {
  const token = localStorage.getItem('accessToken');
  
  const response = await fetch('http://localhost:8080/api/user-addresses', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify(address),
  });

  if (!response.ok) {
    throw new Error('주소 등록 실패');
  }

  return await response.json();
};
```

### 4. 수거 일정 조회

```typescript
const getSchedules = async (year?: number, month?: number) => {
  const token = localStorage.getItem('accessToken');
  
  const params = new URLSearchParams();
  if (year) params.append('year', year.toString());
  if (month) params.append('month', month.toString());
  
  const query = params.toString();
  const url = `http://localhost:8080/api/schedules${query ? `?${query}` : ''}`;
  
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error('일정 조회 실패');
  }

  const result = await response.json();
  return result.data;  // CommonResponse의 data 필드
};
```

---

## 주의사항

1. **토큰 저장**: 로그인 후 받은 `accessToken`을 안전하게 저장하세요 (localStorage, sessionStorage, 또는 상태 관리 라이브러리)

2. **토큰 만료**: 현재 토큰 만료 시간은 24시간입니다. 만료 시 다시 로그인해야 합니다.

3. **CORS**: 백엔드에서 `http://localhost:3000`에서의 요청을 허용하도록 설정되어 있습니다.

4. **인증 헤더**: 인증이 필요한 API는 반드시 `Authorization: Bearer {token}` 헤더를 포함해야 합니다.

5. **날짜 형식**: 일정 API의 `start` 필드는 `YYYY-MM-DD` 형식의 문자열입니다.

6. **주소 삭제**: 대표 주소가 1개만 남은 경우 삭제할 수 없습니다.

---

## API 목록 요약

| 메서드 | 엔드포인트 | 인증 | 설명 |
|--------|-----------|------|------|
| POST | `/api/auth/signup` | ❌ | 회원가입 |
| POST | `/api/auth/login` | ❌ | 로그인 |
| GET | `/api/users/me` | ✅ | 사용자 정보 조회 |
| PATCH | `/api/users/me/nickname` | ✅ | 닉네임 수정 |
| PATCH | `/api/users/me/password` | ✅ | 비밀번호 변경 |
| POST | `/api/user-addresses` | ✅ | 주소 등록/수정 |
| GET | `/api/user-addresses` | ✅ | 주소 조회 |
| PATCH | `/api/user-addresses/{id}/primary` | ✅ | 대표 주소 변경 |
| DELETE | `/api/user-addresses/{id}` | ✅ | 주소 삭제 |
| GET | `/api/schedules` | ✅ | 수거 일정 조회 |

---

**문서 버전**: 1.0.0  
**최종 업데이트**: 2026-01-15

