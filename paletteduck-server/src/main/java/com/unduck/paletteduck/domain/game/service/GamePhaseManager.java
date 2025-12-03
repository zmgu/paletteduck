package com.unduck.paletteduck.domain.game.service;

import com.unduck.paletteduck.config.constants.WebSocketTopics;
import com.unduck.paletteduck.domain.game.constants.GameConstants;
import com.unduck.paletteduck.domain.game.dto.GamePhase;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.dto.TurnInfo;
import com.unduck.paletteduck.domain.game.event.*;
import com.unduck.paletteduck.domain.game.repository.GameRepository;
import com.unduck.paletteduck.domain.room.dto.ReturnToWaitingTracker;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.domain.room.dto.RoomPlayer;
import com.unduck.paletteduck.domain.room.repository.ReturnToWaitingTrackerRepository;
import com.unduck.paletteduck.domain.room.service.RoomService;
import com.unduck.paletteduck.domain.word.service.WordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 게임 페이즈 관리 서비스
 * 게임 진행 중 페이즈 전환 로직을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GamePhaseManager {

    private final GameRepository gameRepository;
    private final RoomService roomService;
    private final WordService wordService;
    private final SimpMessagingTemplate messagingTemplate;
    private final HintService hintService;
    private final ReturnToWaitingTrackerRepository trackerRepository;
    private final AsyncGameTimerScheduler timerScheduler;

    /**
     * 첫 번째 턴 시작
     */
    public void startFirstTurn(String roomId, GameState gameState) {
        RoomInfo roomInfo = roomService.getRoomInfo(roomId);
        if (roomInfo == null) {
            log.warn("RoomInfo not found - roomId: {}", roomId);
            return;
        }

        // 첫 번째 출제자 지정
        String drawerId = gameState.getTurnOrder().get(0);
        String drawerNickname = roomInfo.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(drawerId))
                .map(RoomPlayer::getNickname)
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
        timerScheduler.startWordSelectTimer(roomId, gameState.getGameSessionId(), 1);
    }

    /**
     * 그리기 단계 시작
     */
    public void startDrawingPhase(String roomId, GameState gameState) {
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
        timerScheduler.startDrawingTimer(roomId, gameSessionId, turnNumber, drawTime);

        // 힌트 타이머 시작 (그리기 시간이 충분히 긴 경우에만)
        if (drawTime >= GameConstants.Timing.FIRST_HINT_DELAY) {
            log.info("Starting first hint timer - room: {}, delay: {}s", roomId, GameConstants.Timing.FIRST_HINT_DELAY);
            timerScheduler.startHintTimer(roomId, gameSessionId, turnNumber, 1, GameConstants.Timing.FIRST_HINT_DELAY);
        } else {
            log.debug("Skipping first hint (drawTime: {}s < required: {}s)", drawTime, GameConstants.Timing.FIRST_HINT_DELAY);
        }
        if (drawTime >= GameConstants.Timing.SECOND_HINT_DELAY) {
            log.info("Starting second hint timer - room: {}, delay: {}s", roomId, GameConstants.Timing.SECOND_HINT_DELAY);
            timerScheduler.startHintTimer(roomId, gameSessionId, turnNumber, 2, GameConstants.Timing.SECOND_HINT_DELAY);
        } else {
            log.debug("Skipping second hint (drawTime: {}s < required: {}s)", drawTime, GameConstants.Timing.SECOND_HINT_DELAY);
        }
    }

    /**
     * 다음 턴 시작
     */
    public void startNextTurn(String roomId, GameState gameState) {
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
                .map(RoomPlayer::getNickname)
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
        timerScheduler.startWordSelectTimer(roomId, gameState.getGameSessionId(), nextTurnNumber);
    }

    /**
     * 게임 종료
     */
    public void endGame(String roomId, GameState gameState) {
        gameState.setPhase(GamePhase.GAME_END);
        gameState.setPhaseStartTime(System.currentTimeMillis());

        gameRepository.save(roomId, gameState);

        // 게임 종료 브로드캐스트
        messagingTemplate.convertAndSend(WebSocketTopics.gameState(roomId), gameState);

        log.info("Game ended - room: {}", roomId);

        // 대기방 복귀 추적 시작
        RoomInfo roomInfo = roomService.getRoomInfo(roomId);
        if (roomInfo != null) {
            String hostId = roomInfo.getPlayers().stream()
                    .filter(RoomPlayer::isHost)
                    .map(RoomPlayer::getPlayerId)
                    .findFirst()
                    .orElse(null);

            ReturnToWaitingTracker tracker = new ReturnToWaitingTracker(roomId, hostId);
            trackerRepository.save(roomId, tracker);
            log.info("Return tracker created - room: {}, host: {}", roomId, hostId);

            // 20초 후 자동 복귀 타이머 시작
            timerScheduler.scheduleAutoReturnToWaiting(roomId);
        }
    }

    /**
     * 자동 힌트 제공
     */
    public void provideAutoHint(String roomId, GameState gameState, int hintLevel, int turnNumber) {
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
    }

    // ========== 이벤트 리스너 ==========

    /**
     * 카운트다운 완료 이벤트 처리
     */
    @EventListener
    public void onCountdownCompleted(CountdownCompletedEvent event) {
        startFirstTurn(event.getRoomId(), event.getGameState());
    }

    /**
     * 단어 선택 시간 초과 이벤트 처리
     */
    @EventListener
    public void onWordSelectTimeout(WordSelectTimeoutEvent event) {
        startDrawingPhase(event.getRoomId(), event.getGameState());
    }

    /**
     * 힌트 제공 시간 도달 이벤트 처리
     */
    @EventListener
    public void onHintTime(HintTimeEvent event) {
        provideAutoHint(event.getRoomId(), event.getGameState(), event.getHintLevel(), event.getTurnNumber());
    }

    /**
     * 턴 결과 표시 종료 이벤트 처리
     */
    @EventListener
    public void onTurnResultEnd(TurnResultEndEvent event) {
        startNextTurn(event.getRoomId(), event.getGameState());
    }
}
