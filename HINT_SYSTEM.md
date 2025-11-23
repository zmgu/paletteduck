# 힌트 시스템 문서

## 개요

PaletteDuck 게임의 힌트 시스템은 플레이어들이 정답을 맞추는 것을 돕기 위해 단계적으로 정보를 공개하는 기능입니다.

## 힌트 레벨

### 자동 힌트 (시간 기반)

| 레벨 | 시간 | 내용 | 예시 |
|------|------|------|------|
| 0 | 시작~20초 | 힌트 없음 | `???` |
| 1 | 20초 후 | 글자수 공개 | `_ _ _` |
| 2 | 40초 후 | 초성 랜덤 1자리 공개 | `ㅅ _ _` |

### 수동 힌트 (출제자 버튼)

**활성화 조건**: Level 2 (40초) 이후

1. **💡 초성 힌트 버튼**
   - 클릭 시 초성 랜덤 1자리씩 추가 공개
   - 예: `ㅅ _ _` → `ㅅ ㄱ _`

2. **🔥 글자 힌트 버튼**
   - 클릭 시 글자 랜덤 1개씩 추가 공개
   - 제한: 최대 (글자수 - 1)개까지만 공개 가능
   - 예: `ㅅ _ _` → `사 _ _` → `사 _ 과` (최대)

## 핵심 구현

### 1. 서버 측 (Java/Spring)

#### HintService.java

```java
/**
 * 글자수 힌트를 생성합니다
 */
public String[] generateWordLengthHint(String word) {
    if (word == null || word.isEmpty()) {
        return new String[0];
    }

    String[] hint = new String[word.length()];
    for (int i = 0; i < word.length(); i++) {
        hint[i] = "_";
    }
    return hint;
}

/**
 * 랜덤 초성 위치를 공개합니다
 */
public Integer revealRandomChosung(String word, Set<Integer> revealedPositions) {
    if (word == null || word.isEmpty()) {
        return null;
    }

    // 아직 공개되지 않은 위치 찾기
    List<Integer> availablePositions = new ArrayList<>();
    for (int i = 0; i < word.length(); i++) {
        if (!revealedPositions.contains(i)) {
            availablePositions.add(i);
        }
    }

    if (availablePositions.isEmpty()) {
        return null;
    }

    // 랜덤 위치 선택
    int randomIndex = random.nextInt(availablePositions.size());
    return availablePositions.get(randomIndex);
}

/**
 * 랜덤 글자 위치를 공개합니다 (최대 글자수-1까지)
 */
public Integer revealRandomLetter(String word, Set<Integer> revealedPositions) {
    if (word == null || word.isEmpty()) {
        return null;
    }

    // 최대 공개 가능 개수는 글자수-1
    int maxReveals = word.length() - 1;
    if (revealedPositions.size() >= maxReveals) {
        return null; // 더 이상 공개할 수 없음
    }

    // 아직 공개되지 않은 위치 찾기
    List<Integer> availablePositions = new ArrayList<>();
    for (int i = 0; i < word.length(); i++) {
        if (!revealedPositions.contains(i)) {
            availablePositions.add(i);
        }
    }

    if (availablePositions.isEmpty()) {
        return null;
    }

    // 랜덤 위치 선택
    int randomIndex = random.nextInt(availablePositions.size());
    return availablePositions.get(randomIndex);
}

/**
 * 현재 공개된 정보를 바탕으로 힌트 배열을 생성합니다
 */
public String[] generateHintArray(String word,
                                  Set<Integer> revealedChosungPositions,
                                  Set<Integer> revealedLetterPositions) {
    if (word == null || word.isEmpty()) {
        return new String[0];
    }

    String[] hint = new String[word.length()];

    for (int i = 0; i < word.length(); i++) {
        char ch = word.charAt(i);

        // 글자가 공개된 경우
        if (revealedLetterPositions.contains(i)) {
            hint[i] = String.valueOf(ch);
        }
        // 초성이 공개된 경우
        else if (revealedChosungPositions.contains(i)) {
            hint[i] = extractChosung(ch);
        }
        // 아직 공개되지 않은 경우
        else {
            hint[i] = "_";
        }
    }

    return hint;
}
```

#### GameTimerService.java

```java
@Async
public void startHintTimer(String roomId, int turnNumber, int hintLevel, int delaySeconds) {
    try {
        TimeUnit.SECONDS.sleep(delaySeconds);

        GameState gameState = gameRepository.findById(roomId);
        if (gameState == null || gameState.getCurrentTurn() == null) {
            return;
        }

        // 턴 번호 및 페이즈 검증
        if (gameState.getCurrentTurn().getTurnNumber() != turnNumber) {
            return;
        }
        if (gameState.getPhase() != GamePhase.DRAWING) {
            return;
        }

        String word = gameState.getCurrentTurn().getWord();
        TurnInfo turnInfo = gameState.getCurrentTurn();

        // 힌트 레벨에 따라 처리
        if (hintLevel == 1) {
            // 레벨 1: 글자수 공개
            turnInfo.setHintLevel(1);
            String[] hintArray = hintService.generateWordLengthHint(word);
            turnInfo.setHintArray(hintArray);
            turnInfo.setCurrentHint("글자수 힌트");

            gameRepository.save(roomId, gameState);
            messagingTemplate.convertAndSend(WebSocketTopics.gameState(roomId), gameState);

            log.info("Auto hint (word length) - room: {}, length: {}", roomId, word.length());
        } else if (hintLevel == 2) {
            // 레벨 2: 초성 랜덤 한 자리 공개
            Integer position = hintService.revealRandomChosung(word, turnInfo.getRevealedChosungPositions());
            if (position != null) {
                turnInfo.getRevealedChosungPositions().add(position);
                turnInfo.setHintLevel(2);

                String[] hintArray = hintService.generateHintArray(word,
                    turnInfo.getRevealedChosungPositions(),
                    turnInfo.getRevealedLetterPositions());
                turnInfo.setHintArray(hintArray);

                String hint = hintService.generateHintDisplay(word,
                    turnInfo.getRevealedChosungPositions(),
                    turnInfo.getRevealedLetterPositions());
                turnInfo.setCurrentHint(hint);

                gameRepository.save(roomId, gameState);
                messagingTemplate.convertAndSend(WebSocketTopics.gameState(roomId), gameState);

                log.info("Auto hint (chosung) - room: {}, hint: {}", roomId, hint);
            }
        }

    } catch (InterruptedException e) {
        log.error("Hint timer interrupted - roomId: {}, hintLevel: {}", roomId, hintLevel, e);
        Thread.currentThread().interrupt();
    }
}
```

#### GameService.java

```java
/**
 * 출제자가 수동으로 초성 힌트를 제공합니다
 */
public boolean provideChosungHint(String roomId, String playerId) {
    GameState gameState = gameRepository.findById(roomId);
    if (gameState == null || gameState.getCurrentTurn() == null) {
        return false;
    }

    // 출제자 확인
    if (!gameState.getCurrentTurn().getDrawerId().equals(playerId)) {
        return false;
    }

    // 그리기 단계 확인
    if (gameState.getPhase() != GamePhase.DRAWING) {
        return false;
    }

    // 레벨 2 이상이어야 초성 힌트 사용 가능
    if (gameState.getCurrentTurn().getHintLevel() < 2) {
        return false;
    }

    TurnInfo turnInfo = gameState.getCurrentTurn();
    String word = turnInfo.getWord();

    // 랜덤 초성 위치 공개
    Integer position = hintService.revealRandomChosung(word, turnInfo.getRevealedChosungPositions());
    if (position == null) {
        return false;
    }

    turnInfo.getRevealedChosungPositions().add(position);

    // 힌트 배열 및 문자열 업데이트
    String[] hintArray = hintService.generateHintArray(word,
            turnInfo.getRevealedChosungPositions(),
            turnInfo.getRevealedLetterPositions());
    turnInfo.setHintArray(hintArray);

    String hint = hintService.generateHintDisplay(word,
            turnInfo.getRevealedChosungPositions(),
            turnInfo.getRevealedLetterPositions());
    turnInfo.setCurrentHint(hint);

    gameRepository.save(roomId, gameState);
    return true;
}

/**
 * 출제자가 수동으로 글자 힌트를 제공합니다
 */
public boolean provideLetterHint(String roomId, String playerId) {
    GameState gameState = gameRepository.findById(roomId);
    if (gameState == null || gameState.getCurrentTurn() == null) {
        return false;
    }

    // 출제자 확인
    if (!gameState.getCurrentTurn().getDrawerId().equals(playerId)) {
        return false;
    }

    // 그리기 단계 확인
    if (gameState.getPhase() != GamePhase.DRAWING) {
        return false;
    }

    // 레벨 2 이상이어야 글자 힌트 사용 가능
    if (gameState.getCurrentTurn().getHintLevel() < 2) {
        return false;
    }

    TurnInfo turnInfo = gameState.getCurrentTurn();
    String word = turnInfo.getWord();

    // 랜덤 글자 위치 공개 (최대 글자수-1까지)
    Integer position = hintService.revealRandomLetter(word, turnInfo.getRevealedLetterPositions());
    if (position == null) {
        return false;
    }

    turnInfo.getRevealedLetterPositions().add(position);

    // 힌트 배열 및 문자열 업데이트
    String[] hintArray = hintService.generateHintArray(word,
            turnInfo.getRevealedChosungPositions(),
            turnInfo.getRevealedLetterPositions());
    turnInfo.setHintArray(hintArray);

    String hint = hintService.generateHintDisplay(word,
            turnInfo.getRevealedChosungPositions(),
            turnInfo.getRevealedLetterPositions());
    turnInfo.setCurrentHint(hint);

    gameRepository.save(roomId, gameState);
    return true;
}
```

#### WebSocketGameController.java

```java
@MessageMapping("/room/{roomId}/game/hint/chosung")
public void provideChosungHint(@DestinationVariable String roomId, @Payload String playerId) {
    boolean success = gameService.provideChosungHint(roomId, playerId);

    if (success) {
        GameState gameState = gameService.getGameState(roomId);
        messagingTemplate.convertAndSend(WebSocketTopics.gameState(roomId), gameState);
    }
}

@MessageMapping("/room/{roomId}/game/hint/letter")
public void provideLetterHint(@DestinationVariable String roomId, @Payload String playerId) {
    boolean success = gameService.provideLetterHint(roomId, playerId);

    if (success) {
        GameState gameState = gameService.getGameState(roomId);
        messagingTemplate.convertAndSend(WebSocketTopics.gameState(roomId), gameState);
    }
}
```

### 2. 클라이언트 측 (TypeScript/React)

#### TurnInfo 타입 정의

```typescript
export interface TurnInfo {
  turnNumber: number;
  drawerId: string;
  drawerNickname: string;
  word: string | null;
  wordChoices: string[];
  timeLeft: number;
  correctPlayerIds: string[];
  hintLevel: number;
  currentHint: string | null;
  hintArray: string[] | null;  // 힌트 배열
  revealedChosungPositions: number[];
  revealedLetterPositions: number[];
}
```

#### DrawingArea.tsx

```typescript
const canUseChosungHint = turnInfo.hintLevel >= 2;
const canUseLetterHint = turnInfo.hintLevel >= 2;

// 힌트 표시: hintLevel에 따라 다르게 표시
const displayHint = (() => {
  if (turnInfo.hintLevel === 0) {
    // Level 0: 힌트 없음 - 물음표로 표시
    return '???';
  } else if (turnInfo.hintArray) {
    // Level 1 이상: hintArray 사용
    return turnInfo.hintArray.join(' ');
  } else {
    // fallback
    return '???';
  }
})();

// 출제자 화면
{isDrawer ? (
  <>
    <p style={{ fontSize: '32px', fontWeight: 'bold', color: '#2e7d32' }}>
      단어: {turnInfo.word}
    </p>

    {/* 힌트 제공 버튼 */}
    <div style={{ marginTop: '20px', display: 'flex', gap: '10px' }}>
      <button
        onClick={onProvideChosungHint}
        disabled={!canUseChosungHint}
        style={{
          padding: '10px 20px',
          backgroundColor: canUseChosungHint ? '#ff9800' : '#ccc',
          cursor: canUseChosungHint ? 'pointer' : 'not-allowed',
        }}
      >
        💡 초성 힌트 {!canUseChosungHint && '(40초 후)'}
      </button>

      <button
        onClick={onProvideLetterHint}
        disabled={!canUseLetterHint}
        style={{
          padding: '10px 20px',
          backgroundColor: canUseLetterHint ? '#f44336' : '#ccc',
          cursor: canUseLetterHint ? 'pointer' : 'not-allowed',
        }}
      >
        🔥 글자 힌트 {!canUseLetterHint && '(40초 후)'}
      </button>
    </div>
  </>
) : (
  // 플레이어 화면 - 힌트 표시
  <p style={{ fontSize: '28px', letterSpacing: '8px' }}>
    {displayHint}
  </p>
)}
```

#### GameRoomPage.tsx

```typescript
const provideChosungHint = useCallback(() => {
  if (!playerInfo?.playerId) return;
  wsClient.send(`/app/room/${roomId}/game/hint/chosung`, playerInfo.playerId);
}, [roomId, playerInfo?.playerId]);

const provideLetterHint = useCallback(() => {
  if (!playerInfo?.playerId) return;
  wsClient.send(`/app/room/${roomId}/game/hint/letter`, playerInfo.playerId);
}, [roomId, playerInfo?.playerId]);
```

## 데이터 구조

### TurnInfo

```java
public class TurnInfo {
    private int hintLevel;                      // 현재 힌트 레벨 (0: 없음, 1: 글자수, 2: 초성)
    private String currentHint;                 // 힌트 문자열 (레거시)
    private String[] hintArray;                 // 힌트 배열 (각 위치에 글자, 초성, 또는 "_")
    private Set<Integer> revealedChosungPositions;  // 공개된 초성 위치
    private Set<Integer> revealedLetterPositions;   // 공개된 글자 위치
}
```

## 타이밍 설정

`GameConstants.java`에서 설정:

```java
public static final class Timing {
    public static final int FIRST_HINT_DELAY = 20;   // 첫 번째 힌트 (글자수) - 20초 후
    public static final int SECOND_HINT_DELAY = 40;  // 두 번째 힌트 (초성) - 40초 후
}
```

## Self-Injection 패턴

`@Async` 메서드가 같은 클래스 내에서 호출될 때 프록시가 적용되지 않는 문제를 해결하기 위해 Self-Injection 사용:

```java
@Service
@RequiredArgsConstructor
public class GameTimerService {
    private GameTimerService self;

    @Autowired
    @Lazy
    public void setSelf(GameTimerService self) {
        this.self = self;
    }

    // @Async 메서드 호출 시 self를 통해 호출
    self.startHintTimer(roomId, turnNumber, 1, GameConstants.Timing.FIRST_HINT_DELAY);
}
```

## 주요 특징

1. **랜덤 공개**: 초성과 글자는 랜덤 위치에 공개되어 매번 다른 패턴
2. **점진적 공개**: 시간에 따라 점점 더 많은 정보 제공
3. **전체 정답 방지**: 글자 힌트는 최대 (글자수 - 1)개까지만 공개
4. **출제자 제어**: 40초 후부터 출제자가 직접 힌트 제공 가능
5. **UI 통합**: 별도 힌트 영역 없이 글자수 표시 영역에 힌트 통합

## 예시 시나리오

**정답: "사과" (2글자)**

1. **0~20초**: `???` (힌트 없음)
2. **20초 (Level 1)**: `_ _` (글자수 공개)
3. **40초 (Level 2)**: `ㅅ _` 또는 `_ ㄱ` (초성 랜덤 1자리)
4. **출제자가 초성 힌트 버튼 클릭**: `ㅅ ㄱ` (모든 초성 공개)
5. **출제자가 글자 힌트 버튼 클릭**: `사 _` 또는 `_ 과` (글자 1개 공개, 최대치)

---

**작성일**: 2025-11-22
**버전**: 1.0
