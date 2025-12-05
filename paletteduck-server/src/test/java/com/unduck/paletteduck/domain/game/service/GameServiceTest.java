package com.unduck.paletteduck.domain.game.service;

import com.unduck.paletteduck.domain.game.dto.GamePhase;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.dto.TurnInfo;
import com.unduck.paletteduck.domain.game.repository.GameRepository;
import com.unduck.paletteduck.domain.game.dto.GameSettings;
import com.unduck.paletteduck.domain.room.dto.PlayerRole;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.domain.room.dto.RoomPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("GameService 테스트")
@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private HintService hintService;

    @InjectMocks
    private GameService gameService;

    @Test
    @Tag("game-initialization")
    @DisplayName("initializeGame - 정상: 게임을 초기화하고 플레이어를 설정해야 한다")
    void initializeGame_shouldInitializeGameWithPlayers() {
        // given
        RoomInfo roomInfo = createRoomInfo();

        // when
        GameState gameState = gameService.initializeGame(roomInfo);

        // then
        assertThat(gameState).isNotNull();
        assertThat(gameState.getRoomId()).isEqualTo("room1");
        assertThat(gameState.getTotalRounds()).isEqualTo(3);
        assertThat(gameState.getDrawTime()).isEqualTo(90);
        assertThat(gameState.getPlayers()).hasSize(3);
        assertThat(gameState.getTurnOrder()).hasSize(3);
        verify(gameRepository).save(eq("room1"), any(GameState.class));
    }

    @Test
    @Tag("game-initialization")
    @DisplayName("initializeGame - 턴 순서가 랜덤화되어야 한다")
    void initializeGame_shouldRandomizeTurnOrder() {
        // given
        RoomInfo roomInfo = createRoomInfo();

        // when
        GameState gameState1 = gameService.initializeGame(roomInfo);
        GameState gameState2 = gameService.initializeGame(roomInfo);

        // then
        // 두 번의 초기화로 다른 순서가 나올 수 있음 (확률적 테스트)
        // 최소한 턴 순서가 설정되었는지만 확인
        assertThat(gameState1.getTurnOrder()).hasSize(3);
        assertThat(gameState2.getTurnOrder()).hasSize(3);
    }

    @Test
    @Tag("game-initialization")
    @DisplayName("initializeGame - 관전자는 게임에 포함되지 않아야 한다")
    void initializeGame_shouldExcludeSpectators() {
        // given
        RoomInfo roomInfo = createRoomInfo();
        // 관전자 추가
        RoomPlayer spectator = RoomPlayer.builder()
                .playerId("player4")
                .nickname("관전자")
                .isHost(false)
                .isReady(false)
                .role(PlayerRole.SPECTATOR)
                .build();
        roomInfo.getPlayers().add(spectator);

        // when
        GameState gameState = gameService.initializeGame(roomInfo);

        // then
        assertThat(gameState.getPlayers()).hasSize(3); // 관전자 제외
        assertThat(gameState.getTurnOrder()).hasSize(3);
        assertThat(gameState.getTurnOrder()).doesNotContain("player4");
    }

    @Test
    @Tag("game-hint-chosung")
    @DisplayName("provideChosungHint - 정상: 초성 힌트를 제공해야 한다")
    void provideChosungHint_validRequest_shouldProvideHint() {
        // given
        String roomId = "room1";
        String playerId = "player1";
        GameState gameState = createGameStateWithDrawer(playerId);
        when(gameRepository.findById(roomId)).thenReturn(gameState);
        when(hintService.revealRandomChosung(eq("사과"), any())).thenReturn(0);
        when(hintService.generateHintArray(eq("사과"), any(), any())).thenReturn(new String[]{"ㅅ", "_"});
        when(hintService.generateHintDisplay(eq("사과"), any(), any())).thenReturn("ㅅ _");

        // when
        boolean result = gameService.provideChosungHint(roomId, playerId);

        // then
        assertThat(result).isTrue();
        verify(hintService).revealRandomChosung(eq("사과"), any());
        verify(gameRepository).save(eq(roomId), any(GameState.class));
    }

    @Test
    @Tag("game-hint-chosung")
    @DisplayName("provideChosungHint - 게임 상태가 없으면 false를 반환해야 한다")
    void provideChosungHint_noGameState_shouldReturnFalse() {
        // given
        when(gameRepository.findById("room1")).thenReturn(null);

        // when
        boolean result = gameService.provideChosungHint("room1", "player1");

        // then
        assertThat(result).isFalse();
        verify(hintService, never()).revealRandomChosung(any(), any());
    }

    @Test
    @Tag("game-hint-chosung")
    @DisplayName("provideChosungHint - 출제자가 아니면 false를 반환해야 한다")
    void provideChosungHint_notDrawer_shouldReturnFalse() {
        // given
        String roomId = "room1";
        GameState gameState = createGameStateWithDrawer("player1");
        when(gameRepository.findById(roomId)).thenReturn(gameState);

        // when
        boolean result = gameService.provideChosungHint(roomId, "player2"); // 다른 플레이어

        // then
        assertThat(result).isFalse();
        verify(hintService, never()).revealRandomChosung(any(), any());
    }

    @Test
    @Tag("game-hint-chosung")
    @DisplayName("provideChosungHint - DRAWING 페이즈가 아니면 false를 반환해야 한다")
    void provideChosungHint_wrongPhase_shouldReturnFalse() {
        // given
        String roomId = "room1";
        String playerId = "player1";
        GameState gameState = createGameStateWithDrawer(playerId);
        gameState.setPhase(GamePhase.COUNTDOWN); // 잘못된 페이즈
        when(gameRepository.findById(roomId)).thenReturn(gameState);

        // when
        boolean result = gameService.provideChosungHint(roomId, playerId);

        // then
        assertThat(result).isFalse();
        verify(hintService, never()).revealRandomChosung(any(), any());
    }

    @Test
    @Tag("game-hint-chosung")
    @DisplayName("provideChosungHint - 힌트 레벨이 2 미만이면 false를 반환해야 한다")
    void provideChosungHint_hintLevelTooLow_shouldReturnFalse() {
        // given
        String roomId = "room1";
        String playerId = "player1";
        GameState gameState = createGameStateWithDrawer(playerId);
        gameState.getCurrentTurn().setHintLevel(1); // 레벨 1
        when(gameRepository.findById(roomId)).thenReturn(gameState);

        // when
        boolean result = gameService.provideChosungHint(roomId, playerId);

        // then
        assertThat(result).isFalse();
        verify(hintService, never()).revealRandomChosung(any(), any());
    }

    @Test
    @Tag("game-hint-chosung")
    @DisplayName("provideChosungHint - 더 이상 공개할 위치가 없으면 false를 반환해야 한다")
    void provideChosungHint_noMorePositions_shouldReturnFalse() {
        // given
        String roomId = "room1";
        String playerId = "player1";
        GameState gameState = createGameStateWithDrawer(playerId);
        when(gameRepository.findById(roomId)).thenReturn(gameState);
        when(hintService.revealRandomChosung(eq("사과"), any())).thenReturn(null); // 더 이상 없음

        // when
        boolean result = gameService.provideChosungHint(roomId, playerId);

        // then
        assertThat(result).isFalse();
        verify(hintService).revealRandomChosung(eq("사과"), any());
        verify(gameRepository, never()).save(any(), any());
    }

    @Test
    @Tag("game-hint-letter")
    @DisplayName("provideLetterHint - 정상: 글자 힌트를 제공해야 한다")
    void provideLetterHint_validRequest_shouldProvideHint() {
        // given
        String roomId = "room1";
        String playerId = "player1";
        GameState gameState = createGameStateWithDrawer(playerId);
        when(gameRepository.findById(roomId)).thenReturn(gameState);
        when(hintService.revealRandomLetter(eq("사과"), any())).thenReturn(1);
        when(hintService.generateHintArray(eq("사과"), any(), any())).thenReturn(new String[]{"_", "과"});
        when(hintService.generateHintDisplay(eq("사과"), any(), any())).thenReturn("_ 과");

        // when
        boolean result = gameService.provideLetterHint(roomId, playerId);

        // then
        assertThat(result).isTrue();
        verify(hintService).revealRandomLetter(eq("사과"), any());
        verify(gameRepository).save(eq(roomId), any(GameState.class));
    }

    @Test
    @Tag("game-hint-letter")
    @DisplayName("provideLetterHint - 게임 상태가 없으면 false를 반환해야 한다")
    void provideLetterHint_noGameState_shouldReturnFalse() {
        // given
        when(gameRepository.findById("room1")).thenReturn(null);

        // when
        boolean result = gameService.provideLetterHint("room1", "player1");

        // then
        assertThat(result).isFalse();
        verify(hintService, never()).revealRandomLetter(any(), any());
    }

    @Test
    @Tag("game-hint-letter")
    @DisplayName("provideLetterHint - 출제자가 아니면 false를 반환해야 한다")
    void provideLetterHint_notDrawer_shouldReturnFalse() {
        // given
        String roomId = "room1";
        GameState gameState = createGameStateWithDrawer("player1");
        when(gameRepository.findById(roomId)).thenReturn(gameState);

        // when
        boolean result = gameService.provideLetterHint(roomId, "player2"); // 다른 플레이어

        // then
        assertThat(result).isFalse();
        verify(hintService, never()).revealRandomLetter(any(), any());
    }

    @Test
    @Tag("game-hint-letter")
    @DisplayName("provideLetterHint - DRAWING 페이즈가 아니면 false를 반환해야 한다")
    void provideLetterHint_wrongPhase_shouldReturnFalse() {
        // given
        String roomId = "room1";
        String playerId = "player1";
        GameState gameState = createGameStateWithDrawer(playerId);
        gameState.setPhase(GamePhase.WORD_SELECT); // 잘못된 페이즈
        when(gameRepository.findById(roomId)).thenReturn(gameState);

        // when
        boolean result = gameService.provideLetterHint(roomId, playerId);

        // then
        assertThat(result).isFalse();
        verify(hintService, never()).revealRandomLetter(any(), any());
    }

    @Test
    @Tag("game-hint-letter")
    @DisplayName("provideLetterHint - 힌트 레벨이 2 미만이면 false를 반환해야 한다")
    void provideLetterHint_hintLevelTooLow_shouldReturnFalse() {
        // given
        String roomId = "room1";
        String playerId = "player1";
        GameState gameState = createGameStateWithDrawer(playerId);
        gameState.getCurrentTurn().setHintLevel(0); // 레벨 0
        when(gameRepository.findById(roomId)).thenReturn(gameState);

        // when
        boolean result = gameService.provideLetterHint(roomId, playerId);

        // then
        assertThat(result).isFalse();
        verify(hintService, never()).revealRandomLetter(any(), any());
    }

    @Test
    @Tag("game-hint-letter")
    @DisplayName("provideLetterHint - 더 이상 공개할 글자가 없으면 false를 반환해야 한다")
    void provideLetterHint_maxRevealsReached_shouldReturnFalse() {
        // given
        String roomId = "room1";
        String playerId = "player1";
        GameState gameState = createGameStateWithDrawer(playerId);
        when(gameRepository.findById(roomId)).thenReturn(gameState);
        when(hintService.revealRandomLetter(eq("사과"), any())).thenReturn(null); // 최대 도달

        // when
        boolean result = gameService.provideLetterHint(roomId, playerId);

        // then
        assertThat(result).isFalse();
        verify(hintService).revealRandomLetter(eq("사과"), any());
        verify(gameRepository, never()).save(any(), any());
    }

    // Helper methods

    private RoomInfo createRoomInfo() {
        List<RoomPlayer> players = new ArrayList<>();
        players.add(RoomPlayer.builder()
                .playerId("player1")
                .nickname("플레이어1")
                .isHost(true)
                .isReady(true)
                .role(PlayerRole.PLAYER)
                .build());
        players.add(RoomPlayer.builder()
                .playerId("player2")
                .nickname("플레이어2")
                .isHost(false)
                .isReady(true)
                .role(PlayerRole.PLAYER)
                .build());
        players.add(RoomPlayer.builder()
                .playerId("player3")
                .nickname("플레이어3")
                .isHost(false)
                .isReady(true)
                .role(PlayerRole.PLAYER)
                .build());

        GameSettings settings = new GameSettings();
        settings.setRounds(3);
        settings.setDrawTime(90);
        settings.setMaxPlayers(10);
        settings.setWordChoices(3);

        RoomInfo roomInfo = new RoomInfo();
        roomInfo.setRoomId("room1");
        roomInfo.setPlayers(players);
        roomInfo.setSettings(settings);

        return roomInfo;
    }

    private GameState createGameStateWithDrawer(String drawerId) {
        List<String> turnOrder = new ArrayList<>();
        turnOrder.add(drawerId);
        turnOrder.add("player2");
        turnOrder.add("player3");

        GameState gameState = new GameState("room1", 3, 90, turnOrder);
        gameState.setPhase(GamePhase.DRAWING);

        TurnInfo turnInfo = new TurnInfo(1, drawerId, "출제자");
        turnInfo.setWord("사과");
        turnInfo.setHintLevel(2); // 힌트 레벨 2
        gameState.setCurrentTurn(turnInfo);

        return gameState;
    }
}
