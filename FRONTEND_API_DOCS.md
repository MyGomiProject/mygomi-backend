# 프론트엔드 API 연동 가이드

이 문서는 프론트엔드 개발자가 백엔드 API를 연동할 때 필요한 모든 정보를 포함합니다.

## 기본 정보

- **Base URL**: `http://localhost:8080`
- **인증 방식**: JWT Bearer Token
- **Content-Type**: `application/json`
- **CORS**: `http://localhost:3000`에서의 요청 허용 (프록시 설정 불필요)

---

## API 엔드포인트 목록

### 1. 인증 API

#### 1.1 회원가입
```
POST /api/auth/signup
```

**요청 Body:**
```typescript
{
  email: string;      // 이메일 (필수, 이메일 형식)
  password: string;   // 비밀번호 (필수)
  nickname: string;   // 닉네임 (필수)
}
```

**응답:**
```typescript
{
  data: string;       // "회원가입이 완료되었습니다."
  meta: {
    timestamp: string; // ISO 8601 형식
  }
}
```

**TypeScript 인터페이스:**
```typescript
interface SignupRequest {
  email: string;
  password: string;
  nickname: string;
}

interface CommonResponse<T> {
  data: T;
  meta: {
    timestamp: string;
  };
}
```

**사용 예시:**
```typescript
const response = await fetch('http://localhost:8080/api/auth/signup', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    email: 'user@example.com',
    password: 'password123',
    nickname: '사용자닉네임'
  })
});

const result: CommonResponse<string> = await response.json();
```

---

#### 1.2 로그인
```
POST /api/auth/login
```

**요청 Body:**
```typescript
{
  email: string;
  password: string;
}
```

**응답:**
```typescript
{
  data: {
    accessToken: string;  // JWT 토큰
    userId: number;       // 사용자 ID
  };
  meta: {
    timestamp: string;
  };
}
```

**TypeScript 인터페이스:**
```typescript
interface LoginRequest {
  email: string;
  password: string;
}

interface TokenResponse {
  accessToken: string;
  userId: number;
}

interface LoginResponse extends CommonResponse<TokenResponse> {}
```

**사용 예시:**
```typescript
const response = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    email: 'user@example.com',
    password: 'password123'
  })
});

const result: LoginResponse = await response.json();

// 토큰을 localStorage나 상태 관리에 저장
localStorage.setItem('accessToken', result.data.accessToken);
localStorage.setItem('userId', result.data.userId.toString());
```

---

### 2. 주소 관리 API

#### 2.1 주소 등록/수정
```
POST /api/user-addresses
```

**인증 필요**: ✅ (JWT Token)

**요청 Headers:**
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**요청 Body:**
```typescript
{
  prefecture: string;  // 도/부/현 (예: "도쿄도")
  ward: string;        // 구/시 (예: "아라카와")
  town: string;        // 동/정 (예: "히가시닛포리")
  chome: string;       // 쵸메 (예: "6")
  banchi: string;      // 번지/건물명 (예: "22-24")
  lat: number;         // 위도
  lng: number;         // 경도
  isPrimary: boolean;  // 대표 주소 여부
}
```

**응답:**
```typescript
{
  id: number;
  fullAddress: string;  // 전체 주소 문자열
  isPrimary: boolean;
  areaId: number | null; // 매칭된 수거 지역 ID
}
```

**TypeScript 인터페이스:**
```typescript
interface AddressRequest {
  prefecture: string;
  ward: string;
  town: string;
  chome: string;
  banchi: string;
  lat: number;
  lng: number;
  isPrimary: boolean;
}

interface AddressResponse {
  id: number;
  fullAddress: string;
  isPrimary: boolean;
  areaId: number | null;
}
```

**사용 예시:**
```typescript
const accessToken = localStorage.getItem('accessToken');

const response = await fetch('http://localhost:8080/api/user-addresses', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${accessToken}`
  },
  body: JSON.stringify({
    prefecture: '도쿄도',
    ward: '아라카와',
    town: '히가시닛포리',
    chome: '6',
    banchi: '22-24',
    lat: 35.7325,
    lng: 139.7733,
    isPrimary: true
  })
});

const result: AddressResponse = await response.json();
```

---

#### 2.2 주소 조회
```
GET /api/user-addresses
```

**인증 필요**: ✅ (JWT Token)

**요청 Headers:**
```
Authorization: Bearer {accessToken}
```

**응답:**
```typescript
AddressResponse[]  // 주소 배열
```

**사용 예시:**
```typescript
const accessToken = localStorage.getItem('accessToken');

const response = await fetch('http://localhost:8080/api/user-addresses', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${accessToken}`
  }
});

const addresses: AddressResponse[] = await response.json();
```

---

### 3. 수거 일정 API

#### 3.1 월간 수거 일정 조회
```
GET /api/schedules?year={year}&month={month}
```

**인증 필요**: ✅ (JWT Token)

**Query Parameters:**
- `year` (선택): 연도 (예: 2026) - 없으면 현재 연도
- `month` (선택): 월 (예: 1) - 없으면 현재 월

**요청 Headers:**
```
Authorization: Bearer {accessToken}
```

**응답:**
```typescript
{
  date: string;        // 날짜 (ISO 8601 형식, 예: "2026-01-15")
  wasteName: string;   // 쓰레기 이름 (예: "타는 쓰레기")
  wasteType: string;   // 쓰레기 타입 코드 (예: "BURNABLE")
  note: string | null; // 비고 (예: "연말연시 휴무")
}[]
```

**TypeScript 인터페이스:**
```typescript
interface ScheduleResponse {
  date: string;        // "YYYY-MM-DD" 형식
  wasteName: string;
  wasteType: string;   // "BURNABLE" | "RECYCLABLE" | "NON_BURNABLE" | "LARGE" 등
  note: string | null;
}
```

**사용 예시:**
```typescript
const accessToken = localStorage.getItem('accessToken');
const year = 2026;
const month = 1;

const response = await fetch(
  `http://localhost:8080/api/schedules?year=${year}&month=${month}`,
  {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${accessToken}`
    }
  }
);

const schedules: ScheduleResponse[] = await response.json();
```

---

### 4. 사용자 정보 API

#### 4.1 현재 로그인한 사용자 정보 조회
```
GET /api/users/me
```

**인증 필요**: ✅ (JWT Token)

**요청 Headers:**
```
Authorization: Bearer {accessToken}
```

**응답:**
```typescript
{
  id: number;              // 사용자 ID
  email: string;           // 이메일
  nickname: string;        // 닉네임
  role: string;           // 역할 ("USER" | "ADMIN")
  createdAt: string;      // 가입일시 (ISO 8601 형식)
  addresses: AddressResponse[];  // 사용자 주소 목록
}
```

**TypeScript 인터페이스:**
```typescript
interface UserResponse {
  id: number;
  email: string;
  nickname: string;
  role: "USER" | "ADMIN";
  createdAt: string;  // ISO 8601 형식
  addresses: AddressResponse[];  // 사용자 주소 목록
}
```

**사용 예시:**
```typescript
const accessToken = localStorage.getItem('accessToken');

const response = await fetch('http://localhost:8080/api/users/me', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${accessToken}`
  }
});

const user: UserResponse = await response.json();
```

---

## 공통 유틸리티 함수 예시

### API 클라이언트 래퍼
```typescript
// api/client.ts
const BASE_URL = 'http://localhost:8080';

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

  const response = await fetch(`${BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    throw new Error(`API Error: ${response.status}`);
  }

  return response.json();
}

// 사용 예시
export const authApi = {
  signup: (data: SignupRequest) =>
    apiRequest<CommonResponse<string>>('/api/auth/signup', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  login: (data: LoginRequest) =>
    apiRequest<LoginResponse>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
};

export const addressApi = {
  getAddresses: () =>
    apiRequest<AddressResponse[]>('/api/user-addresses'),

  setAddress: (data: AddressRequest) =>
    apiRequest<AddressResponse>('/api/user-addresses', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
};

export const scheduleApi = {
  getSchedules: (year?: number, month?: number) => {
    const params = new URLSearchParams();
    if (year) params.append('year', year.toString());
    if (month) params.append('month', month.toString());
    
    const query = params.toString();
    return apiRequest<ScheduleResponse[]>(
      `/api/schedules${query ? `?${query}` : ''}`
    );
  },
};

export const userApi = {
  getCurrentUser: () =>
    apiRequest<UserResponse>('/api/users/me'),
};
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
}
```

### 에러 처리 예시
```typescript
try {
  const response = await fetch('http://localhost:8080/api/user-addresses', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  if (!response.ok) {
    const error: ErrorResponse = await response.json();
    throw new Error(error.message);
  }

  const data = await response.json();
  return data;
} catch (error) {
  console.error('API Error:', error);
  // 에러 처리 로직
}
```

---

## 주의사항

1. **토큰 저장**: 로그인 후 받은 `accessToken`을 안전하게 저장하세요 (localStorage, sessionStorage, 또는 상태 관리 라이브러리)

2. **토큰 갱신**: 현재 토큰 만료 시간은 24시간입니다. 만료 시 다시 로그인해야 합니다.

3. **CORS**: 백엔드에서 `http://localhost:3000`에서의 요청을 허용하도록 설정되어 있습니다. 프록시 설정 없이 직접 API를 호출할 수 있습니다.

4. **인증 헤더**: 인증이 필요한 API는 반드시 `Authorization: Bearer {token}` 헤더를 포함해야 합니다.

5. **날짜 형식**: 일정 API의 `date` 필드는 `YYYY-MM-DD` 형식의 문자열입니다.

---

## 테스트 체크리스트

- [ ] 회원가입 API 호출 성공
- [ ] 로그인 API 호출 성공 및 토큰 저장
- [ ] 사용자 정보 조회 API 호출 성공 (토큰 포함)
- [ ] 주소 등록 API 호출 성공 (토큰 포함)
- [ ] 주소 조회 API 호출 성공 (토큰 포함)
- [ ] 일정 조회 API 호출 성공 (토큰 포함)
- [ ] 에러 처리 동작 확인
- [ ] 토큰 없이 인증 필요 API 호출 시 에러 처리 확인

