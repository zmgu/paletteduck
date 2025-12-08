package com.unduck.paletteduck.domain.game.service;

import com.unduck.paletteduck.domain.game.constants.GameConstants;
import com.unduck.paletteduck.domain.game.dto.GamePhase;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.dto.TurnEndReason;
import com.unduck.paletteduck.domain.game.dto.TurnInfo;
import com.unduck.paletteduck.domain.game.event.*;
import com.unduck.paletteduck.domain.game.repository.GameRepository;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.domain.room.dto.RoomPlayer;
import com.unduck.paletteduck.domain.room.model.ReturnToWaitingTracker;
import com.unduck.paletteduck.domain.room.repository.ReturnToWaitingTrackerRepository;
import com.unduck.paletteduck.domain.room.service.RoomService;
import com.unduck.paletteduck.domain.room.util.RoomPlayerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 비동기 게임 타이머 스케줄러
 * 모든 @Async 타이머 메서드를 관리하고 타이머 만료 시 이벤트 발행
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncGameTimerScheduler {

    private final GameRepository gameRepository;
    private final RoomService roomService;
    private final ReturnToWaitingTrackerRepository trackerRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 카운트다운 타이머 (게임 시작 전)
     */
    @Async
    public void startCountdown(String roomId) {
        try {
            TimeUnit.SECONDS.sleep(GameConstants.Timing.COUNTDOWN_TIME);

            GameState gameState = gameRepository.findById(roomId);
            if (gameState == null) {
                log.warn("GameState not found after countdown - roomId: {}", roomId);
                return;
            }

            eventPublisher.publishEvent(new CountdownCompletedEvent(roomId, gameState));
        } catch (InterruptedException e) {
            log.error("Countdown interrupted - roomId: {}", roomId, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 단어 선택 타이머
     */
    @Async
    public void startWordSelectTimer(String roomId, String gameSessionId, int turnNumber) {
        try {
            TimeUnit.SECONDS.sleep(GameConstants.Timing.WORD_SELECT_TIME);

            GameState gameState = gameRepository.findById(roomId);
            if (gameState == null || gameState.getCurrentTurn() == null) {
                return;
            }

            if (!isValidTimer(gameState, gameSessionId, turnNumber, GamePhase.WORD_SELECT, "Word select")) {
                return;
            }

            // 단어 선택 안 했으면 랜덤 선택
            if (gameState.getCurrentTurn().getWord() == null) {
                selectRandomWord(gameState, roomId);
            }

            // 이벤트 발행
            eventPublisher.publishEvent(new WordSelectTimeoutEvent(roomId, gameSessionId, turnNumber, gameState));

        } catch (InterruptedException e) {
            log.error("Word select timer interrupted - roomId: {}", roomId, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 그리기 타이머
     */
    @Async
    public void startDrawingTimer(String roomId, String gameSessionId, int turnNumber, int drawTime) {
        try {
            TimeUnit.SECONDS.sleep(drawTime);

            GameState gameState = gameRepository.findById(roomId);
            if (gameState == null || gameState.getCurrentTurn() == null) {
                return;
            }

            if (!isValidTimer(gameState, gameSessionId, turnNumber, GamePhase.DRAWING, "Drawing")) {
                return;
            }

            log.info("Drawing time ended - room: {}", roomId);
            eventPublisher.publishEvent(new DrawingTimeoutEvent(roomId, gameSessionId, turnNumber, gameState));

        } catch (InterruptedException e) {
            log.error("Drawing timer interrupted - roomId: {}", roomId, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 힌트 타이머
     */
    @Async
    public void startHintTimer(String roomId, String gameSessionId, int turnNumber, int hintLevel, int delaySeconds) {
        try {
            TimeUnit.SECONDS.sleep(delaySeconds);

            GameState gameState = gameRepository.findById(roomId);
            if (gameState == null || gameState.getCurrentTurn() == null) {
                return;
            }

            if (!isValidTimer(gameState, gameSessionId, turnNumber, GamePhase.DRAWING, "Hint")) {
                return;
            }

            // 이벤트 발행
            eventPublisher.publishEvent(new HintTimeEvent(roomId, gameSessionId, turnNumber, gameState, hintLevel));

        } catch (InterruptedException e) {
            log.error("Hint timer interrupted - roomId: {}, hintLevel: {}", roomId, hintLevel, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 턴 결과 표시 후 다음 턴 시작
     */
    @Async
    public void scheduleTurnResultEnd(String roomId) {
        try {
            TimeUnit.MILLISECONDS.sleep(GameConstants.Timing.ROUND_END_DELAY);

            GameState gameState = gameRepository.findById(roomId);
            if (gameState == null) {
                return;
            }

            eventPublisher.publishEvent(new TurnResultEndEvent(roomId, gameState));

        } catch (InterruptedException e) {
            log.error("Turn result display interrupted - roomId: {}", roomId, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 지연 후 턴 종료 (모든 플레이어 정답 시)
     */
    @Async
    public void endTurnWithDelay(String roomId, GameState gameState, TurnEndReason reason, int delayMillis) {
        try {
            TimeUnit.MILLISECONDS.sleep(delayMillis);
            eventPublisher.publishEvent(new TurnEndEvent(roomId, gameState, reason));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Delayed turn end interrupted for room: {}", roomId);
        }
    }

    /**
     * 자동 대기방 복귀 스케줄
     */
    @Async
    public void scheduleAutoReturnToWaiting(String roomId) {
        try {
            TimeUnit.SECONDS.sleep(GameConstants.Timing.AUTO_RETURN_TO_WAITING_TIME);

            ReturnToWaitingTracker tracker = trackerRepository.findById(roomId);
            if (tracker == null) {
                log.debug("Return tracker not found - room might have been manually returned: {}", roomId);
                return;
            }

            RoomInfo roomInfo = roomService.getRoomInfo(roomId);
            if (roomInfo == null) {
                log.debug("Room not found - room might have been deleted: {}", roomId);
                trackerRepository.delete(roomId);
                return;
            }

            handleAutoReturn(roomId, tracker, roomInfo);

        } catch (InterruptedException e) {
            log.error("Auto-return timer interrupted - roomId: {}", roomId, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 타이머 유효성 검증
     */
    private boolean isValidTimer(GameState gameState, String gameSessionId, int turnNumber,
                                  GamePhase expectedPhase, String timerType) {
        // 게임 세션 확인
        if (!gameSessionId.equals(gameState.getGameSessionId())) {
            log.debug("{} timer expired for old game session - current: {}, timer: {}",
                    timerType, gameState.getGameSessionId(), gameSessionId);
            return false;
        }

        // 턴 번호 확인
        if (gameState.getCurrentTurn().getTurnNumber() != turnNumber) {
            log.debug("{} timer expired for old turn - current: {}, timer: {}",
                    timerType, gameState.getCurrentTurn().getTurnNumber(), turnNumber);
            return false;
        }

        // 페이즈 확인
        if (gameState.getPhase() != expectedPhase) {
            log.debug("{} timer expired but phase already changed - current: {}",
                    timerType, gameState.getPhase());
            return false;
        }

        return true;
    }

    /**
     * 랜덤 단어 선택
     */
    private void selectRandomWord(GameState gameState, String roomId) {
        List<String> choices = gameState.getCurrentTurn().getWordChoices();
        Collections.shuffle(choices);
        String randomWord = choices.get(0);
        gameState.getCurrentTurn().setWord(randomWord);
        gameRepository.save(roomId, gameState);

        log.info("Word auto-selected - room: {}, word: {}", roomId, randomWord);
    }

    /**
     * 자동 복귀 처리
     */
    private void handleAutoReturn(String roomId, ReturnToWaitingTracker tracker, RoomInfo roomInfo) {
        if (!tracker.isAnyoneReturned()) {
            // 아무도 복귀 안함 -> 방 삭제
            log.info("No one returned manually - deleting room: {}", roomId);
            roomService.deleteRoom(roomId);
            trackerRepository.delete(roomId);
        } else {
            // 방장 위임 처리
            delegateHostOnAutoReturn(roomId, tracker, roomInfo);
            trackerRepository.delete(roomId);
            log.info("Auto-return timer completed - room maintained: {}", roomId);
        }
    }

    /**
     * 자동 복귀 시 방장 위임
     */
    private void delegateHostOnAutoReturn(String roomId, ReturnToWaitingTracker tracker, RoomInfo roomInfo) {
        if (tracker.isHasOriginalHostReturned() || tracker.getFirstReturnedPlayerId() == null) {
            return; // 원래 방장이 복귀했거나 복귀한 참가자가 없음
        }

        String originalHostId = tracker.getOriginalHostId();
        String newHostId = tracker.getFirstReturnedPlayerId();

        RoomPlayer originalHost = findPlayerById(roomInfo, originalHostId);
        RoomPlayer newHost = findPlayerById(roomInfo, newHostId);

        if (newHost != null) {
            if (originalHost != null) {
                originalHost.setHost(false);
                originalHost.setReady(false);
            }

            newHost.setHost(true);
            newHost.setReady(false);

            roomService.saveRoomInfo(roomId, roomInfo);

            log.info("Host authority delegated on auto-return - from: {} (original, absent), to: {} (first returner)",
                    originalHost != null ? originalHost.getNickname() : "unknown", newHost.getNickname());
        } else {
            log.warn("First returned player not found in room - playerId: {}", newHostId);
        }
    }

    /**
     * 플레이어 ID로 찾기
     */
    private RoomPlayer findPlayerById(RoomInfo roomInfo, String playerId) {
        return RoomPlayerUtil.findPlayerById(roomInfo, playerId).orElse(null);
    }
}
