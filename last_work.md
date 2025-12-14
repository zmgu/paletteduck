# 베스트 아티스트 그림 저장 로직 구현

## 개요
게임 종료 시 베스트 아티스트(전체 게임에서 총 추천수가 가장 많은 플레이어)의 그림 중 가장 많은 추천을 받은 그림을 GAME_END 화면에 표시하기 위한 로직

## 핵심 개념
- **베스트 아티스트**: 전체 게임에서 `totalLikes`가 가장 많은 플레이어
- **표시할 그림**: 베스트 아티스트가 그린 그림들 중 한 턴에서 가장 많은 추천을 받은 그림

## 주의사항
베스트 아티스트 ≠ 최고 추천 그림일 수 있음

예시:
- 플레이어 A: 턴1에서 5개, 턴2에서 4개 → 총 9개 추천 (베스트 아티스트)
- 플레이어 B: 턴1에서 8개 추천 → 총 8개 추천

→ 베스트 아티스트는 A지만, 한 턴 최고 추천은 B의 그림

## 구현 방법: 플레이어별 최고 그림 저장

### 1. 타입 정의
```typescript
type PlayerBestDrawings = {
  [playerId: string]: {
    imageUrl: string;        // Canvas를 base64 또는 blob URL로 저장
    likes: number;           // 해당 턴에서 받은 추천 수
    turnNumber: number;      // 턴 번호
    nickname: string;        // 출제자 닉네임
  }
};
```

### 2. State 추가
```typescript
const [playerBestDrawings, setPlayerBestDrawings] = useState<PlayerBestDrawings>({});
```

### 3. 턴 종료 시 업데이트 (TURN_RESULT → 다음 턴 전)
```typescript
function updatePlayerBestDrawing(turnData, canvasImageUrl) {
  const drawerId = turnData.drawerId;

  // 현재 턴의 추천 수 계산
  const currentLikes = Object.values(turnData.votes)
    .filter(vote => vote === 'LIKE').length;

  const existingBest = playerBestDrawings[drawerId];

  // 해당 플레이어의 기존 최고 그림보다 추천이 많으면 교체
  if (!existingBest || currentLikes > existingBest.likes) {
    setPlayerBestDrawings(prev => ({
      ...prev,
      [drawerId]: {
        imageUrl: canvasImageUrl,
        likes: currentLikes,
        turnNumber: turnData.turnNumber,
        nickname: turnData.drawerNickname
      }
    }));
  }
}
```

### 4. GAME_END에서 베스트 아티스트 그림 찾기
```typescript
// 베스트 아티스트 찾기 (totalLikes 기준)
const bestArtist = players.reduce((best, player) =>
  (player.totalLikes || 0) > (best?.totalLikes || 0) ? player : best
);

// 베스트 아티스트의 최고 추천 그림 가져오기
const bestArtistDrawing = bestArtist
  ? playerBestDrawings[bestArtist.playerId]
  : null;
```

### 5. GAME_END 화면에서 렌더링
```typescript
{/* 그림 영역 */}
<div style={{
  width: '400px',
  height: '400px',
  backgroundColor: '#f5f5f5',
  border: '3px solid #9c27b0',
  borderRadius: '8px',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  position: 'relative',
  overflow: 'hidden'
}}>
  {bestArtistDrawing ? (
    <img
      src={bestArtistDrawing.imageUrl}
      alt="베스트 아티스트의 그림"
      style={{
        width: '100%',
        height: '100%',
        objectFit: 'contain'
      }}
    />
  ) : (
    <div style={{
      textAlign: 'center',
      color: '#999',
      fontSize: '16px'
    }}>
      베스트 아티스트의 그림
    </div>
  )}
</div>
```

## 구현 위치

### 본 페이지 (GameRoomPage)
- `paletteduck-client/src/pages/GameRoomPage/index.tsx`
- WebSocket 메시지 핸들러에서 턴 종료 시 `updatePlayerBestDrawing()` 호출

### 프리뷰 페이지 (현재 수정 중)
- `paletteduck-client/src/pages/GameRoomPreview.tsx`
- 이미 GAME_END 화면 레이아웃은 수정 완료 (1214-1339라인)
- 그림 표시 공간은 준비됨 (1235-1255라인)

## 장점
- **정확함**: 베스트 아티스트의 그림을 정확히 보장
- **메모리 효율**: 플레이어 수만큼만 저장 (최대 18개)
- **간단한 조회**: O(1)로 그림 찾기

## 다음 작업
1. 본 페이지에 State 추가
2. WebSocket 핸들러에서 턴 종료 시 `updatePlayerBestDrawing()` 호출
3. GAME_END 메시지 수신 시 베스트 아티스트 그림 렌더링
4. 프리뷰 페이지에서 테스트
