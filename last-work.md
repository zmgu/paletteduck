# 마지막 작업 내용 (2025-12-03)

## 구현 완료: 공개방/비밀방 기능

### 1. 백엔드 구현

#### 수정된 파일들

**RoomInfo.java**
```java
@JsonProperty("isPublic")
@Getter(AccessLevel.NONE)  // Lombok 자동 생성 제외
@Setter(AccessLevel.NONE)
private boolean isPublic = true;

// 명시적 getter/setter
public boolean isPublic() {
    return isPublic;
}

public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
}
```
- **중요**: Jackson이 `"public"`과 `"isPublic"` 두 개로 직렬화하는 버그 수정
- Lombok @Getter를 제외하고 명시적으로 작성해야 함

**RoomCreateRequest.java** (신규)
```java
@JsonProperty("isPublic")
private boolean isPublic = true;
```

**RoomService.java**
- `createRoom(String playerId, String nickname, boolean isPublic)` - isPublic 파라미터 추가
- `findRandomPublicRoom()` - 랜덤 공개방 찾기
- `findAvailablePublicRooms()` - 입장 가능한 공개방 목록
- `isRoomAvailable()` - 공개방 + WAITING + 자리 있음 체크
- `countParticipants()` - 참가자 수 계산 (관전자 제외)

**RoomRepository.java**
- `findAll()` - 모든 방 조회 (Redis KEYS 사용)

**RoomController.java**
- `POST /api/room/create` - RoomCreateRequest 받아서 공개/비밀방 생성
- `POST /api/room/random` - 랜덤 공개방 입장
  - 성공: `{roomId, inviteCode}` 반환
  - 실패: 404 "사용 가능한 공개방이 없습니다."

### 2. 프론트엔드 구현

**MainPage.tsx**
- 방 만들기 버튼 클릭 → 모달 표시
- 모달에서 "🌍 공개방 만들기" / "🔒 비밀방 만들기" 선택
- "방 목록" 버튼 삭제 → "랜덤 방 입장" 버튼 추가
- `handleCreateRoom(isPublic: boolean)` - isPublic 전달
- `handleRandomJoin()` - POST /api/room/random 호출

### 3. 해결한 주요 버그

#### 문제 1: Jackson 중복 직렬화
```json
// 잘못된 형식 (버그)
{"public":true,"isPublic":true}

// 올바른 형식 (수정 후)
{"isPublic":true}
```
- **원인**: Lombok @Getter가 `isPublic()` 메서드 생성 → Jackson이 `"public"` 속성으로도 직렬화
- **해결**: `@Getter(AccessLevel.NONE)` + 명시적 getter/setter 작성

#### 문제 2: 로컬 Redis와 Docker Redis 동시 실행
- **원인**: WSL Redis(또는 로컬 서비스)가 6379 포트에서 실행 중
- **증상**: Docker Redis 데이터를 삭제해도 이전 데이터 계속 로드됨
- **해결**: 로컬 Redis 중지, Docker Redis만 사용
- **확인 방법**: `netstat -ano | findstr ":6379"`

### 4. Redis 설정

**docker-compose.yml**
```yaml
services:
  redis:
    image: redis:7.4.1
    container_name: paletteduck-redis
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    command: redis-server --appendonly yes
```

**application-local.yml**
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### 5. 테스트 시나리오

#### ✅ 정상 동작 확인
1. **서버 시작 직후 랜덤 입장**
   - 방이 없으므로 → "사용 가능한 공개방이 없습니다."

2. **비밀방 생성 후 랜덤 입장**
   - 비밀방은 제외 → "사용 가능한 공개방이 없습니다."
   - 로그: `"Room xxx is private, skipping"`

3. **공개방 생성 후 랜덤 입장**
   - 해당 공개방으로 입장 성공

4. **로그 확인**
   ```
   Room loaded - roomId: xxx, isPublic: true, JSON: {"isPublic":true,...}
   ```
   - `"public"` 필드 없어야 정상!

### 6. 현재 상태

#### 완료
- ✅ 공개방/비밀방 구분 기능
- ✅ 랜덤 방 입장 (공개방만)
- ✅ 방 생성 UI (모달)
- ✅ Jackson 직렬화 버그 수정
- ✅ Redis 데이터 정리

#### 미완료 (향후 작업)
- ⏳ 초대코드 입력 기능 활성화
- ⏳ 방 목록 보기 기능 (공개방/비밀방, WAITING/PLAYING 표시)
- ⏳ 테스트 코드 작성

### 7. 서버 실행 방법

```bash
# Redis 시작
cd paletteduck-server
docker-compose up -d redis

# Redis 데이터 확인
docker exec paletteduck-redis redis-cli DBSIZE
docker exec paletteduck-redis redis-cli KEYS "*"

# 서버 시작
./gradlew bootRun

# 또는 IntelliJ에서 PaletteduckServerApplication 실행
```

### 8. 문제 발생 시 체크리스트

#### Redis 데이터가 삭제 안될 때
```bash
# 로컬 Redis 확인
netstat -ano | findstr ":6379"

# Docker Redis 완전 재생성
docker stop paletteduck-redis
docker rm paletteduck-redis
docker volume rm paletteduck-server_redis-data
cd paletteduck-server && docker-compose up -d redis
```

#### Jackson 직렬화 문제 재발 시
- RoomInfo.java의 `@Getter(AccessLevel.NONE)` 확인
- 로그에서 `"public":true,"isPublic":true` 두 개 있으면 버그 재발
- 빌드 후 서버 재시작 필수

### 9. API 엔드포인트

```
POST /api/room/create
Body: {"isPublic": true|false}
Response: {"roomId": "xxx", "inviteCode": "xxx"}

POST /api/room/random
Response:
  - 200: {"roomId": "xxx", "inviteCode": "xxx"}
  - 404: {"message": "사용 가능한 공개방이 없습니다."}

POST /api/room/{roomId}/join
GET  /api/room/{roomId}
POST /api/room/{roomId}/leave
```

### 10. 주요 로그 패턴

```
# 정상 - 비밀방 필터링
Room check - roomId: xxx, isPublic: false, status: WAITING, participants: 1/10
Room xxx is private, skipping

# 정상 - 공개방 선택
Room available for random join - roomId: xxx
Random public room selected - roomId: xxx, isPublic: true

# 비정상 - 중복 필드 (버그)
JSON: {"public":true,"isPublic":true}  ← 이러면 안됨!

# 정상 - 단일 필드
JSON: {"isPublic":true}  ← 정상
```

---

## 다음 작업 시 확인 사항

1. **서버 시작 전**
   - 로컬 Redis 실행 여부 확인 (`netstat -ano | findstr ":6379"`)
   - Docker Redis만 실행되어야 함

2. **테스트 시**
   - 브라우저 캐시 삭제 (Ctrl+Shift+Delete)
   - 서버 로그에서 `"public"` 필드 없는지 확인

3. **빌드 후**
   - 반드시 서버 재시작
   - Redis 데이터 완전 삭제 추천 (`docker exec paletteduck-redis redis-cli FLUSHALL`)
