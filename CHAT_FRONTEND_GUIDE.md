# 마이고미 채팅 프론트엔드 개발 가이드

이 문서는 **프론트엔드(또는 Cursor)**가 채팅 기능을 구현할 때 참고할 수 있도록 작성되었습니다.  
백엔드는 **STOMP over SockJS** 기반 WebSocket으로 실시간 채팅을 제공합니다.

---

## 1. 전체 구조 요약

| 구분 | 방식 | 용도 |
|------|------|------|
| **HTTP API** | REST | 채팅방 생성, 목록 조회, **과거 메시지** 조회 |
| **WebSocket** | STOMP + SockJS | **실시간 메시지** 수신·전송 |

- 채팅방 입장/목록/과거 메시지 → **HTTP**
- 메시지 보내기·받기(실시간) → **WebSocket**

---

## 2. HTTP API (채팅방·과거 메시지)

Base URL: `http://localhost:8080`  
모든 요청에 **Authorization: Bearer {accessToken}** 필요.

### 2.1 채팅방 생성 (또는 기존 방 참여)

```
POST /api/chat/room?sharePostId={나눔게시글ID}
```

- **응답**: `Long` — 채팅방 ID (roomId). 이 값을 WebSocket 구독·전송에 사용.

### 2.2 내 채팅방 목록 조회

```
GET /api/chat/rooms
```

- **응답** (배열):
```json
[
  {
    "roomId": 1,
    "postTitle": "이케아 의자 나눔합니다",
    "opponentNickname": "나눔천사"
  }
]
```

### 2.3 특정 채팅방 과거 메시지 조회

```
GET /api/chat/room/{roomId}/messages
```

- **응답** (배열): 아래 "실시간 수신 메시지 형식"과 동일한 객체 배열.

---

## 3. WebSocket (실시간 채팅)

### 3.1 연결 정보

| 항목 | 값 |
|------|-----|
| **프로토콜** | STOMP over SockJS |
| **연결 URL** | `http://localhost:8080/ws-stomp` |
| **인증** | 연결 시 헤더에 `Authorization: Bearer {accessToken}` |

### 3.2 주소 규칙

| 역할 | prefix | 예시 | 설명 |
|------|--------|------|------|
| **메시지 보내기** | `/pub` | `/pub/chat/message` | 클라이언트 → 서버 |
| **메시지 받기** | `/sub` | `/sub/chat/room/{roomId}` | 서버 → 클라이언트 (구독) |

- **전송 destination**: `"/pub/chat/message"`
- **구독 destination**: `"/sub/chat/room/" + roomId` (roomId는 숫자, 예: 1 → `/sub/chat/room/1`)

### 3.3 전송 메시지 형식 (클라이언트 → 서버)

destination: `"/pub/chat/message"`  
body: **JSON 문자열**

```json
{
  "roomId": 1,
  "message": "안녕하세요, 물건 아직 있나요?"
}
```

- `roomId`: number (채팅방 ID)
- `message`: string (채팅 내용)

### 3.4 실시간 수신 메시지 형식 (서버 → 클라이언트)

구독 destination: `/sub/chat/room/{roomId}`  
수신 시 **message.body**를 JSON 파싱하면 아래 형태입니다.

```json
{
  "messageId": 100,
  "senderEmail": "user@example.com",
  "senderNickname": "나눔받고싶어요",
  "message": "네, 가능합니다!",
  "sendTime": "2026-02-12 14:30"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| messageId | number | 메시지 고유 ID |
| senderEmail | string | 보낸 사람 이메일 (본인 메시지 여부 판단용) |
| senderNickname | string | 보낸 사람 닉네임 |
| message | string | 메시지 내용 |
| sendTime | string | "yyyy-MM-dd HH:mm" 형식 |

- **내 메시지 vs 상대 메시지**: 로그인 유저의 이메일과 `senderEmail`을 비교하면 됩니다.

---

## 4. 프론트엔드 구현 순서

### Step 1: 패키지 설치

```bash
npm install sockjs-client @stomp/stompjs
```

(또는 yarn/pnpm 동일)

### Step 2: 채팅방 화면 진입 시

1. **채팅방 ID 확보**
   - 나눔 게시글에서 "채팅하기" 클릭 시:  
     `POST /api/chat/room?sharePostId={게시글ID}` 호출 → 응답이 **roomId**.
   - 이미 있는 채팅방 목록에서 입장 시: 목록에 있는 **roomId** 사용.

2. **과거 메시지 표시**
   - `GET /api/chat/room/{roomId}/messages` 호출 후, 응답 배열을 화면에 렌더링.

3. **WebSocket 연결**
   - 아래 Step 3 참고.

### Step 3: WebSocket 연결 및 구독

- **연결 URL**: `http://localhost:8080/ws-stomp` (SockJS)
- **연결 헤더**: `{ Authorization: "Bearer " + accessToken }`
- **연결 성공 후**:  
  `stompClient.subscribe("/sub/chat/room/" + roomId, callback)`  
  callback에서 `message.body`를 `JSON.parse` 해서 위 "실시간 수신 메시지 형식"으로 사용.

### Step 4: 메시지 전송

- `stompClient.send("/pub/chat/message", {}, JSON.stringify({ roomId, message }))`
- `roomId`는 number, `message`는 string.

### Step 5: 채팅방 나갈 때

- `stompClient.disconnect()` 등으로 연결 해제.
- 다른 방 입장 시에는 새 roomId로 다시 연결·구독.

---

## 5. 코드 예시 (TypeScript / React 흐름)

아래는 **흐름만** 보여주는 예시입니다. 프로젝트에 맞게 URL·상태 관리 방식은 바꿔서 사용하면 됩니다.

```typescript
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

const WS_URL = "http://localhost:8080/ws-stomp";

// 1) 채팅방 입장 시: HTTP로 roomId·과거 메시지 확보 후
const roomId = 1; // POST /api/chat/room 또는 목록에서 획득
const accessToken = localStorage.getItem("accessToken");

// 2) WebSocket 연결
const socket = new SockJS(WS_URL);
const stompClient = new Client({
  webSocketFactory: () => socket,
  connectHeaders: {
    Authorization: `Bearer ${accessToken}`,
  },
  onConnect: () => {
    // 3) 구독: 이 방의 메시지 수신
    stompClient.subscribe(`/sub/chat/room/${roomId}`, (message) => {
      const data = JSON.parse(message.body);
      // data: { messageId, senderEmail, senderNickname, message, sendTime }
      setMessages((prev) => [...prev, data]);
    });
  },
});

stompClient.activate();

// 4) 메시지 전송
function sendMessage(text: string) {
  stompClient.publish({
    destination: "/pub/chat/message",
    body: JSON.stringify({ roomId, message: text }),
  });
}

// 5) 나갈 때
stompClient.deactivate();
```

- **@stomp/stompjs** 사용 시 `Client` 생성 후 `activate()` / `deactivate()` 로 연결·해제합니다.
- SockJS만 쓰고 `Stomp.over(socket)` 방식을 쓰는 경우는 프로젝트 루트 **chat-test.html** 참고.

---

## 6. 테스트용 HTML (백엔드 제공)

프로젝트 루트에 **chat-test.html** 파일이 있습니다.

- **용도**: 브라우저에서 WebSocket 채팅이 정상 동작하는지 확인.
- **사용법**:
  1. 백엔드 서버 실행 (`http://localhost:8080`).
  2. 브라우저에서 `chat-test.html` 열기.
  3. 방 번호(roomId), JWT 토큰(로그인 후 받은 accessToken) 입력 후 "입장하기".
  4. 메시지 입력 후 전송.

동작 방식이 궁금하면 이 파일의 스크립트 부분을 보면, 연결·구독·전송·수신 처리 방식을 그대로 참고할 수 있습니다.

---

## 7. 주의사항

- **CORS**: 현재 백엔드 `WebSocketConfig`에서 `setAllowedOriginPatterns("*")` 로 설정되어 있어, 개발 시 `http://localhost:3000` 등에서 연결 가능. 운영 환경에서는 필요 시 도메인 제한 권장.
- **토큰**: WebSocket 연결 시 반드시 `Authorization: Bearer {accessToken}` 를 넣어야 하며, 만료되면 연결이 거부될 수 있음.
- **채팅방 권한**: 해당 roomId에 참여한 사용자만 구독·전송할 수 있도록 백엔드에서 검사합니다. 프론트는 항상 **유효한 roomId**와 **유효한 토큰**을 사용하면 됩니다.

---

## 8. TypeScript 타입 (참고)

```typescript
// 전송 (클라이언트 → 서버)
interface ChatMessageRequest {
  roomId: number;
  message: string;
}

// 수신 (서버 → 클라이언트) & GET /api/chat/room/{roomId}/messages 응답 항목
interface ChatMessageResponse {
  messageId: number;
  senderEmail: string;
  senderNickname: string;
  message: string;
  sendTime: string;
}

// GET /api/chat/rooms 응답 항목
interface ChatRoomItem {
  roomId: number;
  postTitle: string;
  opponentNickname: string;
}
```

---

이 문서와 **chat-test.html**을 함께 참고하면, 프론트엔드에서 채팅(HTTP + WebSocket)을 동일한 규격으로 구현할 수 있습니다.
