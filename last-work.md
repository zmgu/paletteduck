# 마지막 작업 내용 (2025-12-04)

## 구현 완료: 방 목록 조회 + 초대코드 입력 기능

### 1. 방 목록 조회 기능

#### 백엔드

**RoomListResponse.java** (신규)
```java
public class RoomListResponse {
    private String roomId;
    private String inviteCode;
    private RoomStatus status;
    private int currentPlayers;    // 참가자 수 (관전자 제외)
    private int maxPlayers;
    private String hostNickname;
    private Integer currentRound;  // PLAYING일 때만
    private Integer totalRounds;   // PLAYING일 때만
    private Long createdAt;        // 방 생성 시간 (정렬용)
}
```

**RoomService.java**
- `getPublicRoomList()`: 공개방 목록 조회 (최신순 정렬)
- `convertToRoomListResponse()`: RoomInfo → RoomListResponse 변환
  - PLAYING 상태일 때 GameState에서 currentRound, totalRounds 조회
- 불필요한 import 제거: `JsonProcessingException`, `ObjectMapper`, `RedisTemplate`, `Duration`

**RoomController.java**
- `GET /api/room/list`: 공개방 목록 조회 엔드포인트

#### 프론트엔드

**game.types.ts**
```typescript
export interface RoomListResponse {
  roomId: string;
  inviteCode: string;
  status: RoomStatus;
  currentPlayers: number;
  maxPlayers: number;
  hostNickname: string;
  currentRound: number | null;
  totalRounds: number | null;
  createdAt: number;
}
```

**MainPage.tsx**
- "방 목록" 버튼 추가
- 방 목록 모달 구현 (리스트 형태)
- 각 방 정보 표시:
  - 방장 닉네임
  - 상태 (대기중/게임중)
  - 인원 (현재/최대)
  - 라운드 (PLAYING일 때: "라운드: 2/5")
- 방 클릭 시 입장/관전 처리

### 2. 초대코드 입력 기능

#### 백엔드

**RoomService.java**
- `findRoomByInviteCode(String inviteCode)`: 초대코드로 방 찾기

**RoomController.java**
- `POST /api/room/join-by-code`: 초대코드로 방 입장
  - Request: `{"inviteCode": "c5e64178"}`
  - Response: `{"roomId": "xxx", "inviteCode": "xxx"}`
  - 404: "초대코드에 해당하는 방을 찾을 수 없습니다."

#### 프론트엔드

**MainPage.tsx**
- "초대코드 입력" 버튼 활성화
- 초대코드 입력 모달 추가
- URL 파싱 처리:
  ```typescript
  // URL 형식인 경우 roomId 추출
  const urlMatch = code.match(/\/room\/([^/?#]+)/);
  if (urlMatch) {
    code = urlMatch[1];
  }
  ```
- 입력 가능 형식:
  - 초대코드: `c5e64178`
  - URL 전체: `http://localhost:5173/room/c5e64178`
  - 경로만: `/room/c5e64178`

**useRoomActions.ts**
- `copyInviteCode()`: URL 전체 복사 + 피드백 메시지
  ```typescript
  const url = `${window.location.origin}/room/${inviteCode}`;
  navigator.clipboard.writeText(url)
    .then(() => alert('초대 링크가 복사되었습니다!'));
  ```

### 3. Redis 중복 실행 문제 해결

#### 문제
- 로컬 WSL Redis와 Docker Redis가 동시 실행
- wslrelay.exe가 6379 포트 점유
- Spring Boot가 WSL Redis(빈 데이터)에 연결됨

#### 해결
```bash
# wslrelay 프로세스 종료
taskkill /F /PID [wslrelay의 PID]

# WSL Redis 비활성화 (영구 해결)
wsl -d Ubuntu bash -c "sudo systemctl stop redis-server"
wsl -d Ubuntu bash -c "sudo systemctl disable redis-server"
```

#### 확인 방법
```bash
# 6379 포트 사용 프로세스 확인
netstat -ano | findstr ":6379"

# Docker Redis만 실행 중이어야 함
tasklist | findstr "[PID]"  # com.docker.backend.exe만 있어야 함
```

### 4. 주요 API 엔드포인트

```
GET  /api/room/list
Response: [
  {
    "roomId": "c5e64178",
    "inviteCode": "c5e64178",
    "status": "PLAYING",
    "currentPlayers": 5,
    "maxPlayers": 10,
    "hostNickname": "방장",
    "currentRound": 2,
    "totalRounds": 5,
    "createdAt": 1764829067999
  }
]

POST /api/room/join-by-code
Body: {"inviteCode": "c5e64178"}
Response:
  - 200: {"roomId": "c5e64178", "inviteCode": "c5e64178"}
  - 404: {"message": "초대코드에 해당하는 방을 찾을 수 없습니다."}
```

### 5. 코드 정리

#### 제거된 불필요한 import (RoomService.java)
- `com.fasterxml.jackson.core.JsonProcessingException`
- `com.fasterxml.jackson.databind.ObjectMapper`
- `org.springframework.data.redis.core.RedisTemplate`
- `java.time.Duration`

### 6. 서버 실행 방법

```bash
# 1. 6379 포트 상태 확인
netstat -ano | findstr ":6379"

# 2. wslrelay가 있다면 종료
taskkill /F /PID [wslrelay의 PID]

# 3. Docker Redis 시작
cd paletteduck-server
docker-compose up -d redis

# 4. Redis 데이터 확인
docker exec paletteduck-redis redis-cli DBSIZE
docker exec paletteduck-redis redis-cli KEYS "*"

# 5. 서버 시작
./gradlew bootRun
```

### 7. 테스트 시나리오

#### ✅ 방 목록 조회
1. 메인 페이지에서 "방 목록" 버튼 클릭
2. 공개방 목록 모달 표시
3. 각 방 정보 확인:
   - WAITING: "상태: 대기중 | 인원: 3/10"
   - PLAYING: "상태: 게임중 | 인원: 5/10 | 라운드: 2/5"
4. 방 클릭 시:
   - WAITING → 참가자로 입장
   - PLAYING → 관전자로 입장

#### ✅ 초대코드 입력
1. 방장이 "초대 코드 복사" 클릭
2. URL 복사됨: `http://localhost:5173/room/c5e64178`
3. 다른 세션에서 "초대코드 입력" 클릭
4. URL 전체 또는 코드만 입력
5. 방 입장 성공

### 8. 현재 상태

#### 완료
- ✅ 공개방/비밀방 구분 기능
- ✅ 랜덤 방 입장 (공개방만)
- ✅ **방 목록 조회 (공개방, WAITING+PLAYING)**
- ✅ **초대코드 입력 기능 (URL 파싱 지원)**
- ✅ **초대 링크 복사 (URL 전체)**
- ✅ 방 생성 UI (모달)
- ✅ Jackson 직렬화 버그 수정
- ✅ Redis 데이터 정리
- ✅ 불필요한 코드 정리

#### 미완료 (향후 작업)
- ⏳ 방 목록 실시간 업데이트 (폴링 또는 WebSocket)
- ⏳ 방 목록 필터링 (상태별, 인원별)
- ⏳ 테스트 코드 작성

### 9. 문제 발생 시 체크리스트

#### Redis 연결 문제
```bash
# 1. 6379 포트 확인
netstat -ano | findstr ":6379"

# 2. wslrelay 확인 및 종료
tasklist | findstr "wslrelay"
taskkill /F /PID [PID]

# 3. Docker Redis만 실행 확인
tasklist | findstr "docker"

# 4. 서버 재시작
```

#### 초대코드를 찾을 수 없을 때
1. 서버가 Docker Redis에 연결되어 있는지 확인
2. wslrelay 프로세스가 실행 중인지 확인
3. 서버 로그 확인: "Finding room by invite code"
4. 입력한 초대코드가 정확한지 확인

---

## 다음 작업 시 확인 사항

1. **서버 시작 전**
   - 로컬 Redis(wslrelay) 실행 여부 확인
   - Docker Redis만 실행되어야 함

2. **테스트 시**
   - 브라우저 캐시 삭제 (Ctrl+Shift+Delete)
   - 6379 포트에 wslrelay가 없는지 확인

3. **빌드 후**
   - 반드시 서버 재시작
   - Redis 데이터 확인: `docker exec paletteduck-redis redis-cli KEYS "*"`
