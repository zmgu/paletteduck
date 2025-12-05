package com.unduck.paletteduck.domain.room.controller;

import com.unduck.paletteduck.domain.chat.dto.ChatMessage;
import com.unduck.paletteduck.domain.chat.dto.RoleChangeMessage;
import com.unduck.paletteduck.domain.game.dto.GamePhase;
import com.unduck.paletteduck.domain.game.dto.GameSettings;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.service.GameService;
import com.unduck.paletteduck.domain.room.dto.*;
import com.unduck.paletteduck.domain.room.service.RoomGameService;
import com.unduck.paletteduck.domain.room.service.RoomPlayerService;
import com.unduck.paletteduck.domain.room.service.RoomService;
import com.unduck.paletteduck.domain.room.service.SessionMappingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("WebSocketRoomController 테스트")
@ExtendWith(MockitoExtension.class)
class WebSocketRoomControllerTest {

    @Mock
    private RoomService roomService;

    @Mock
    private RoomPlayerService roomPlayerService;

    @Mock
    private RoomGameService roomGameService;

    @Mock
    private SessionMappingService sessionMappingService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private GameService gameService;

    @Mock
    private SimpMessageHeaderAccessor headerAccessor;

    @InjectMocks
    private WebSocketRoomController controller;

    private RoomInfo roomInfo;
    private GameState gameState;

    @BeforeEach
    void setUp() {
        roomInfo = createRoomInfo("room1");
        gameState = createGameState("room1");
    }

    @Test
    @Tag("ws-room-session")
    @DisplayName("registerSession - 정상: 세션이 등록되어야 한다")
    void registerSession_shouldRegisterSession() {
        // given
        String roomId = "room1";
        String playerId = "player1";
        String sessionId = "session123";

        when(headerAccessor.getSessionId()).thenReturn(sessionId);
        when(roomService.getRoomInfo(roomId)).thenReturn(roomInfo);

        // when
        controller.registerSession(roomId, playerId, headerAccessor);

        // then
        verify(sessionMappingService).addSession(sessionId, playerId, roomId);
    }

    @Test
    @Tag("ws-room-session")
    @DisplayName("registerSession - 게임 진행 중이면 GameState를 브로드캐스트해야 한다")
    void registerSession_gamePlaying_shouldBroadcastGameState() {
        // given
        String roomId = "room1";
        String playerId = "player1";
        String sessionId = "session123";

        roomInfo.setStatus(RoomStatus.PLAYING);
        when(headerAccessor.getSessionId()).thenReturn(sessionId);
        when(roomService.getRoomInfo(roomId)).thenReturn(roomInfo);
        when(gameService.getGameState(roomId)).thenReturn(gameState);

        // when
        controller.registerSession(roomId, playerId, headerAccessor);

        // then
        verify(gameService).getGameState(roomId);
        verify(messagingTemplate).convertAndSend(anyString(), eq(gameState));
    }

    @Test
    @Tag("ws-room-session")
    @DisplayName("registerSession - 대기 중이면 GameState를 브로드캐스트하지 않아야 한다")
    void registerSession_roomWaiting_shouldNotBroadcastGameState() {
        // given
        String roomId = "room1";
        String playerId = "player1";
        String sessionId = "session123";

        roomInfo.setStatus(RoomStatus.WAITING);
        when(headerAccessor.getSessionId()).thenReturn(sessionId);
        when(roomService.getRoomInfo(roomId)).thenReturn(roomInfo);

        // when
        controller.registerSession(roomId, playerId, headerAccessor);

        // then
        verify(gameService, never()).getGameState(anyString());
    }

    @Test
    @Tag("ws-room-info")
    @DisplayName("updateRoomInfo - 정상: 방 정보가 브로드캐스트되어야 한다")
    void updateRoomInfo_shouldBroadcastRoomInfo() {
        // given
        String roomId = "room1";
        when(roomService.getRoomInfo(roomId)).thenReturn(roomInfo);

        // when
        controller.updateRoomInfo(roomId);

        // then
        verify(roomService).getRoomInfo(roomId);
        verify(messagingTemplate).convertAndSend(anyString(), eq(roomInfo));
    }

    @Test
    @Tag("ws-room-info")
    @DisplayName("updateRoomInfo - 방이 없으면 브로드캐스트하지 않아야 한다")
    void updateRoomInfo_noRoom_shouldNotBroadcast() {
        // given
        String roomId = "room1";
        when(roomService.getRoomInfo(roomId)).thenReturn(null);

        // when
        controller.updateRoomInfo(roomId);

        // then
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @Tag("ws-room-ready")
    @DisplayName("toggleReady - 정상: 준비 상태가 토글되고 브로드캐스트되어야 한다")
    void toggleReady_shouldToggleAndBroadcast() {
        // given
        String roomId = "room1";
        String playerId = "player1";

        when(roomService.getRoomInfo(roomId)).thenReturn(roomInfo);

        // when
        controller.toggleReady(roomId, playerId);

        // then
        verify(roomPlayerService).toggleReady(roomId, playerId);
        verify(messagingTemplate).convertAndSend(anyString(), eq(roomInfo));
    }

    @Test
    @Tag("ws-room-role")
    @DisplayName("changeRole - 정상: 역할이 변경되고 브로드캐스트되어야 한다")
    void changeRole_shouldChangeRoleAndBroadcast() {
        // given
        String roomId = "room1";
        RoleChangeMessage message = new RoleChangeMessage();
        message.setPlayerId("player1");
        message.setNewRole(PlayerRole.SPECTATOR);

        when(roomService.getRoomInfo(roomId)).thenReturn(roomInfo);

        // when
        controller.changeRole(roomId, message);

        // then
        verify(roomPlayerService).changeRole(roomId, "player1", PlayerRole.SPECTATOR);
        verify(messagingTemplate).convertAndSend(anyString(), eq(roomInfo));
    }

    @Test
    @Tag("ws-room-settings")
    @DisplayName("updateSettings - 정상: 설정이 업데이트되고 브로드캐스트되어야 한다")
    void updateSettings_shouldUpdateAndBroadcast() {
        // given
        String roomId = "room1";
        GameSettings settings = new GameSettings();
        settings.setRounds(5);
        settings.setDrawTime(120);

        SettingsUpdateMessage message = new SettingsUpdateMessage();
        message.setPlayerId("player1");
        message.setSettings(settings);

        when(roomService.getRoomInfo(roomId)).thenReturn(roomInfo);

        // when
        controller.updateSettings(roomId, message);

        // then
        verify(roomGameService).updateSettings(roomId, "player1", settings);
        verify(messagingTemplate).convertAndSend(anyString(), eq(roomInfo));
    }

    @Test
    @Tag("ws-room-game")
    @DisplayName("startGame - 정상: 게임이 시작되고 브로드캐스트되어야 한다")
    void startGame_shouldStartGameAndBroadcast() {
        // given
        String roomId = "room1";
        String playerId = "player1";

        when(roomGameService.startGame(roomId, playerId)).thenReturn(gameState);
        when(roomService.getRoomInfo(roomId)).thenReturn(roomInfo);

        // when
        controller.startGame(roomId, playerId);

        // then
        verify(roomGameService).startGame(roomId, playerId);
        verify(messagingTemplate).convertAndSend(anyString(), eq(roomInfo));
        verify(messagingTemplate).convertAndSend(anyString(), eq(gameState));
    }

    @Test
    @Tag("ws-room-chat")
    @DisplayName("sendChat - 정상: 채팅 메시지가 타임스탬프와 함께 브로드캐스트되어야 한다")
    void sendChat_shouldBroadcastWithTimestamp() {
        // given
        String roomId = "room1";
        ChatMessage message = ChatMessage.builder()
                .messageId("msg1")
                .playerId("player1")
                .nickname("플레이어1")
                .message("안녕하세요")
                .build();

        long timeBefore = System.currentTimeMillis();

        // when
        controller.sendChat(roomId, message);

        // then
        verify(messagingTemplate).convertAndSend(anyString(), argThat((ChatMessage msg) -> {
            return msg.getTimestamp() >= timeBefore;
        }));
    }

    @Test
    @Tag("ws-room-return")
    @DisplayName("returnToWaitingRoom - 정상: 대기실로 돌아가기가 처리되어야 한다")
    void returnToWaitingRoom_shouldProcessReturn() {
        // given
        String roomId = "room1";
        String playerId = "player1";

        when(roomGameService.handlePlayerReturnToWaiting(roomId, playerId)).thenReturn(roomInfo);

        // when
        controller.returnToWaitingRoom(roomId, playerId);

        // then
        verify(roomGameService).handlePlayerReturnToWaiting(roomId, playerId);
    }

    @Test
    @Tag("ws-room-return")
    @DisplayName("returnToWaitingRoom - 실패하면 에러 메시지가 브로드캐스트되어야 한다")
    void returnToWaitingRoom_failure_shouldBroadcastError() {
        // given
        String roomId = "room1";
        String playerId = "player1";

        when(roomGameService.handlePlayerReturnToWaiting(roomId, playerId))
                .thenThrow(new IllegalStateException("아직 모든 플레이어가 투표하지 않았습니다"));

        // when
        controller.returnToWaitingRoom(roomId, playerId);

        // then
        verify(messagingTemplate).convertAndSend(anyString(), any(ErrorMessage.class));
    }

    // Helper methods

    private RoomInfo createRoomInfo(String roomId) {
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
                .role(PlayerRole.PLAYER)
                .build());

        GameSettings settings = new GameSettings();
        settings.setRounds(3);
        settings.setDrawTime(90);
        settings.setMaxPlayers(10);
        settings.setWordChoices(3);

        RoomInfo info = new RoomInfo();
        info.setRoomId(roomId);
        info.setPlayers(players);
        info.setSettings(settings);
        info.setStatus(RoomStatus.WAITING);
        return info;
    }

    private GameState createGameState(String roomId) {
        List<String> turnOrder = Arrays.asList("player1", "player2");
        GameState state = new GameState(roomId, 3, 90, turnOrder);
        state.setPhase(GamePhase.COUNTDOWN);
        return state;
    }
}
