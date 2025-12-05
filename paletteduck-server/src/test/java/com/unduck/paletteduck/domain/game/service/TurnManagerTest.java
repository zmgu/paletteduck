package com.unduck.paletteduck.domain.game.service;

import com.unduck.paletteduck.domain.game.dto.GamePhase;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.dto.TurnEndReason;
import com.unduck.paletteduck.domain.game.dto.TurnInfo;
import com.unduck.paletteduck.domain.game.repository.GameRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("TurnManager 테스트")
@ExtendWith(MockitoExtension.class)
class TurnManagerTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private AsyncGameTimerScheduler timerScheduler;

    @InjectMocks
    private TurnManager turnManager;

    @Test
    @Tag("turn-end")
    @DisplayName("endTurn - TIME_OUT: 턴을 종료하고 TURN_RESULT 페이즈로 전환해야 한다")
    void endTurn_timeout_shouldEndTurnAndSetTurnResultPhase() {
        // given
        String roomId = "room1";
        GameState gameState = createGameState();
        TurnEndReason reason = TurnEndReason.TIME_OUT;

        // when
        turnManager.endTurn(roomId, gameState, reason);

        // then
        assertThat(gameState.getPhase()).isEqualTo(GamePhase.TURN_RESULT);
        assertThat(gameState.getCurrentTurn().getTurnEndReason()).isEqualTo(TurnEndReason.TIME_OUT);
        verify(gameRepository).save(eq(roomId), eq(gameState));
        verify(messagingTemplate).convertAndSend(anyString(), eq(gameState));
        verify(timerScheduler).scheduleTurnResultEnd(eq(roomId));
    }

    @Test
    @Tag("turn-end")
    @DisplayName("endTurn - ALL_CORRECT: 모든 플레이어가 정답을 맞춘 경우")
    void endTurn_allCorrect_shouldEndTurnWithAllCorrectReason() {
        // given
        String roomId = "room1";
        GameState gameState = createGameState();
        TurnEndReason reason = TurnEndReason.ALL_CORRECT;

        // when
        turnManager.endTurn(roomId, gameState, reason);

        // then
        assertThat(gameState.getPhase()).isEqualTo(GamePhase.TURN_RESULT);
        assertThat(gameState.getCurrentTurn().getTurnEndReason()).isEqualTo(TurnEndReason.ALL_CORRECT);
        verify(gameRepository).save(eq(roomId), eq(gameState));
        verify(messagingTemplate).convertAndSend(anyString(), eq(gameState));
        verify(timerScheduler).scheduleTurnResultEnd(eq(roomId));
    }

    @Test
    @Tag("turn-end")
    @DisplayName("endTurn - DRAWER_LEFT: 출제자가 퇴장한 경우")
    void endTurn_drawerLeft_shouldEndTurnWithDrawerLeftReason() {
        // given
        String roomId = "room1";
        GameState gameState = createGameState();
        TurnEndReason reason = TurnEndReason.DRAWER_LEFT;

        // when
        turnManager.endTurn(roomId, gameState, reason);

        // then
        assertThat(gameState.getPhase()).isEqualTo(GamePhase.TURN_RESULT);
        assertThat(gameState.getCurrentTurn().getTurnEndReason()).isEqualTo(TurnEndReason.DRAWER_LEFT);
        verify(gameRepository).save(eq(roomId), eq(gameState));
    }

    @Test
    @Tag("turn-end")
    @DisplayName("endTurn - phaseStartTime이 설정되어야 한다")
    void endTurn_shouldSetPhaseStartTime() {
        // given
        String roomId = "room1";
        GameState gameState = createGameState();
        long timeBefore = System.currentTimeMillis();

        // when
        turnManager.endTurn(roomId, gameState, TurnEndReason.TIME_OUT);

        // then
        assertThat(gameState.getPhaseStartTime()).isGreaterThanOrEqualTo(timeBefore);
    }

    // Helper methods

    private GameState createGameState() {
        GameState gameState = new GameState("room1", 3, 90, Arrays.asList("player1", "player2", "player3"));
        TurnInfo turnInfo = new TurnInfo(1, "player1", "플레이어1");
        turnInfo.setWord("사과");
        gameState.setCurrentTurn(turnInfo);
        gameState.setPhase(GamePhase.DRAWING);
        return gameState;
    }
}
