# 테스트 코드 인덱스

## 📖 사용 방법

### Gradle로 특정 태그의 테스트만 실행
```bash
# 특정 태그의 테스트만 실행
./gradlew test --tests "*" -Dgroups="hint-service"

# 여러 태그 실행
./gradlew test --tests "*" -Dgroups="hint-service,scoring-service"

# 특정 클래스의 특정 테스트만 실행
./gradlew test --tests "HintServiceTest" -Dgroups="hint-korean"
```

### IntelliJ에서 실행
- 테스트 메서드 옆의 실행 버튼 클릭
- 또는 `@Tag` 어노테이션을 클릭하여 같은 태그의 모든 테스트 실행

---

## 🎮 서비스 레이어 테스트 (149개)

### HintService - 힌트 시스템 (26개)

#### 한글 초성 추출
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `hint-korean-chosung` | extractChosung - 한글 초성 추출 | HintServiceTest.java:23 |
| `hint-korean-chosung` | extractChosung - 쌍자음 처리 | HintServiceTest.java:33 |
| `hint-korean-chosung` | extractChosung - 영문 처리 | HintServiceTest.java:43 |
| `hint-korean-chosung` | extractChosung - 숫자 처리 | HintServiceTest.java:53 |
| `hint-korean-chosung` | extractChosung - 공백 처리 | HintServiceTest.java:63 |
| `hint-korean-chosung` | extractChosung - 특수문자 처리 | HintServiceTest.java:73 |

#### 힌트 배열 생성
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `hint-array-generation` | generateWordLengthHint - 글자수 힌트 | HintServiceTest.java:83 |
| `hint-array-generation` | generateHintArray - 초성 공개 | HintServiceTest.java:93 |
| `hint-array-generation` | generateHintArray - 글자 공개 | HintServiceTest.java:104 |
| `hint-array-generation` | generateHintArray - 초성+글자 공개 | HintServiceTest.java:116 |
| `hint-array-generation` | generateHintArray - 공백 단어 | HintServiceTest.java:129 |

#### 랜덤 힌트 위치 선택
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `hint-random-reveal` | revealRandomChosung - 랜덤 초성 공개 | HintServiceTest.java:139 |
| `hint-random-reveal` | revealRandomChosung - 모든 위치 공개 시 null | HintServiceTest.java:151 |
| `hint-random-reveal` | revealRandomChosung - 일부 공개 후 랜덤 | HintServiceTest.java:163 |
| `hint-random-reveal` | revealRandomLetter - 랜덤 글자 공개 | HintServiceTest.java:177 |
| `hint-random-reveal` | revealRandomLetter - 모든 위치 공개 시 null | HintServiceTest.java:189 |
| `hint-random-reveal` | revealRandomLetter - 최대 공개 수 제한 | HintServiceTest.java:201 |

#### 힌트 디스플레이
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `hint-display` | generateHintDisplay - 초성만 공개 | HintServiceTest.java:215 |
| `hint-display` | generateHintDisplay - 글자만 공개 | HintServiceTest.java:228 |
| `hint-display` | generateHintDisplay - 초성+글자 공개 | HintServiceTest.java:242 |
| `hint-display` | generateHintDisplay - 아무것도 공개 안됨 | HintServiceTest.java:257 |
| `hint-display` | generateHintDisplay - 영문 단어 | HintServiceTest.java:267 |
| `hint-display` | generateHintDisplay - 공백 포함 단어 | HintServiceTest.java:277 |
| `hint-display` | generateHintDisplay - 특수문자 포함 | HintServiceTest.java:288 |
| `hint-display` | generateHintDisplay - 한영 혼합 | HintServiceTest.java:299 |

---

### GameScoringService - 점수 계산 (31개)

#### 정답자 점수 계산
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `scoring-answerer` | awardAnswererScore - 1등 점수 | GameScoringServiceTest.java:31 |
| `scoring-answerer` | awardAnswererScore - 2등 점수 | GameScoringServiceTest.java:44 |
| `scoring-answerer` | awardAnswererScore - 3등 점수 | GameScoringServiceTest.java:57 |
| `scoring-answerer` | awardAnswererScore - 4등 점수 | GameScoringServiceTest.java:70 |
| `scoring-answerer` | awardAnswererScore - 5등 이후 점수 | GameScoringServiceTest.java:83 |

#### 출제자 보너스 점수
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `scoring-drawer-bonus` | awardDrawerBonus - 2-4명 150% 배율 | GameScoringServiceTest.java:96 |
| `scoring-drawer-bonus` | awardDrawerBonus - 5-6명 120% 배율 | GameScoringServiceTest.java:111 |
| `scoring-drawer-bonus` | awardDrawerBonus - 7-8명 100% 배율 | GameScoringServiceTest.java:126 |
| `scoring-drawer-bonus` | awardDrawerBonus - 9-10명 80% 배율 | GameScoringServiceTest.java:141 |
| `scoring-drawer-bonus` | awardDrawerBonus - 11명 이상 65% 배율 | GameScoringServiceTest.java:156 |

#### 힌트 패널티
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `scoring-hint-penalty` | applyHintPenalty - 힌트 없음 100% | GameScoringServiceTest.java:171 |
| `scoring-hint-penalty` | applyHintPenalty - 레벨1 90% | GameScoringServiceTest.java:180 |
| `scoring-hint-penalty` | applyHintPenalty - 레벨2 80% | GameScoringServiceTest.java:189 |
| `scoring-hint-penalty` | applyHintPenalty - 수동힌트 각 -10% | GameScoringServiceTest.java:198 |
| `scoring-hint-penalty` | applyHintPenalty - 자동+수동 누적 | GameScoringServiceTest.java:209 |

#### 조기 정답 패널티
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `scoring-early-penalty` | applyEarlyAnswerPenalty - 70% 이상 정답 | GameScoringServiceTest.java:221 |
| `scoring-early-penalty` | applyEarlyAnswerPenalty - 50-70% 패널티 없음 | GameScoringServiceTest.java:236 |
| `scoring-early-penalty` | applyEarlyAnswerPenalty - 힌트 사용 시 패널티 없음 | GameScoringServiceTest.java:251 |

#### 통합 점수 계산
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `scoring-integration` | handleCorrectAnswer - 첫 정답자 | GameScoringServiceTest.java:266 |
| `scoring-integration` | handleCorrectAnswer - 두번째 정답자 | GameScoringServiceTest.java:280 |
| `scoring-integration` | handleCorrectAnswer - 정답자+출제자 점수 | GameScoringServiceTest.java:294 |
| `scoring-integration` | handleCorrectAnswer - isCorrect 플래그 | GameScoringServiceTest.java:311 |
| `scoring-integration` | handleCorrectAnswer - 중복 정답 무시 | GameScoringServiceTest.java:323 |

#### 엣지 케이스
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `scoring-edge-case` | 점수 음수 방지 | GameScoringServiceTest.java:335 |
| `scoring-edge-case` | 플레이어 1명일 때 | GameScoringServiceTest.java:349 |
| `scoring-edge-case` | 다중 힌트 조합 | GameScoringServiceTest.java:363 |
| `scoring-edge-case` | 모든 플레이어 정답 시 출제자 점수 | GameScoringServiceTest.java:382 |
| `scoring-edge-case` | 점수 누적 확인 | GameScoringServiceTest.java:401 |

---

### AnswerValidationService - 정답 검증 (11개)

| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `answer-validation` | checkAnswer - 정확한 정답 | AnswerValidationServiceTest.java:26 |
| `answer-validation` | checkAnswer - 대소문자 다름 | AnswerValidationServiceTest.java:40 |
| `answer-validation` | checkAnswer - 공백 포함 | AnswerValidationServiceTest.java:54 |
| `answer-validation` | checkAnswer - 대소문자+공백 | AnswerValidationServiceTest.java:68 |
| `answer-validation` | checkAnswer - 틀린 답 | AnswerValidationServiceTest.java:82 |
| `answer-validation` | checkAnswer - 부분 정답 | AnswerValidationServiceTest.java:96 |
| `answer-validation` | checkAnswer - null currentTurn | AnswerValidationServiceTest.java:110 |
| `answer-validation` | checkAnswer - null word | AnswerValidationServiceTest.java:125 |
| `answer-validation` | checkAnswer - 여러 공백 | AnswerValidationServiceTest.java:142 |
| `answer-validation` | checkAnswer - 탭과 줄바꿈 | AnswerValidationServiceTest.java:156 |
| `answer-validation` | checkAnswer - 한영 혼합 | AnswerValidationServiceTest.java:170 |

---

### GameService - 게임 초기화 및 힌트 (14개)

#### 게임 초기화
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `game-initialization` | initializeGame - 정상 초기화 | GameServiceTest.java:39 |
| `game-initialization` | initializeGame - 턴 순서 랜덤화 | GameServiceTest.java:58 |
| `game-initialization` | initializeGame - 관전자 제외 | GameServiceTest.java:75 |

#### 초성 힌트
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `game-hint-chosung` | provideChosungHint - 정상 제공 | GameServiceTest.java:99 |
| `game-hint-chosung` | provideChosungHint - 게임 상태 없음 | GameServiceTest.java:120 |
| `game-hint-chosung` | provideChosungHint - 출제자 아님 | GameServiceTest.java:133 |
| `game-hint-chosung` | provideChosungHint - 잘못된 페이즈 | GameServiceTest.java:150 |
| `game-hint-chosung` | provideChosungHint - 힌트 레벨 낮음 | GameServiceTest.java:168 |
| `game-hint-chosung` | provideChosungHint - 더 이상 공개 불가 | GameServiceTest.java:186 |

#### 글자 힌트
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `game-hint-letter` | provideLetterHint - 정상 제공 | GameServiceTest.java:205 |
| `game-hint-letter` | provideLetterHint - 게임 상태 없음 | GameServiceTest.java:226 |
| `game-hint-letter` | provideLetterHint - 출제자 아님 | GameServiceTest.java:240 |
| `game-hint-letter` | provideLetterHint - 잘못된 페이즈 | GameServiceTest.java:256 |
| `game-hint-letter` | provideLetterHint - 힌트 레벨 낮음 | GameServiceTest.java:274 |

---

### GamePhaseManager - 게임 페이즈 관리 (14개)

#### 턴 시작
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `phase-turn-start` | startFirstTurn - 첫 턴 시작 | GamePhaseManagerTest.java:62 |
| `phase-turn-start` | startFirstTurn - RoomInfo 없음 | GamePhaseManagerTest.java:88 |
| `phase-turn-start` | startDrawingPhase - DRAWING 전환 | GamePhaseManagerTest.java:104 |
| `phase-turn-start` | startNextTurn - 다음 턴 시작 | GamePhaseManagerTest.java:127 |
| `phase-turn-start` | startNextTurn - 라운드 변경 | GamePhaseManagerTest.java:152 |
| `phase-turn-start` | startNextTurn - 마지막 턴 게임 종료 | GamePhaseManagerTest.java:175 |

#### 게임 종료
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `phase-game-end` | endGame - GAME_END 전환 | GamePhaseManagerTest.java:196 |

#### 자동 힌트
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `phase-auto-hint` | provideAutoHint - 레벨1 글자수 | GamePhaseManagerTest.java:217 |
| `phase-auto-hint` | provideAutoHint - 레벨2 초성 | GamePhaseManagerTest.java:239 |
| `phase-auto-hint` | provideAutoHint - 레벨2 더 이상 없음 | GamePhaseManagerTest.java:264 |

#### 이벤트 리스너
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `event-listener` | onCountdownCompleted - 카운트다운 완료 시 첫 턴 시작 | GamePhaseManagerTest.java:293 |
| `event-listener` | onWordSelectTimeout - 단어 선택 타임아웃 시 그리기 페이즈 시작 | GamePhaseManagerTest.java:319 |
| `event-listener` | onHintTime - 힌트 시간 도달 시 자동 힌트 제공 | GamePhaseManagerTest.java:342 |
| `event-listener` | onTurnResultEnd - 턴 결과 종료 시 다음 턴 시작 | GamePhaseManagerTest.java:369 |

---

### RoomService - 방 관리 (19개)

#### 방 생성
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `room-create` | createRoom - 공개방 생성 | RoomServiceTest.java:37 |
| `room-create` | createRoom - 비공개방 생성 | RoomServiceTest.java:56 |

#### 랜덤 방 찾기
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `room-random-match` | findRandomPublicRoom - 사용 가능한 방 반환 | RoomServiceTest.java:72 |
| `room-random-match` | findRandomPublicRoom - 사용 가능한 방 없음 | RoomServiceTest.java:91 |

#### 방 입장 가능 여부
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `room-availability` | isRoomAvailable - 공개방, WAITING, 자리 있음 | RoomServiceTest.java:109 |
| `room-availability` | isRoomAvailable - 비공개방 | RoomServiceTest.java:125 |
| `room-availability` | isRoomAvailable - PLAYING 상태 | RoomServiceTest.java:141 |
| `room-availability` | isRoomAvailable - 방 가득참 | RoomServiceTest.java:157 |
| `room-availability` | countParticipants - 관전자 제외 | RoomServiceTest.java:173 |

#### 방 목록 및 검색
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `room-list` | getPublicRoomList - 공개방만 필터링 | RoomServiceTest.java:196 |
| `room-list` | getPublicRoomList - 최신순 정렬 | RoomServiceTest.java:325 |
| `room-list` | getPublicRoomList - 게임 진행 중 라운드 정보 포함 | RoomServiceTest.java:349 |
| `room-list` | getPublicRoomList - 대기 중 라운드 정보 null | RoomServiceTest.java:373 |
| `room-search` | findRoomByInviteCode - 초대코드로 찾기 | RoomServiceTest.java:215 |
| `room-search` | findRoomByInviteCode - 못 찾음 | RoomServiceTest.java:233 |

#### 방 관리
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `room-management` | getRoomInfo - 방 정보 조회 | RoomServiceTest.java:262 |
| `room-management` | getRoomInfo - 방 없음 null 반환 | RoomServiceTest.java:280 |
| `room-management` | saveRoomInfo - 방 정보 저장 | RoomServiceTest.java:295 |
| `room-management` | deleteRoom - 방 삭제 | RoomServiceTest.java:310 |

---

### RoomPlayerService - 방 플레이어 관리 (17개)

#### 방 입장
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `room-join` | joinRoom - 정상 입장 PLAYER 역할 | RoomPlayerServiceTest.java:41 |
| `room-join` | joinRoom - 중복 입장 방지 | RoomPlayerServiceTest.java:76 |
| `room-join` | joinRoom - 게임 진행 중 SPECTATOR 입장 | RoomPlayerServiceTest.java:107 |
| `room-join` | joinRoom - 최대 참가자 수 초과 SPECTATOR 입장 | RoomPlayerServiceTest.java:137 |
| `room-join` | joinRoom - 방 없음 예외 | RoomPlayerServiceTest.java:167 |

#### 방 퇴장
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `room-leave` | leaveRoom - 정상 퇴장 | RoomPlayerServiceTest.java:184 |
| `room-leave` | leaveRoom - 방장 퇴장 시 방장 위임 | RoomPlayerServiceTest.java:215 |
| `room-leave` | leaveRoom - 마지막 플레이어 퇴장 시 방 삭제 | RoomPlayerServiceTest.java:258 |
| `room-leave` | leaveRoom - 방 없음 null 반환 | RoomPlayerServiceTest.java:280 |
| `room-leave` | leaveRoom - 플레이어 없음 null 반환 | RoomPlayerServiceTest.java:295 |

#### 역할 변경
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `role-change` | changeRole - 역할 변경 및 준비 해제 | RoomPlayerServiceTest.java:310 |
| `role-change` | changeRole - 방 없음 예외 | RoomPlayerServiceTest.java:333 |
| `role-change` | changeRole - 플레이어 없음 예외 | RoomPlayerServiceTest.java:348 |

#### 준비 상태 토글
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `ready-toggle` | toggleReady - 준비 상태 토글 | RoomPlayerServiceTest.java:363 |
| `ready-toggle` | toggleReady - 방장은 토글 불가 | RoomPlayerServiceTest.java:392 |
| `ready-toggle` | toggleReady - 방 없음 예외 | RoomPlayerServiceTest.java:415 |
| `ready-toggle` | toggleReady - 플레이어 없음 예외 | RoomPlayerServiceTest.java:430 |

---

### RoomGameService - 방 게임 관리 (15개)

#### 게임 시작 및 설정
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `game-start` | startGame - 게임 시작 및 PLAYING 상태 전환 | RoomGameServiceTest.java:51 |
| `game-start` | startGame - 방 없음 예외 | RoomGameServiceTest.java:70 |
| `settings-update` | updateSettings - 방장이 설정 변경 | RoomGameServiceTest.java:82 |
| `settings-update` | updateSettings - 방장 아님 변경 불가 | RoomGameServiceTest.java:105 |
| `settings-update` | updateSettings - 방 없음 예외 | RoomGameServiceTest.java:125 |

#### 대기방 복귀
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `return-to-waiting` | returnToWaitingRoom - WAITING 전환 및 준비 초기화 | RoomGameServiceTest.java:141 |
| `return-to-waiting` | returnToWaitingRoom - 이미 WAITING 상태 무시 | RoomGameServiceTest.java:162 |
| `return-to-waiting` | returnToWaitingRoom - 방 없음 예외 | RoomGameServiceTest.java:178 |

#### 플레이어 복귀 처리
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `player-return` | handlePlayerReturnToWaiting - 원래 방장 복귀 시 권한 유지 | RoomGameServiceTest.java:192 |
| `player-return` | handlePlayerReturnToWaiting - 참가자 먼저 복귀 | RoomGameServiceTest.java:217 |
| `player-return` | handlePlayerReturnToWaiting - 관전자 첫 복귀 불가 | RoomGameServiceTest.java:238 |
| `player-return` | handlePlayerReturnToWaiting - 중복 복귀 방지 | RoomGameServiceTest.java:265 |
| `player-return` | handlePlayerReturnToWaiting - 첫 복귀자 방 WAITING 전환 | RoomGameServiceTest.java:285 |
| `player-return` | handlePlayerReturnToWaiting - 원래 방장 나중 복귀 시 권한 복원 | RoomGameServiceTest.java:306 |
| `player-return` | handlePlayerReturnToWaiting - 방 없음 예외 | RoomGameServiceTest.java:357 |

---

### TurnManager - 턴 관리 (4개)

| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `turn-end` | endTurn - TIME_OUT | TurnManagerTest.java:39 |
| `turn-end` | endTurn - ALL_CORRECT | TurnManagerTest.java:58 |
| `turn-end` | endTurn - DRAWER_LEFT | TurnManagerTest.java:77 |
| `turn-end` | endTurn - phaseStartTime 설정 | TurnManagerTest.java:94 |

---

### WordService - 단어 관리 (15개)

#### 단어 로딩
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `word-load` | loadWords - 모든 난이도 로드 | WordServiceTest.java:23 |

#### 랜덤 단어
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `word-random` | getRandomWords - 요청 개수 반환 | WordServiceTest.java:34 |
| `word-random` | getRandomWords - 전체보다 많이 요청 | WordServiceTest.java:46 |
| `word-random` | getRandomWords - 중복 없이 반환 | WordServiceTest.java:60 |
| `word-random` | getRandomWords - 빈 리스트 요청 | WordServiceTest.java:71 |
| `word-random` | getRandomWords - 다른 순서로 반환 | WordServiceTest.java:81 |

#### 난이도별 혼합
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `word-mixed` | getMixedWords - 요청 개수 반환 | WordServiceTest.java:94 |
| `word-mixed` | getMixedWords - 3개 요청 시 비율 | WordServiceTest.java:106 |
| `word-mixed` | getMixedWords - 5개 요청 시 비율 | WordServiceTest.java:119 |
| `word-mixed` | getMixedWords - 10개 요청 시 비율 | WordServiceTest.java:132 |
| `word-mixed` | getMixedWords - 중복 없이 반환 | WordServiceTest.java:145 |
| `word-mixed` | getMixedWords - 빈 리스트 요청 | WordServiceTest.java:156 |
| `word-mixed` | getMixedWords - 1개 요청 시 처리 | WordServiceTest.java:166 |
| `word-mixed` | getMixedWords - 다른 순서로 반환 | WordServiceTest.java:179 |
| `word-mixed` | getMixedWords - 큰 숫자 요청 | WordServiceTest.java:189 |

---

## 🌐 Controller 레이어 테스트 (72개)

### REST API Controllers (40개)

#### WordController - 단어 API (5개)

| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `api-word` | getRandomWords - 랜덤 단어 반환 | WordControllerTest.java:28 |
| `api-word` | getRandomWords - 기본값 3 사용 | WordControllerTest.java:50 |
| `api-word` | getRandomWords - 커스텀 개수 | WordControllerTest.java:65 |
| `api-word` | getRandomWords - 0개 요청 | WordControllerTest.java:80 |
| `api-word` | getRandomWords - 큰 값 요청 | WordControllerTest.java:95 |

---

#### PlayerController - 플레이어 API (6개)

| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `api-player-join` | join - 정상 가입 | PlayerControllerTest.java:33 |
| `api-player-join` | join - 한글 닉네임 | PlayerControllerTest.java:61 |
| `api-player-join` | join - 영문 닉네임 | PlayerControllerTest.java:81 |
| `api-player-join` | join - 숫자 포함 닉네임 | PlayerControllerTest.java:101 |
| `api-player-join` | join - 2자 닉네임 | PlayerControllerTest.java:136 |
| `api-player-join` | join - 10자 닉네임 | PlayerControllerTest.java:152 |

---

#### GameController - 게임 API (7개)

| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `api-game-state` | getGameState - 정상 반환 | GameControllerTest.java:51 |
| `api-game-state` | getGameState - 404 반환 | GameControllerTest.java:70 |
| `api-game-drawing` | uploadDrawing - 정상 업로드 | GameControllerTest.java:80 |
| `api-game-drawing` | uploadDrawing - 게임 상태 없음 | GameControllerTest.java:107 |
| `api-game-drawing` | uploadDrawing - 잘못된 페이즈 | GameControllerTest.java:126 |
| `api-game-drawing` | uploadDrawing - 출제자 아님 403 | GameControllerTest.java:145 |
| `api-game-drawing` | uploadDrawing - playerId 추가 | GameControllerTest.java:172 |

---

#### RoomController - 방 API (11개)

| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `api-room-create` | createRoom - 공개방 생성 | RoomControllerTest.java:53 |
| `api-room-create` | createRoom - 비공개방 생성 | RoomControllerTest.java:78 |
| `api-room-info` | getRoomInfo - 방 정보 반환 | RoomControllerTest.java:103 |
| `api-room-info` | getRoomInfo - 404 반환 | RoomControllerTest.java:120 |
| `api-room-join` | joinRoom - 방 입장 | RoomControllerTest.java:132 |
| `api-room-leave` | leaveRoom - 방 퇴장 | RoomControllerTest.java:154 |
| `api-room-list` | getRoomList - 공개방 목록 | RoomControllerTest.java:182 |
| `api-room-random` | joinRandomRoom - 랜덤 입장 | RoomControllerTest.java:213 |
| `api-room-random` | joinRandomRoom - 사용 가능한 방 없음 | RoomControllerTest.java:237 |
| `api-room-invite` | joinByInviteCode - 초대코드 입장 | RoomControllerTest.java:255 |
| `api-room-invite` | joinByInviteCode - 404 반환 | RoomControllerTest.java:285 |

---

### WebSocket Controllers (32개)

#### WebSocketGameController - 게임 WebSocket (18개)

##### 단어 및 그림
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `ws-game-word` | selectWord - 단어 선택 처리 | WebSocketGameControllerTest.java:62 |
| `ws-game-draw` | drawPath - 정상 브로드캐스트 | WebSocketGameControllerTest.java:77 |
| `ws-game-draw` | drawPath - 게임 상태 없음 | WebSocketGameControllerTest.java:96 |
| `ws-game-draw` | drawPath - 잘못된 페이즈 | WebSocketGameControllerTest.java:114 |
| `ws-game-draw` | drawPath - 출제자 아님 무시 | WebSocketGameControllerTest.java:133 |
| `ws-game-clear` | clearCanvas - 정상 브로드캐스트 | WebSocketGameControllerTest.java:151 |
| `ws-game-clear` | clearCanvas - 출제자 아님 무시 | WebSocketGameControllerTest.java:165 |

##### 그림 스트리밍
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `ws-game-streaming` | streamDrawing - 정상 브로드캐스트 | WebSocketGameControllerTest.java:183 |
| `ws-game-streaming` | streamDrawing - 20개마다 저장 | WebSocketGameControllerTest.java:202 |

##### 힌트 제공
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `ws-game-hint` | provideChosungHint - 정상 제공 | WebSocketGameControllerTest.java:223 |
| `ws-game-hint` | provideChosungHint - 실패 시 무시 | WebSocketGameControllerTest.java:238 |
| `ws-game-hint` | provideLetterHint - 정상 제공 | WebSocketGameControllerTest.java:253 |

##### 투표 시스템
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `ws-game-vote` | handleVote - 좋아요 처리 | WebSocketGameControllerTest.java:268 |
| `ws-game-vote` | handleVote - 게임 상태 없음 | WebSocketGameControllerTest.java:286 |
| `ws-game-vote` | handleVote - 잘못된 페이즈 무시 | WebSocketGameControllerTest.java:301 |
| `ws-game-vote` | handleVote - 출제자 투표 불가 | WebSocketGameControllerTest.java:320 |
| `ws-game-vote` | handleVote - 관전자 투표 불가 | WebSocketGameControllerTest.java:339 |

---

#### WebSocketRoomController - 방 WebSocket (14개)

##### 세션 관리
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `ws-room-session` | registerSession - 세션 등록 | WebSocketRoomControllerTest.java:70 |
| `ws-room-session` | registerSession - 게임 진행 중 상태 전송 | WebSocketRoomControllerTest.java:87 |
| `ws-room-session` | registerSession - 대기 중 상태 미전송 | WebSocketRoomControllerTest.java:108 |

##### 방 정보 및 설정
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `ws-room-info` | updateRoomInfo - 정보 브로드캐스트 | WebSocketRoomControllerTest.java:124 |
| `ws-room-info` | updateRoomInfo - 방 없으면 무시 | WebSocketRoomControllerTest.java:137 |
| `ws-room-ready` | toggleReady - 준비 상태 토글 | WebSocketRoomControllerTest.java:151 |
| `ws-room-role` | changeRole - 역할 변경 | WebSocketRoomControllerTest.java:172 |
| `ws-room-settings` | updateSettings - 설정 업데이트 | WebSocketRoomControllerTest.java:191 |

##### 게임 시작 및 채팅
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `ws-room-game` | startGame - 게임 시작 | WebSocketRoomControllerTest.java:214 |
| `ws-room-chat` | sendChat - 타임스탬프 포함 전송 | WebSocketRoomControllerTest.java:232 |

##### 대기실 복귀
| 태그 | 테스트명 | 파일 위치 |
|-----|---------|----------|
| `ws-room-return` | returnToWaitingRoom - 정상 처리 | WebSocketRoomControllerTest.java:256 |
| `ws-room-return` | returnToWaitingRoom - 실패 시 에러 전송 | WebSocketRoomControllerTest.java:270 |

---

## 📊 태그별 통계

### 도메인별
- `hint-*`: 26개 (힌트 시스템)
- `scoring-*`: 31개 (점수 계산)
- `answer-*`: 11개 (정답 검증)
- `game-*`: 27개 (게임 로직)
- `phase-*`: 14개 (페이즈 관리)
- `room-*`: 51개 (방 관리)
- `turn-*`: 4개 (턴 관리)
- `word-*`: 15개 (단어 관리)
- `api-*`: 40개 (REST API)
- `ws-*`: 32개 (WebSocket)
- `event-*`: 4개 (이벤트 리스너)
- `player-*`: 15개 (플레이어 관리)
- `settings-*`: 3개 (설정 관리)
- `return-*`: 3개 (대기방 복귀)

### 레이어별
- `service`: 149개 (비즈니스 로직)
- `controller`: 72개 (API 엔드포인트)

---

## 🔍 빠른 검색

### 기능별 테스트 찾기

```bash
# 힌트 관련 모든 테스트
./gradlew test -Dgroups="hint-*"

# 점수 계산 관련 모든 테스트
./gradlew test -Dgroups="scoring-*"

# REST API 모든 테스트
./gradlew test -Dgroups="api-*"

# WebSocket 모든 테스트
./gradlew test -Dgroups="ws-*"

# 방 관련 모든 테스트 (서비스 + API + WebSocket)
./gradlew test -Dgroups="room-*,api-room-*,ws-room-*"
```

### 문제 영역별 테스트

```bash
# 초성 힌트 문제 발생 시
./gradlew test -Dgroups="hint-korean-chosung,ws-game-hint"

# 점수 계산 문제 발생 시
./gradlew test -Dgroups="scoring-*"

# 방 입장 문제 발생 시
./gradlew test -Dgroups="room-create,room-availability,api-room-join"

# 게임 시작 문제 발생 시
./gradlew test -Dgroups="phase-turn-start,ws-room-game"
```

---

## 📝 업데이트 이력

- 2025-12-05: 방 관리 서비스 테스트 추가 (221개 테스트)
  - RoomPlayerService 테스트 17개 추가 (신규)
  - RoomGameService 테스트 15개 추가 (신규)
  - GamePhaseManager 이벤트 리스너 테스트 4개 추가
  - RoomService 테스트 7개 추가
- 2025-12-04: 초기 테스트 인덱스 생성 (180개 테스트)
  - 모든 서비스 레이어 테스트 완료
  - 모든 Controller 레이어 테스트 완료
  - @Tag 어노테이션 기반 테스트 그룹화

---

## 💡 팁

1. **특정 기능만 빠르게 테스트**: `@Tag`를 활용하여 관련 테스트만 실행
2. **CI/CD 파이프라인**: 중요도가 높은 태그를 우선 실행하여 빠른 피드백
3. **리팩토링 시**: 변경된 영역의 태그로 회귀 테스트 실행
4. **새 기능 개발**: 관련 태그의 기존 테스트를 참고하여 일관성 유지

---

**전체 테스트 실행**: `./gradlew test`
**테스트 리포트**: `build/reports/tests/test/index.html`
