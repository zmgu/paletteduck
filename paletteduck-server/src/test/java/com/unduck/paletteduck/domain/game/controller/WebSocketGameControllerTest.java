package com.unduck.paletteduck.domain.game.controller;

import com.unduck.paletteduck.domain.game.dto.*;
import com.unduck.paletteduck.domain.game.service.AsyncGameTimerScheduler;
import com.unduck.paletteduck.domain.game.service.GameScoringService;
import com.unduck.paletteduck.domain.game.service.GameService;
import com.unduck.paletteduck.domain.game.service.GameTimerService;
import com.unduck.paletteduck.domain.room.dto.PlayerRole;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.domain.room.dto.RoomPlayer;
import com.unduck.paletteduck.domain.room.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("WebSocketGameController 테스트")
@ExtendWith(MockitoExtension.class)
class WebSocketGameControllerTest {

    @Mock
    private GameService gameService;

    @Mock
    private GameTimerService gameTimerService;

    @Mock
    private AsyncGameTimerScheduler asyncGameTimerScheduler;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private RoomService roomService;

    @Mock
    private GameScoringService gameScoringService;

    @InjectMocks
    private WebSocketGameController controller;

    private GameState gameState;
    private RoomInfo roomInfo;

    @BeforeEach
    void setUp() {
        gameState = createGameState("room1");
        roomInfo = createRoomInfo("room1");
    }

    @Test
    @Tag("ws-game-word")
    @DisplayName("selectWord - 정상: 단어 선택이 처리되어야 한다")
    void selectWord_shouldProcessWordSelection() {
        // given
        String roomId = "room1";
        Map<String, String> payload = new HashMap<>();
        payload.put("playerId", "player1");
        payload.put("word", "사과");

        // when
        controller.selectWord(roomId, payload);

        // then
        verify(gameTimerService).selectWord(roomId, "player1", "사과");
    }

    @Test
    @Tag("ws-game-draw")
    @DisplayName("drawPath - 정상: 출제자의 그림 그리기가 브로드캐스트되어야 한다")
    void drawPath_validDrawer_shouldBroadcast() {
        // given
        String roomId = "room1";
        DrawData drawData = DrawData.builder()
                .playerId("player1")
                .build();

        when(gameService.getGameState(roomId)).thenReturn(gameState);

        // when
        controller.drawPath(roomId, drawData);

        // then
        verify(gameService).getGameState(roomId);
        verify(messagingTemplate).convertAndSend(anyString(), eq(drawData));
    }

    @Test
    @Tag("ws-game-draw")
    @DisplayName("drawPath - 게임 상태가 없으면 아무 작업도 하지 않아야 한다")
    void drawPath_noGameState_shouldDoNothing() {
        // given
        String roomId = "room1";
        DrawData drawData = DrawData.builder()
                .playerId("player1")
                .build();

        when(gameService.getGameState(roomId)).thenReturn(null);

        // when
        controller.drawPath(roomId, drawData);

        // then
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @Tag("ws-game-draw")
    @DisplayName("drawPath - DRAWING 페이즈가 아니면 아무 작업도 하지 않아야 한다")
    void drawPath_wrongPhase_shouldDoNothing() {
        // given
        String roomId = "room1";
        DrawData drawData = DrawData.builder()
                .playerId("player1")
                .build();

        gameState.setPhase(GamePhase.WORD_SELECT);
        when(gameService.getGameState(roomId)).thenReturn(gameState);

        // when
        controller.drawPath(roomId, drawData);

        // then
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @Tag("ws-game-draw")
    @DisplayName("drawPath - 출제자가 아니면 그림 그리기가 무시되어야 한다")
    void drawPath_notDrawer_shouldIgnore() {
        // given
        String roomId = "room1";
        DrawData drawData = DrawData.builder()
                .playerId("player2")
                .build();

        when(gameService.getGameState(roomId)).thenReturn(gameState);

        // when
        controller.drawPath(roomId, drawData);

        // then
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @Tag("ws-game-clear")
    @DisplayName("clearCanvas - 정상: 출제자의 캔버스 지우기가 브로드캐스트되어야 한다")
    void clearCanvas_validDrawer_shouldBroadcast() {
        // given
        String roomId = "room1";
        String playerId = "player1";

        when(gameService.getGameState(roomId)).thenReturn(gameState);

        // when
        controller.clearCanvas(roomId, playerId);

        // then
        verify(messagingTemplate).convertAndSend(anyString(), any(Map.class));
    }

    @Test
    @Tag("ws-game-clear")
    @DisplayName("clearCanvas - 출제자가 아니면 무시되어야 한다")
    void clearCanvas_notDrawer_shouldIgnore() {
        // given
        String roomId = "room1";
        String playerId = "player2"; // 출제자가 아님

        when(gameService.getGameState(roomId)).thenReturn(gameState);

        // when
        controller.clearCanvas(roomId, playerId);

        // then
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @Tag("ws-game-streaming")
    @DisplayName("streamDrawing - 정상: 그림 스트리밍이 브로드캐스트되어야 한다")
    void streamDrawing_validDrawer_shouldBroadcast() {
        // given
        String roomId = "room1";
        Map<String, Object> data = new HashMap<>();
        data.put("playerId", "player1");
        data.put("x", 100);
        data.put("y", 200);

        gameState.getCurrentTurn().setDrawingEvents(new ArrayList<>());
        when(gameService.getGameState(roomId)).thenReturn(gameState);

        // when
        controller.streamDrawing(roomId, data);

        // then
        verify(messagingTemplate).convertAndSend(anyString(), eq(data));
    }

    @Test
    @Tag("ws-game-streaming")
    @DisplayName("streamDrawing - 20개마다 Redis에 저장되어야 한다")
    void streamDrawing_shouldSaveEvery20Events() {
        // given
        String roomId = "room1";
        Map<String, Object> data = new HashMap<>();
        data.put("playerId", "player1");

        List<Map<String, Object>> events = new ArrayList<>();
        for (int i = 0; i < 19; i++) {
            events.add(new HashMap<>());
        }
        gameState.getCurrentTurn().setDrawingEvents(events);
        when(gameService.getGameState(roomId)).thenReturn(gameState);

        // when
        controller.streamDrawing(roomId, data);

        // then
        verify(gameService).updateGameState(roomId, gameState);
    }

    @Test
    @Tag("ws-game-hint")
    @DisplayName("provideChosungHint - 정상: 초성 힌트가 제공되어야 한다")
    void provideChosungHint_shouldProvideHint() {
        // given
        String roomId = "room1";
        String playerId = "player1";

        when(gameService.provideChosungHint(roomId, playerId)).thenReturn(true);
        when(gameService.getGameState(roomId)).thenReturn(gameState);

        // when
        controller.provideChosungHint(roomId, playerId);

        // then
        verify(gameService).provideChosungHint(roomId, playerId);
        verify(messagingTemplate).convertAndSend(anyString(), eq(gameState));
    }

    @Test
    @Tag("ws-game-hint")
    @DisplayName("provideChosungHint - 실패하면 브로드캐스트하지 않아야 한다")
    void provideChosungHint_failure_shouldNotBroadcast() {
        // given
        String roomId = "room1";
        String playerId = "player1";

        when(gameService.provideChosungHint(roomId, playerId)).thenReturn(false);

        // when
        controller.provideChosungHint(roomId, playerId);

        // then
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @Tag("ws-game-hint")
    @DisplayName("provideLetterHint - 정상: 글자 힌트가 제공되어야 한다")
    void provideLetterHint_shouldProvideHint() {
        // given
        String roomId = "room1";
        String playerId = "player1";

        when(gameService.provideLetterHint(roomId, playerId)).thenReturn(true);
        when(gameService.getGameState(roomId)).thenReturn(gameState);

        // when
        controller.provideLetterHint(roomId, playerId);

        // then
        verify(gameService).provideLetterHint(roomId, playerId);
        verify(messagingTemplate).convertAndSend(anyString(), eq(gameState));
    }

    @Test
    @Tag("ws-game-vote")
    @DisplayName("handleVote - 정상: 좋아요 투표가 처리되어야 한다")
    void handleVote_like_shouldProcessVote() {
        // given
        String roomId = "room1";
        Map<String, String> data = new HashMap<>();
        data.put("voterId", "player2");
        data.put("voteType", "LIKE");

        when(gameService.getGameState(roomId)).thenReturn(gameState);
        when(roomService.getRoomInfo(roomId)).thenReturn(roomInfo);

        // when
        controller.handleVote(roomId, data);

        // then
        verify(gameService).updateGameState(eq(roomId), any(GameState.class));
        verify(messagingTemplate).convertAndSend(anyString(), any(GameState.class));
    }

    @Test
    @Tag("ws-game-vote")
    @DisplayName("handleVote - 게임 상태가 없으면 아무 작업도 하지 않아야 한다")
    void handleVote_noGameState_shouldDoNothing() {
        // given
        String roomId = "room1";
        Map<String, String> data = new HashMap<>();
        data.put("voterId", "player2");
        data.put("voteType", "LIKE");

        when(gameService.getGameState(roomId)).thenReturn(null);

        // when
        controller.handleVote(roomId, data);

        // then
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @Tag("ws-game-vote")
    @DisplayName("handleVote - DRAWING 페이즈가 아니면 무시되어야 한다")
    void handleVote_wrongPhase_shouldIgnore() {
        // given
        String roomId = "room1";
        Map<String, String> data = new HashMap<>();
        data.put("voterId", "player2");
        data.put("voteType", "LIKE");

        gameState.setPhase(GamePhase.WORD_SELECT);
        when(gameService.getGameState(roomId)).thenReturn(gameState);
        when(roomService.getRoomInfo(roomId)).thenReturn(roomInfo);

        // when
        controller.handleVote(roomId, data);

        // then
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @Tag("ws-game-vote")
    @DisplayName("handleVote - 출제자는 자신의 그림에 투표할 수 없어야 한다")
    void handleVote_drawerCannotVote_shouldIgnore() {
        // given
        String roomId = "room1";
        Map<String, String> data = new HashMap<>();
        data.put("voterId", "player1"); // 출제자
        data.put("voteType", "LIKE");

        when(gameService.getGameState(roomId)).thenReturn(gameState);
        when(roomService.getRoomInfo(roomId)).thenReturn(roomInfo);

        // when
        controller.handleVote(roomId, data);

        // then
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @Tag("ws-game-vote")
    @DisplayName("handleVote - 관전자는 투표할 수 없어야 한다")
    void handleVote_spectatorCannotVote_shouldIgnore() {
        // given
        String roomId = "room1";
        Map<String, String> data = new HashMap<>();
        data.put("voterId", "spectator1");
        data.put("voteType", "LIKE");

        RoomPlayer spectator = RoomPlayer.builder()
                .playerId("spectator1")
                .nickname("관전자")
                .role(PlayerRole.SPECTATOR)
                .build();
        roomInfo.getPlayers().add(spectator);

        when(gameService.getGameState(roomId)).thenReturn(gameState);
        when(roomService.getRoomInfo(roomId)).thenReturn(roomInfo);

        // when
        controller.handleVote(roomId, data);

        // then
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    // Helper methods

    private GameState createGameState(String roomId) {
        List<String> turnOrder = Arrays.asList("player1", "player2", "player3");
        GameState state = new GameState(roomId, 3, 90, turnOrder);
        state.setPhase(GamePhase.DRAWING);

        TurnInfo turnInfo = new TurnInfo(1, "player1", "플레이어1");
        turnInfo.setWord("사과");
        state.setCurrentTurn(turnInfo);

        // 플레이어 초기화
        Player player1 = Player.builder()
                .playerId("player1")
                .nickname("플레이어1")
                .build();
        Player player2 = Player.builder()
                .playerId("player2")
                .nickname("플레이어2")
                .build();
        Player player3 = Player.builder()
                .playerId("player3")
                .nickname("플레이어3")
                .build();
        state.setPlayers(Arrays.asList(player1, player2, player3));

        return state;
    }

    private RoomInfo createRoomInfo(String roomId) {
        List<RoomPlayer> players = new ArrayList<>();
        players.add(RoomPlayer.builder()
                .playerId("player1")
                .nickname("플레이어1")
                .role(PlayerRole.PLAYER)
                .build());
        players.add(RoomPlayer.builder()
                .playerId("player2")
                .nickname("플레이어2")
                .role(PlayerRole.PLAYER)
                .build());
        players.add(RoomPlayer.builder()
                .playerId("player3")
                .nickname("플레이어3")
                .role(PlayerRole.PLAYER)
                .build());

        RoomInfo info = new RoomInfo();
        info.setRoomId(roomId);
        info.setPlayers(players);
        return info;
    }
}
