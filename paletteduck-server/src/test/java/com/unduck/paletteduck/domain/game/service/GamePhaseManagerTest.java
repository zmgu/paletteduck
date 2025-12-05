package com.unduck.paletteduck.domain.game.service;

import com.unduck.paletteduck.domain.game.dto.GamePhase;
import com.unduck.paletteduck.domain.game.dto.GameSettings;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.dto.TurnInfo;
import com.unduck.paletteduck.domain.game.repository.GameRepository;
import com.unduck.paletteduck.domain.room.dto.PlayerRole;
import com.unduck.paletteduck.domain.room.dto.ReturnToWaitingTracker;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.domain.room.dto.RoomPlayer;
import com.unduck.paletteduck.domain.room.repository.ReturnToWaitingTrackerRepository;
import com.unduck.paletteduck.domain.room.service.RoomService;
import com.unduck.paletteduck.domain.word.service.WordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("GamePhaseManager 테스트")
@ExtendWith(MockitoExtension.class)
class GamePhaseManagerTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private RoomService roomService;

    @Mock
    private WordService wordService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private HintService hintService;

    @Mock
    private ReturnToWaitingTrackerRepository trackerRepository;

    @Mock
    private AsyncGameTimerScheduler timerScheduler;

    @InjectMocks
    private GamePhaseManager gamePhaseManager;

    @Test
    @Tag("phase-turn-start")
    @DisplayName("startFirstTurn - 정상: 첫 턴을 시작하고 WORD_SELECT 페이즈로 전환해야 한다")
    void startFirstTurn_shouldStartFirstTurnAndSetWordSelectPhase() {
        // given
        String roomId = "room1";
        GameState gameState = createGameState();
        RoomInfo roomInfo = createRoomInfo();
        List<String> wordChoices = Arrays.asList("사과", "바나나", "포도");

        when(roomService.getRoomInfo(roomId)).thenReturn(roomInfo);
        when(wordService.getMixedWords(3)).thenReturn(wordChoices);

        // when
        gamePhaseManager.startFirstTurn(roomId, gameState);

        // then
        assertThat(gameState.getPhase()).isEqualTo(GamePhase.WORD_SELECT);
        assertThat(gameState.getCurrentTurn()).isNotNull();
        assertThat(gameState.getCurrentTurn().getTurnNumber()).isEqualTo(1);
        assertThat(gameState.getCurrentTurn().getDrawerId()).isEqualTo("player1");
        assertThat(gameState.getCurrentTurn().getWordChoices()).hasSize(3);
        verify(gameRepository).save(eq(roomId), eq(gameState));
        verify(messagingTemplate).convertAndSend(anyString(), eq(gameState));
        verify(timerScheduler).startWordSelectTimer(eq(roomId), anyString(), eq(1));
    }

    @Test
    @Tag("phase-turn-start")
    @DisplayName("startFirstTurn - RoomInfo가 없으면 아무 작업도 하지 않아야 한다")
    void startFirstTurn_noRoomInfo_shouldDoNothing() {
        // given
        String roomId = "room1";
        GameState gameState = createGameState();
        when(roomService.getRoomInfo(roomId)).thenReturn(null);

        // when
        gamePhaseManager.startFirstTurn(roomId, gameState);

        // then
        verify(gameRepository, never()).save(any(), any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(GameState.class));
    }

    @Test
    @Tag("phase-turn-start")
    @DisplayName("startDrawingPhase - 정상: DRAWING 페이즈로 전환하고 타이머를 시작해야 한다")
    void startDrawingPhase_shouldSetDrawingPhaseAndStartTimers() {
        // given
        String roomId = "room1";
        GameState gameState = createGameStateWithTurn();

        // when
        gamePhaseManager.startDrawingPhase(roomId, gameState);

        // then
        assertThat(gameState.getPhase()).isEqualTo(GamePhase.DRAWING);
        assertThat(gameState.getCurrentTurn().getHintLevel()).isEqualTo(0);
        assertThat(gameState.getCurrentTurn().getWordChoices()).isEmpty();
        assertThat(gameState.getCurrentTurn().getRevealedChosungPositions()).isEmpty();
        assertThat(gameState.getCurrentTurn().getRevealedLetterPositions()).isEmpty();
        verify(gameRepository).save(eq(roomId), eq(gameState));
        verify(messagingTemplate).convertAndSend(anyString(), eq(gameState));
        verify(timerScheduler).startDrawingTimer(eq(roomId), anyString(), anyInt(), eq(90));
        verify(timerScheduler).startHintTimer(eq(roomId), anyString(), anyInt(), eq(1), eq(20));
        verify(timerScheduler).startHintTimer(eq(roomId), anyString(), anyInt(), eq(2), eq(40));
    }

    @Test
    @Tag("phase-turn-start")
    @DisplayName("startNextTurn - 정상: 다음 턴을 시작해야 한다")
    void startNextTurn_shouldStartNextTurn() {
        // given
        String roomId = "room1";
        GameState gameState = createGameStateWithTurn();
        gameState.getCurrentTurn().setTurnNumber(1);
        RoomInfo roomInfo = createRoomInfo();
        List<String> wordChoices = Arrays.asList("딸기", "수박", "멜론");

        when(roomService.getRoomInfo(roomId)).thenReturn(roomInfo);
        when(wordService.getMixedWords(3)).thenReturn(wordChoices);

        // when
        gamePhaseManager.startNextTurn(roomId, gameState);

        // then
        assertThat(gameState.getPhase()).isEqualTo(GamePhase.WORD_SELECT);
        assertThat(gameState.getCurrentTurn().getTurnNumber()).isEqualTo(2);
        assertThat(gameState.getCurrentTurn().getDrawerId()).isEqualTo("player2");
        assertThat(gameState.getCurrentTurn().getWordChoices()).hasSize(3);
        verify(gameRepository).save(eq(roomId), eq(gameState));
        verify(messagingTemplate).convertAndSend(anyString(), eq(gameState));
    }

    @Test
    @Tag("phase-turn-start")
    @DisplayName("startNextTurn - 라운드가 변경되어야 한다")
    void startNextTurn_shouldUpdateRound() {
        // given
        String roomId = "room1";
        GameState gameState = createGameStateWithTurn();
        gameState.getCurrentTurn().setTurnNumber(3); // 3명 플레이어, 3번째 턴 완료
        gameState.setCurrentRound(1);
        RoomInfo roomInfo = createRoomInfo();
        List<String> wordChoices = Arrays.asList("딸기", "수박", "멜론");

        when(roomService.getRoomInfo(roomId)).thenReturn(roomInfo);
        when(wordService.getMixedWords(3)).thenReturn(wordChoices);

        // when
        gamePhaseManager.startNextTurn(roomId, gameState);

        // then
        assertThat(gameState.getCurrentRound()).isEqualTo(2); // 2라운드로 변경
        assertThat(gameState.getCurrentTurn().getTurnNumber()).isEqualTo(4);
        assertThat(gameState.getCurrentTurn().getDrawerId()).isEqualTo("player1"); // 다시 첫 번째 플레이어
    }

    @Test
    @Tag("phase-game-end")
    @DisplayName("startNextTurn - 마지막 턴이면 게임을 종료해야 한다")
    void startNextTurn_lastTurn_shouldEndGame() {
        // given
        String roomId = "room1";
        GameState gameState = createGameStateWithTurn();
        gameState.setTotalRounds(3); // 3라운드
        gameState.getCurrentTurn().setTurnNumber(9); // 3명 * 3라운드 = 9턴 완료
        RoomInfo roomInfo = createRoomInfo();

        when(roomService.getRoomInfo(roomId)).thenReturn(roomInfo);

        // when
        gamePhaseManager.startNextTurn(roomId, gameState);

        // then
        assertThat(gameState.getPhase()).isEqualTo(GamePhase.GAME_END);
        verify(trackerRepository).save(eq(roomId), any(ReturnToWaitingTracker.class));
        verify(timerScheduler).scheduleAutoReturnToWaiting(eq(roomId));
    }

    @Test
    @Tag("phase-game-end")
    @DisplayName("endGame - 정상: 게임을 종료하고 GAME_END 페이즈로 전환해야 한다")
    void endGame_shouldSetGameEndPhase() {
        // given
        String roomId = "room1";
        GameState gameState = createGameStateWithTurn();
        RoomInfo roomInfo = createRoomInfo();

        when(roomService.getRoomInfo(roomId)).thenReturn(roomInfo);

        // when
        gamePhaseManager.endGame(roomId, gameState);

        // then
        assertThat(gameState.getPhase()).isEqualTo(GamePhase.GAME_END);
        verify(gameRepository).save(eq(roomId), eq(gameState));
        verify(messagingTemplate).convertAndSend(anyString(), eq(gameState));
        verify(trackerRepository).save(eq(roomId), any(ReturnToWaitingTracker.class));
        verify(timerScheduler).scheduleAutoReturnToWaiting(eq(roomId));
    }

    @Test
    @Tag("phase-auto-hint")
    @DisplayName("provideAutoHint - 레벨 1: 글자수 힌트를 제공해야 한다")
    void provideAutoHint_level1_shouldProvideWordLengthHint() {
        // given
        String roomId = "room1";
        GameState gameState = createGameStateWithTurn();
        gameState.getCurrentTurn().setWord("사과");
        String[] hintArray = new String[]{"_", "_"};

        when(hintService.generateWordLengthHint("사과")).thenReturn(hintArray);

        // when
        gamePhaseManager.provideAutoHint(roomId, gameState, 1, 1);

        // then
        assertThat(gameState.getCurrentTurn().getHintLevel()).isEqualTo(1);
        assertThat(gameState.getCurrentTurn().getHintArray()).isEqualTo(hintArray);
        assertThat(gameState.getCurrentTurn().getCurrentHint()).isEqualTo("글자수 힌트");
        verify(gameRepository).save(eq(roomId), eq(gameState));
        verify(messagingTemplate).convertAndSend(anyString(), eq(gameState));
    }

    @Test
    @Tag("phase-auto-hint")
    @DisplayName("provideAutoHint - 레벨 2: 초성 힌트를 제공해야 한다")
    void provideAutoHint_level2_shouldProvideChosungHint() {
        // given
        String roomId = "room1";
        GameState gameState = createGameStateWithTurn();
        gameState.getCurrentTurn().setWord("사과");
        String[] hintArray = new String[]{"ㅅ", "_"};

        when(hintService.revealRandomChosung(eq("사과"), any())).thenReturn(0);
        when(hintService.generateHintArray(eq("사과"), any(), any())).thenReturn(hintArray);
        when(hintService.generateHintDisplay(eq("사과"), any(), any())).thenReturn("ㅅ _");

        // when
        gamePhaseManager.provideAutoHint(roomId, gameState, 2, 1);

        // then
        assertThat(gameState.getCurrentTurn().getHintLevel()).isEqualTo(2);
        assertThat(gameState.getCurrentTurn().getRevealedChosungPositions()).contains(0);
        assertThat(gameState.getCurrentTurn().getHintArray()).isEqualTo(hintArray);
        assertThat(gameState.getCurrentTurn().getCurrentHint()).isEqualTo("ㅅ _");
        verify(gameRepository).save(eq(roomId), eq(gameState));
        verify(messagingTemplate).convertAndSend(anyString(), eq(gameState));
    }

    @Test
    @Tag("phase-auto-hint")
    @DisplayName("provideAutoHint - 레벨 2에서 더 이상 공개할 초성이 없으면 아무 작업도 하지 않아야 한다")
    void provideAutoHint_level2_noMoreChosung_shouldDoNothing() {
        // given
        String roomId = "room1";
        GameState gameState = createGameStateWithTurn();
        gameState.getCurrentTurn().setWord("사과");

        when(hintService.revealRandomChosung(eq("사과"), any())).thenReturn(null);

        // when
        gamePhaseManager.provideAutoHint(roomId, gameState, 2, 1);

        // then
        assertThat(gameState.getCurrentTurn().getHintLevel()).isEqualTo(0); // 변경되지 않음
        verify(gameRepository, never()).save(any(), any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(GameState.class));
    }

    @Test
    @Tag("event-listener")
    @DisplayName("onCountdownCompleted - 카운트다운 완료 이벤트를 받으면 첫 턴을 시작해야 한다")
    void onCountdownCompleted_shouldStartFirstTurn() {
        // given
        String roomId = "room1";
        GameState gameState = createGameState();
        RoomInfo roomInfo = createRoomInfo();
        List<String> wordChoices = Arrays.asList("사과", "바나나", "포도");

        when(roomService.getRoomInfo(roomId)).thenReturn(roomInfo);
        when(wordService.getMixedWords(3)).thenReturn(wordChoices);

        com.unduck.paletteduck.domain.game.event.CountdownCompletedEvent event =
                new com.unduck.paletteduck.domain.game.event.CountdownCompletedEvent(roomId, gameState);

        // when
        gamePhaseManager.onCountdownCompleted(event);

        // then
        assertThat(gameState.getPhase()).isEqualTo(com.unduck.paletteduck.domain.game.dto.GamePhase.WORD_SELECT);
        assertThat(gameState.getCurrentTurn()).isNotNull();
        verify(gameRepository).save(eq(roomId), eq(gameState));
        verify(messagingTemplate).convertAndSend(anyString(), eq(gameState));
    }

    @Test
    @Tag("event-listener")
    @DisplayName("onWordSelectTimeout - 단어 선택 타임아웃 이벤트를 받으면 그리기 페이즈를 시작해야 한다")
    void onWordSelectTimeout_shouldStartDrawingPhase() {
        // given
        String roomId = "room1";
        GameState gameState = createGameStateWithTurn();
        String gameSessionId = "session1";
        int turnNumber = 1;

        com.unduck.paletteduck.domain.game.event.WordSelectTimeoutEvent event =
                new com.unduck.paletteduck.domain.game.event.WordSelectTimeoutEvent(roomId, gameSessionId, turnNumber, gameState);

        // when
        gamePhaseManager.onWordSelectTimeout(event);

        // then
        assertThat(gameState.getPhase()).isEqualTo(com.unduck.paletteduck.domain.game.dto.GamePhase.DRAWING);
        verify(gameRepository).save(eq(roomId), eq(gameState));
        verify(messagingTemplate).convertAndSend(anyString(), eq(gameState));
        verify(timerScheduler).startDrawingTimer(eq(roomId), anyString(), anyInt(), eq(90));
    }

    @Test
    @Tag("event-listener")
    @DisplayName("onHintTime - 힌트 시간 이벤트를 받으면 자동 힌트를 제공해야 한다")
    void onHintTime_shouldProvideAutoHint() {
        // given
        String roomId = "room1";
        GameState gameState = createGameStateWithTurn();
        gameState.getCurrentTurn().setWord("사과");
        String gameSessionId = "session1";
        int hintLevel = 1;
        int turnNumber = 1;

        String[] hintArray = new String[]{"_", "_"};
        when(hintService.generateWordLengthHint("사과")).thenReturn(hintArray);

        com.unduck.paletteduck.domain.game.event.HintTimeEvent event =
                new com.unduck.paletteduck.domain.game.event.HintTimeEvent(roomId, gameSessionId, turnNumber, gameState, hintLevel);

        // when
        gamePhaseManager.onHintTime(event);

        // then
        assertThat(gameState.getCurrentTurn().getHintLevel()).isEqualTo(1);
        verify(gameRepository).save(eq(roomId), eq(gameState));
        verify(messagingTemplate).convertAndSend(anyString(), eq(gameState));
    }

    @Test
    @Tag("event-listener")
    @DisplayName("onTurnResultEnd - 턴 결과 종료 이벤트를 받으면 다음 턴을 시작해야 한다")
    void onTurnResultEnd_shouldStartNextTurn() {
        // given
        String roomId = "room1";
        GameState gameState = createGameStateWithTurn();
        gameState.getCurrentTurn().setTurnNumber(1);
        RoomInfo roomInfo = createRoomInfo();
        List<String> wordChoices = Arrays.asList("딸기", "수박", "멜론");

        when(roomService.getRoomInfo(roomId)).thenReturn(roomInfo);
        when(wordService.getMixedWords(3)).thenReturn(wordChoices);

        com.unduck.paletteduck.domain.game.event.TurnResultEndEvent event =
                new com.unduck.paletteduck.domain.game.event.TurnResultEndEvent(roomId, gameState);

        // when
        gamePhaseManager.onTurnResultEnd(event);

        // then
        assertThat(gameState.getPhase()).isEqualTo(com.unduck.paletteduck.domain.game.dto.GamePhase.WORD_SELECT);
        assertThat(gameState.getCurrentTurn().getTurnNumber()).isEqualTo(2);
        verify(gameRepository).save(eq(roomId), eq(gameState));
        verify(messagingTemplate).convertAndSend(anyString(), eq(gameState));
    }

    // Helper methods

    private GameState createGameState() {
        List<String> turnOrder = Arrays.asList("player1", "player2", "player3");
        return new GameState("room1", 3, 90, turnOrder);
    }

    private GameState createGameStateWithTurn() {
        GameState gameState = createGameState();
        TurnInfo turnInfo = new TurnInfo(1, "player1", "플레이어1");
        turnInfo.setWord("사과");
        turnInfo.setWordChoices(Arrays.asList("사과", "바나나", "포도"));
        gameState.setCurrentTurn(turnInfo);
        gameState.setPhase(GamePhase.WORD_SELECT);
        return gameState;
    }

    private RoomInfo createRoomInfo() {
        List<RoomPlayer> players = new ArrayList<>();
        players.add(RoomPlayer.builder()
                .playerId("player1")
                .nickname("플레이어1")
                .isHost(true)
                .role(PlayerRole.PLAYER)
                .build());
        players.add(RoomPlayer.builder()
                .playerId("player2")
                .nickname("플레이어2")
                .isHost(false)
                .role(PlayerRole.PLAYER)
                .build());
        players.add(RoomPlayer.builder()
                .playerId("player3")
                .nickname("플레이어3")
                .isHost(false)
                .role(PlayerRole.PLAYER)
                .build());

        GameSettings settings = new GameSettings();
        settings.setRounds(3);
        settings.setDrawTime(90);
        settings.setWordChoices(3);

        RoomInfo roomInfo = new RoomInfo();
        roomInfo.setRoomId("room1");
        roomInfo.setPlayers(players);
        roomInfo.setSettings(settings);

        return roomInfo;
    }
}
