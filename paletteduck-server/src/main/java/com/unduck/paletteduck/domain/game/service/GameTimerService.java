package com.unduck.paletteduck.domain.game.service;

import com.unduck.paletteduck.config.constants.WebSocketTopics;
import com.unduck.paletteduck.domain.game.constants.GameConstants;
import com.unduck.paletteduck.domain.game.dto.GamePhase;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.dto.TurnEndReason;
import com.unduck.paletteduck.domain.game.dto.TurnInfo;
import com.unduck.paletteduck.domain.game.repository.GameRepository;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.domain.room.service.RoomService;
import com.unduck.paletteduck.domain.word.service.WordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameTimerService {

    private final GameRepository gameRepository;
    private final RoomService roomService;
    private final WordService wordService;
    private final SimpMessagingTemplate messagingTemplate;
    private final HintService hintService;
    private GameTimerService self;

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    public void setSelf(GameTimerService self) {
        this.self = self;
    }

    @Async
    public void startCountdown(String roomId) {
        try {
            TimeUnit.SECONDS.sleep(GameConstants.Timing.COUNTDOWN_TIME);

            GameState gameState = gameRepository.findById(roomId);
            if (gameState == null) {
                log.warn("GameState not found after countdown - roomId: {}", roomId);
                return;
            }

            startFirstTurn(roomId, gameState);
        } catch (InterruptedException e) {
            log.error("Countdown interrupted - roomId: {}", roomId, e);
            Thread.currentThread().interrupt();
        }
    }

    private void startFirstTurn(String roomId, GameState gameState) {
        RoomInfo roomInfo = roomService.getRoomInfo(roomId);
        if (roomInfo == null) {
            log.warn("RoomInfo not found - roomId: {}", roomId);
            return;
        }

        // 첫 번째 출제자 지정
        String drawerId = gameState.getTurnOrder().get(0);
        String drawerNickname = roomInfo.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(drawerId))
                .map(p -> p.getNickname())
                .findFirst()
                .orElse("Unknown");

        // 단어 선택지 생성
        int wordChoiceCount = roomInfo.getSettings().getWordChoices();
        List<String> wordChoices = wordService.getMixedWords(wordChoiceCount);

        // 턴 정보 생성
        TurnInfo turnInfo = new TurnInfo(1, drawerId, drawerNickname);
        turnInfo.setWordChoices(wordChoices);
        turnInfo.setTimeLeft(GameConstants.Timing.WORD_SELECT_TIME);

        // GameState 업데이트
        gameState.setPhase(GamePhase.WORD_SELECT);
        gameState.setCurrentTurn(turnInfo);
        gameState.setPhaseStartTime(System.currentTimeMillis());

        gameRepository.save(roomId, gameState);

        // 브로드캐스트
        messagingTemplate.convertAndSend(WebSocketTopics.gameState(roomId), gameState);

        log.info("First turn started - room: {}, drawer: {}, words: {}",
                roomId, drawerNickname, wordChoices);

        // 단어 선택 타이머 시작
        self.startWordSelectTimer(roomId, gameState.getGameSessionId(), 1);
    }

    @Async
    public void startWordSelectTimer(String roomId, String gameSessionId, int turnNumber) {
        try {
            TimeUnit.SECONDS.sleep(GameConstants.Timing.WORD_SELECT_TIME);

            GameState gameState = gameRepository.findById(roomId);
            if (gameState == null || gameState.getCurrentTurn() == null) {
                return;
            }

            // ✅ 게임 세션이 다르면 무시 (게임 재시작된 경우)
            if (!gameSessionId.equals(gameState.getGameSessionId())) {
                log.debug("Word select timer expired for old game session - room: {}, timer session: {}, current session: {}",
                    roomId, gameSessionId, gameState.getGameSessionId());
                return;
            }

            // ✅ 턴 번호가 다르면 무시 (다음 턴으로 이미 넘어간 경우)
            if (gameState.getCurrentTurn().getTurnNumber() != turnNumber) {
                log.debug("Word select timer expired for old turn - room: {}, timer turn: {}, current turn: {}",
                    roomId, turnNumber, gameState.getCurrentTurn().getTurnNumber());
                return;
            }

            // ✅ 이미 다른 단계로 넘어갔으면 무시 (단어를 일찍 선택한 경우)
            if (gameState.getPhase() != GamePhase.WORD_SELECT) {
                log.debug("Word select timer expired but phase already changed - room: {}, phase: {}",
                    roomId, gameState.getPhase());
                return;
            }

            // 단어 선택 안 했으면 랜덤 선택
            if (gameState.getCurrentTurn().getWord() == null) {
                List<String> choices = gameState.getCurrentTurn().getWordChoices();
                Collections.shuffle(choices);
                String randomWord = choices.get(0);
                gameState.getCurrentTurn().setWord(randomWord);

                log.info("Word auto-selected - room: {}, word: {}", roomId, randomWord);
            }

            // 그리기 단계로 전환
            startDrawingPhase(roomId, gameState);

        } catch (InterruptedException e) {
            log.error("Word select timer interrupted - roomId: {}", roomId, e);
            Thread.currentThread().interrupt();
        }
    }

    private void startDrawingPhase(String roomId, GameState gameState) {
        gameState.setPhase(GamePhase.DRAWING);
        gameState.setPhaseStartTime(System.currentTimeMillis());
        gameState.getCurrentTurn().setTimeLeft(gameState.getDrawTime());
        gameState.getCurrentTurn().setWordChoices(List.of()); // 선택지 제거
        gameState.getCurrentTurn().setHintLevel(0); // 힌트 레벨 초기화
        gameState.getCurrentTurn().setCurrentHint(null); // 힌트 초기화
        gameState.getCurrentTurn().setHintArray(null); // 힌트 배열 초기화
        gameState.getCurrentTurn().getRevealedChosungPositions().clear(); // 공개된 초성 위치 초기화
        gameState.getCurrentTurn().getRevealedLetterPositions().clear(); // 공개된 글자 위치 초기화

        gameRepository.save(roomId, gameState);

        // 브로드캐스트
        messagingTemplate.convertAndSend(WebSocketTopics.gameState(roomId), gameState);

        log.info("Drawing phase started - room: {}, word: {}, time: {}s",
                roomId, gameState.getCurrentTurn().getWord(), gameState.getDrawTime());

        String gameSessionId = gameState.getGameSessionId();
        int turnNumber = gameState.getCurrentTurn().getTurnNumber();
        int drawTime = gameState.getDrawTime();

        // 그리기 타이머 시작
        self.startDrawingTimer(roomId, gameSessionId, turnNumber, drawTime);

        // 힌트 타이머 시작 (그리기 시간이 충분히 긴 경우에만)
        if (drawTime >= GameConstants.Timing.FIRST_HINT_DELAY) {
            log.info("Starting first hint timer - room: {}, delay: {}s", roomId, GameConstants.Timing.FIRST_HINT_DELAY);
            self.startHintTimer(roomId, gameSessionId, turnNumber, 1, GameConstants.Timing.FIRST_HINT_DELAY);
        } else {
            log.debug("Skipping first hint (drawTime: {}s < required: {}s)", drawTime, GameConstants.Timing.FIRST_HINT_DELAY);
        }
        if (drawTime >= GameConstants.Timing.SECOND_HINT_DELAY) {
            log.info("Starting second hint timer - room: {}, delay: {}s", roomId, GameConstants.Timing.SECOND_HINT_DELAY);
            self.startHintTimer(roomId, gameSessionId, turnNumber, 2, GameConstants.Timing.SECOND_HINT_DELAY);
        } else {
            log.debug("Skipping second hint (drawTime: {}s < required: {}s)", drawTime, GameConstants.Timing.SECOND_HINT_DELAY);
        }
    }

    @Async
    public void startDrawingTimer(String roomId, String gameSessionId, int turnNumber, int drawTime) {
        try {
            TimeUnit.SECONDS.sleep(drawTime);

            GameState gameState = gameRepository.findById(roomId);
            if (gameState == null || gameState.getCurrentTurn() == null) {
                return;
            }

            // ✅ 게임 세션이 다르면 무시 (게임 재시작된 경우)
            if (!gameSessionId.equals(gameState.getGameSessionId())) {
                log.debug("Drawing timer expired for old game session - room: {}, timer session: {}, current session: {}",
                    roomId, gameSessionId, gameState.getGameSessionId());
                return;
            }

            // ✅ 턴 번호가 다르면 무시 (다음 턴으로 이미 넘어간 경우)
            if (gameState.getCurrentTurn().getTurnNumber() != turnNumber) {
                log.debug("Drawing timer expired for old turn - room: {}, timer turn: {}, current turn: {}",
                    roomId, turnNumber, gameState.getCurrentTurn().getTurnNumber());
                return;
            }

            // ✅ 이미 다른 단계로 넘어갔으면 무시 (모든 플레이어가 정답을 맞춘 경우 등)
            if (gameState.getPhase() != GamePhase.DRAWING) {
                log.debug("Drawing timer expired but phase already changed - room: {}, phase: {}",
                    roomId, gameState.getPhase());
                return;
            }

            log.info("Drawing time ended - room: {}", roomId);
            endTurn(roomId, gameState, TurnEndReason.TIME_OUT);

        } catch (InterruptedException e) {
            log.error("Drawing timer interrupted - roomId: {}", roomId, e);
            Thread.currentThread().interrupt();
        }
    }

    public void endTurn(String roomId, GameState gameState, TurnEndReason reason) {
        // 턴 종료 사유 설정
        if (gameState.getCurrentTurn() != null) {
            gameState.getCurrentTurn().setTurnEndReason(reason);
        }

        // 턴 결과 단계로 전환
        gameState.setPhase(GamePhase.TURN_RESULT);
        gameState.setPhaseStartTime(System.currentTimeMillis());

        gameRepository.save(roomId, gameState);

        // 턴 결과 브로드캐스트
        messagingTemplate.convertAndSend(WebSocketTopics.gameState(roomId), gameState);

        log.info("Turn ended, showing results - room: {}, turn: {}, reason: {}",
                roomId, gameState.getCurrentTurn().getTurnNumber(), reason);

        // 턴 결과 표시 후 다음 턴 준비
        self.scheduleTurnResultEnd(roomId);
    }

    @Async
    public void scheduleTurnResultEnd(String roomId) {
        try {
            // 5초간 턴 결과 표시
            TimeUnit.MILLISECONDS.sleep(GameConstants.Timing.ROUND_END_DELAY);

            GameState gameState = gameRepository.findById(roomId);
            if (gameState == null) {
                return;
            }

            // 바로 다음 턴 시작 (추가 delay 없이)
            startNextTurn(roomId, gameState);

        } catch (InterruptedException e) {
            log.error("Turn result display interrupted - roomId: {}", roomId, e);
            Thread.currentThread().interrupt();
        }
    }

    @Async
    public void scheduleNextTurn(String roomId) {
        try {
            TimeUnit.MILLISECONDS.sleep(GameConstants.Timing.ROUND_END_DELAY);

            GameState gameState = gameRepository.findById(roomId);
            if (gameState == null) {
                return;
            }

            startNextTurn(roomId, gameState);

        } catch (InterruptedException e) {
            log.error("Next turn scheduling interrupted - roomId: {}", roomId, e);
            Thread.currentThread().interrupt();
        }
    }

    private void startNextTurn(String roomId, GameState gameState) {
        RoomInfo roomInfo = roomService.getRoomInfo(roomId);
        if (roomInfo == null) {
            log.warn("RoomInfo not found - roomId: {}", roomId);
            return;
        }

        int currentTurnNumber = gameState.getCurrentTurn().getTurnNumber();
        List<String> turnOrder = gameState.getTurnOrder();

        // 다음 턴 번호 계산
        int nextTurnNumber = currentTurnNumber + 1;
        int totalTurns = turnOrder.size() * gameState.getTotalRounds();

        // 게임 종료 체크
        if (nextTurnNumber > totalTurns) {
            endGame(roomId, gameState);
            return;
        }

        // 라운드 업데이트
        int currentRound = ((nextTurnNumber - 1) / turnOrder.size()) + 1;
        gameState.setCurrentRound(currentRound);

        // 다음 출제자 결정
        int drawerIndex = (nextTurnNumber - 1) % turnOrder.size();
        String drawerId = turnOrder.get(drawerIndex);
        String drawerNickname = roomInfo.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(drawerId))
                .map(p -> p.getNickname())
                .findFirst()
                .orElse("Unknown");

        // 단어 선택지 생성
        int wordChoiceCount = roomInfo.getSettings().getWordChoices();
        List<String> wordChoices = wordService.getMixedWords(wordChoiceCount);

        // 새 턴 정보 생성
        TurnInfo newTurnInfo = new TurnInfo(nextTurnNumber, drawerId, drawerNickname);
        newTurnInfo.setWordChoices(wordChoices);
        newTurnInfo.setTimeLeft(GameConstants.Timing.WORD_SELECT_TIME);

        // 모든 플레이어의 정답 상태 초기화
        if (gameState.getPlayers() != null) {
            gameState.getPlayers().forEach(p -> p.setIsCorrect(false));
        }

        // GameState 업데이트
        gameState.setPhase(GamePhase.WORD_SELECT);
        gameState.setCurrentTurn(newTurnInfo);
        gameState.setPhaseStartTime(System.currentTimeMillis());

        gameRepository.save(roomId, gameState);

        // 브로드캐스트
        messagingTemplate.convertAndSend(WebSocketTopics.gameState(roomId), gameState);

        log.info("Next turn started - room: {}, turn: {}/{}, round: {}/{}, drawer: {}",
                roomId, nextTurnNumber, totalTurns, currentRound, gameState.getTotalRounds(), drawerNickname);

        // 단어 선택 타이머 시작
        self.startWordSelectTimer(roomId, gameState.getGameSessionId(), nextTurnNumber);
    }

    public void endGame(String roomId, GameState gameState) {
        gameState.setPhase(GamePhase.GAME_END);
        gameState.setPhaseStartTime(System.currentTimeMillis());

        gameRepository.save(roomId, gameState);

        // 게임 종료 브로드캐스트
        messagingTemplate.convertAndSend(WebSocketTopics.gameState(roomId), gameState);

        log.info("Game ended - room: {}", roomId);
    }

    @Async
    public void startHintTimer(String roomId, String gameSessionId, int turnNumber, int hintLevel, int delaySeconds) {
        try {
            TimeUnit.SECONDS.sleep(delaySeconds);

            GameState gameState = gameRepository.findById(roomId);
            if (gameState == null || gameState.getCurrentTurn() == null) {
                return;
            }

            // 게임 세션이 다르면 무시 (게임 재시작된 경우)
            if (!gameSessionId.equals(gameState.getGameSessionId())) {
                log.debug("Hint timer expired for old game session - room: {}, timer session: {}, current session: {}",
                    roomId, gameSessionId, gameState.getGameSessionId());
                return;
            }

            // 턴 번호가 다르면 무시 (다음 턴으로 이미 넘어간 경우)
            if (gameState.getCurrentTurn().getTurnNumber() != turnNumber) {
                log.debug("Hint timer expired for old turn - room: {}, timer turn: {}, current turn: {}",
                    roomId, turnNumber, gameState.getCurrentTurn().getTurnNumber());
                return;
            }

            // 이미 다른 단계로 넘어갔으면 무시
            if (gameState.getPhase() != GamePhase.DRAWING) {
                log.debug("Hint timer expired but phase already changed - room: {}, phase: {}",
                    roomId, gameState.getPhase());
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

                log.info("Auto hint (word length) - room: {}, turn: {}, length: {}", roomId, turnNumber, word.length());
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

                    log.info("Auto hint (chosung) - room: {}, turn: {}, hint: {}", roomId, turnNumber, hint);
                }
            }

        } catch (InterruptedException e) {
            log.error("Hint timer interrupted - roomId: {}, hintLevel: {}", roomId, hintLevel, e);
            Thread.currentThread().interrupt();
        }
    }

    public void selectWord(String roomId, String playerId, String word) {
        GameState gameState = gameRepository.findById(roomId);
        if (gameState == null || gameState.getCurrentTurn() == null) {
            return;
        }

        // 출제자 본인인지 확인
        if (!gameState.getCurrentTurn().getDrawerId().equals(playerId)) {
            log.warn("Unauthorized word selection - playerId: {}, drawer: {}",
                    playerId, gameState.getCurrentTurn().getDrawerId());
            return;
        }

        // 선택지에 있는 단어인지 확인, 또는 직접 입력한 유효한 단어인지 확인
        boolean isInWordChoices = gameState.getCurrentTurn().getWordChoices().contains(word);
        boolean isValidCustomWord = word != null && word.matches("^[가-힣ㄱ-ㅎㅏ-ㅣ]{2,10}$");

        if (!isInWordChoices && !isValidCustomWord) {
            log.warn("Invalid word selection - word: {}, isInChoices: {}, isValidCustom: {}",
                    word, isInWordChoices, isValidCustomWord);
            return;
        }

        gameState.getCurrentTurn().setWord(word);
        gameRepository.save(roomId, gameState);

        if (isInWordChoices) {
            log.info("Word selected from choices - room: {}, word: {}", roomId, word);
        } else {
            log.info("Custom word selected - room: {}, word: {}", roomId, word);
        }

        // 즉시 그리기 단계로 전환
        startDrawingPhase(roomId, gameState);
    }
}