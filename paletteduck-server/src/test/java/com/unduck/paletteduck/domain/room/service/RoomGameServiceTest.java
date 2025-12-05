package com.unduck.paletteduck.domain.room.service;

import com.unduck.paletteduck.domain.game.dto.GameSettings;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.service.AsyncGameTimerScheduler;
import com.unduck.paletteduck.domain.game.service.GameService;
import com.unduck.paletteduck.domain.room.dto.*;
import com.unduck.paletteduck.domain.room.repository.ReturnToWaitingTrackerRepository;
import com.unduck.paletteduck.domain.room.repository.RoomRepository;
import com.unduck.paletteduck.domain.room.validator.RoomValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("RoomGameService 테스트")
@ExtendWith(MockitoExtension.class)
class RoomGameServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomValidator roomValidator;

    @Mock
    private GameService gameService;

    @Mock
    private AsyncGameTimerScheduler asyncGameTimerScheduler;

    @Mock
    private ReturnToWaitingTrackerRepository trackerRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private RoomGameService roomGameService;

    @Test
    @Tag("game-start")
    @DisplayName("startGame - 정상: 게임을 시작하고 PLAYING 상태로 변경해야 한다")
    void startGame_shouldStartGameAndSetPlayingStatus() {
        // given
        String roomId = "room1";
        String playerId = "host";
        RoomInfo roomInfo = createRoomInfo(roomId, RoomStatus.WAITING);
        GameState gameState = createGameState(roomId);

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);
        when(gameService.initializeGame(roomInfo)).thenReturn(gameState);

        // when
        GameState result = roomGameService.startGame(roomId, playerId);

        // then
        assertThat(result).isEqualTo(gameState);
        assertThat(roomInfo.getStatus()).isEqualTo(RoomStatus.PLAYING);
        verify(roomRepository).save(eq(roomId), eq(roomInfo));
        verify(gameService).initializeGame(eq(roomInfo));
        verify(asyncGameTimerScheduler).startCountdown(eq(roomId));
    }

    @Test
    @Tag("game-start")
    @DisplayName("startGame - 방이 없으면 예외를 발생시켜야 한다")
    void startGame_roomNotFound_shouldThrowException() {
        // given
        String roomId = "nonexistent";
        String playerId = "host";

        when(roomRepository.findById(roomId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> roomGameService.startGame(roomId, playerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Room not found");
    }

    @Test
    @Tag("settings-update")
    @DisplayName("updateSettings - 정상: 방장이 설정을 변경해야 한다")
    void updateSettings_byHost_shouldUpdateSettings() {
        // given
        String roomId = "room1";
        String hostId = "host";
        RoomInfo roomInfo = createRoomInfo(roomId, RoomStatus.WAITING);
        GameSettings newSettings = new GameSettings();
        newSettings.setRounds(5);
        newSettings.setDrawTime(120);
        newSettings.setMaxPlayers(8);

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);

        // when
        roomGameService.updateSettings(roomId, hostId, newSettings);

        // then
        assertThat(roomInfo.getSettings()).isEqualTo(newSettings);
        verify(roomValidator).validateSettingsUpdate(eq(roomInfo), eq(newSettings));
        verify(roomRepository).save(eq(roomId), eq(roomInfo));
    }

    @Test
    @Tag("settings-update")
    @DisplayName("updateSettings - 방장이 아니면 설정을 변경하지 않아야 한다")
    void updateSettings_notHost_shouldNotUpdate() {
        // given
        String roomId = "room1";
        String playerId = "player1";
        RoomInfo roomInfo = createRoomInfo(roomId, RoomStatus.WAITING);
        GameSettings originalSettings = roomInfo.getSettings();
        GameSettings newSettings = new GameSettings();
        newSettings.setRounds(5);

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);

        // when
        roomGameService.updateSettings(roomId, playerId, newSettings);

        // then
        assertThat(roomInfo.getSettings()).isEqualTo(originalSettings);
        verify(roomRepository, never()).save(any(), any());
    }

    @Test
    @Tag("settings-update")
    @DisplayName("updateSettings - 방이 없으면 예외를 발생시켜야 한다")
    void updateSettings_roomNotFound_shouldThrowException() {
        // given
        String roomId = "nonexistent";
        String playerId = "host";
        GameSettings newSettings = new GameSettings();

        when(roomRepository.findById(roomId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> roomGameService.updateSettings(roomId, playerId, newSettings))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Room not found");
    }

    @Test
    @Tag("return-to-waiting")
    @DisplayName("returnToWaitingRoom - 정상: 방 상태를 WAITING으로 변경하고 플레이어 준비 상태를 초기화해야 한다")
    void returnToWaitingRoom_shouldResetRoomToWaiting() {
        // given
        String roomId = "room1";
        RoomInfo roomInfo = createRoomInfo(roomId, RoomStatus.PLAYING);

        // 플레이어들의 준비 상태 설정
        roomInfo.getPlayers().forEach(p -> p.setReady(true));

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);

        // when
        RoomInfo result = roomGameService.returnToWaitingRoom(roomId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RoomStatus.WAITING);
        assertThat(result.getPlayers()).allMatch(p -> !p.isReady());
        verify(roomRepository).save(eq(roomId), eq(roomInfo));
        verify(gameService).deleteGame(eq(roomId));
    }

    @Test
    @Tag("return-to-waiting")
    @DisplayName("returnToWaitingRoom - 이미 WAITING 상태면 아무 작업도 하지 않아야 한다")
    void returnToWaitingRoom_alreadyWaiting_shouldDoNothing() {
        // given
        String roomId = "room1";
        RoomInfo roomInfo = createRoomInfo(roomId, RoomStatus.WAITING);

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);

        // when
        RoomInfo result = roomGameService.returnToWaitingRoom(roomId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RoomStatus.WAITING);
        verify(roomRepository, never()).save(any(), any());
        verify(gameService, never()).deleteGame(any());
    }

    @Test
    @Tag("return-to-waiting")
    @DisplayName("returnToWaitingRoom - 방이 없으면 예외를 발생시켜야 한다")
    void returnToWaitingRoom_roomNotFound_shouldThrowException() {
        // given
        String roomId = "nonexistent";

        when(roomRepository.findById(roomId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> roomGameService.returnToWaitingRoom(roomId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Room not found");
    }

    @Test
    @Tag("player-return")
    @DisplayName("handlePlayerReturnToWaiting - 정상: 원래 방장이 먼저 복귀하면 방장 권한을 유지해야 한다")
    void handlePlayerReturnToWaiting_originalHostFirst_shouldKeepHostAuthority() {
        // given
        String roomId = "room1";
        String hostId = "host";
        RoomInfo roomInfo = createRoomInfo(roomId, RoomStatus.PLAYING);
        ReturnToWaitingTracker tracker = new ReturnToWaitingTracker(roomId, hostId);

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);
        when(trackerRepository.findById(roomId)).thenReturn(tracker);

        // when
        RoomInfo result = roomGameService.handlePlayerReturnToWaiting(roomId, hostId);

        // then
        assertThat(result).isNotNull();
        assertThat(tracker.isHasOriginalHostReturned()).isTrue();
        assertThat(tracker.isHasPlayerOrHostReturned()).isTrue();

        RoomPlayer host = result.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(hostId))
                .findFirst()
                .orElse(null);

        assertThat(host).isNotNull();
        assertThat(host.isHost()).isTrue();
        verify(trackerRepository).save(eq(roomId), eq(tracker));
        verify(messagingTemplate).convertAndSend(anyString(), eq(roomInfo));
    }

    @Test
    @Tag("player-return")
    @DisplayName("handlePlayerReturnToWaiting - 정상: 참가자가 먼저 복귀하면 첫 복귀 플레이어로 기록되어야 한다")
    void handlePlayerReturnToWaiting_playerFirst_shouldRecordFirstPlayer() {
        // given
        String roomId = "room1";
        String playerId = "player1";
        String hostId = "host";
        RoomInfo roomInfo = createRoomInfo(roomId, RoomStatus.PLAYING);
        ReturnToWaitingTracker tracker = new ReturnToWaitingTracker(roomId, hostId);

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);
        when(trackerRepository.findById(roomId)).thenReturn(tracker);

        // when
        RoomInfo result = roomGameService.handlePlayerReturnToWaiting(roomId, playerId);

        // then
        assertThat(result).isNotNull();
        assertThat(tracker.getFirstReturnedPlayerId()).isEqualTo(playerId);
        assertThat(tracker.isHasPlayerOrHostReturned()).isTrue();
        assertThat(tracker.isHasOriginalHostReturned()).isFalse();
        verify(trackerRepository).save(eq(roomId), eq(tracker));
    }

    @Test
    @Tag("player-return")
    @DisplayName("handlePlayerReturnToWaiting - 관전자가 첫 복귀자가 되려고 하면 예외를 발생시켜야 한다")
    void handlePlayerReturnToWaiting_spectatorFirst_shouldThrowException() {
        // given
        String roomId = "room1";
        String spectatorId = "spectator1";
        String hostId = "host";
        RoomInfo roomInfo = createRoomInfo(roomId, RoomStatus.PLAYING);

        // 관전자 추가
        RoomPlayer spectator = RoomPlayer.builder()
                .playerId(spectatorId)
                .nickname("관전자")
                .role(PlayerRole.SPECTATOR)
                .isHost(false)
                .build();
        roomInfo.getPlayers().add(spectator);

        ReturnToWaitingTracker tracker = new ReturnToWaitingTracker(roomId, hostId);

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);
        when(trackerRepository.findById(roomId)).thenReturn(tracker);

        // when & then
        assertThatThrownBy(() -> roomGameService.handlePlayerReturnToWaiting(roomId, spectatorId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("관전자는 참가자나 방장이 먼저 복귀한 후에 복귀할 수 있습니다");
    }

    @Test
    @Tag("player-return")
    @DisplayName("handlePlayerReturnToWaiting - 이미 복귀한 플레이어가 다시 복귀하려고 하면 예외를 발생시켜야 한다")
    void handlePlayerReturnToWaiting_alreadyReturned_shouldThrowException() {
        // given
        String roomId = "room1";
        String playerId = "player1";
        String hostId = "host";
        RoomInfo roomInfo = createRoomInfo(roomId, RoomStatus.PLAYING);
        ReturnToWaitingTracker tracker = new ReturnToWaitingTracker(roomId, hostId);
        tracker.addReturnedPlayer(playerId);

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);
        when(trackerRepository.findById(roomId)).thenReturn(tracker);

        // when & then
        assertThatThrownBy(() -> roomGameService.handlePlayerReturnToWaiting(roomId, playerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 복귀한 플레이어입니다");
    }

    @Test
    @Tag("player-return")
    @DisplayName("handlePlayerReturnToWaiting - 첫 번째 복귀자는 방을 WAITING 상태로 변경해야 한다")
    void handlePlayerReturnToWaiting_firstReturner_shouldChangeRoomToWaiting() {
        // given
        String roomId = "room1";
        String playerId = "player1";
        String hostId = "host";
        RoomInfo roomInfo = createRoomInfo(roomId, RoomStatus.PLAYING);
        ReturnToWaitingTracker tracker = new ReturnToWaitingTracker(roomId, hostId);

        when(roomRepository.findById(roomId)).thenReturn(roomInfo)
                .thenReturn(roomInfo); // returnToWaitingRoom 내부에서 한 번 더 호출됨
        when(trackerRepository.findById(roomId)).thenReturn(tracker);

        // when
        RoomInfo result = roomGameService.handlePlayerReturnToWaiting(roomId, playerId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RoomStatus.WAITING);
        verify(gameService).deleteGame(eq(roomId));
    }

    @Test
    @Tag("player-return")
    @DisplayName("handlePlayerReturnToWaiting - 원래 방장이 나중에 복귀하면 방장 권한을 복원해야 한다")
    void handlePlayerReturnToWaiting_originalHostReturnsLater_shouldRestoreHostAuthority() {
        // given
        String roomId = "room1";
        String playerId = "player1";
        String hostId = "host";
        RoomInfo roomInfo = createRoomInfo(roomId, RoomStatus.PLAYING);

        // 먼저 플레이어가 복귀했고, 원래 방장은 아직 복귀하지 않은 상태
        ReturnToWaitingTracker tracker = new ReturnToWaitingTracker(roomId, hostId);
        tracker.addReturnedPlayer(playerId);
        tracker.setFirstReturnedPlayerId(playerId);
        tracker.setHasPlayerOrHostReturned(true);

        // 원래 방장의 권한을 임시로 제거한 상태로 시뮬레이션
        RoomPlayer host = roomInfo.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(hostId))
                .findFirst()
                .orElse(null);
        host.setHost(false);

        RoomPlayer player1 = roomInfo.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);
        player1.setHost(false); // 임시 방장도 아닌 상태

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);
        when(trackerRepository.findById(roomId)).thenReturn(tracker);

        // when - 원래 방장이 복귀
        RoomInfo result = roomGameService.handlePlayerReturnToWaiting(roomId, hostId);

        // then
        assertThat(result).isNotNull();
        assertThat(tracker.isHasOriginalHostReturned()).isTrue();

        RoomPlayer returnedHost = result.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(hostId))
                .findFirst()
                .orElse(null);

        assertThat(returnedHost).isNotNull();
        assertThat(returnedHost.isHost()).isTrue();
    }

    @Test
    @Tag("player-return")
    @DisplayName("handlePlayerReturnToWaiting - 방이 없으면 예외를 발생시켜야 한다")
    void handlePlayerReturnToWaiting_roomNotFound_shouldThrowException() {
        // given
        String roomId = "nonexistent";
        String playerId = "player1";

        when(roomRepository.findById(roomId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> roomGameService.handlePlayerReturnToWaiting(roomId, playerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Room not found");
    }

    // Helper methods

    private RoomInfo createRoomInfo(String roomId, RoomStatus status) {
        List<RoomPlayer> players = new ArrayList<>();

        // 방장
        RoomPlayer host = RoomPlayer.builder()
                .playerId("host")
                .nickname("방장")
                .isHost(true)
                .role(PlayerRole.PLAYER)
                .joinedAt(1000L)
                .build();

        // 일반 플레이어
        RoomPlayer player1 = RoomPlayer.builder()
                .playerId("player1")
                .nickname("플레이어1")
                .isHost(false)
                .role(PlayerRole.PLAYER)
                .joinedAt(2000L)
                .build();

        RoomPlayer player2 = RoomPlayer.builder()
                .playerId("player2")
                .nickname("플레이어2")
                .isHost(false)
                .role(PlayerRole.PLAYER)
                .joinedAt(3000L)
                .build();

        players.add(host);
        players.add(player1);
        players.add(player2);

        GameSettings settings = new GameSettings();
        settings.setMaxPlayers(10);
        settings.setRounds(3);
        settings.setDrawTime(90);
        settings.setWordChoices(3);

        RoomInfo roomInfo = new RoomInfo();
        roomInfo.setRoomId(roomId);
        roomInfo.setPlayers(players);
        roomInfo.setSettings(settings);
        roomInfo.setStatus(status);

        return roomInfo;
    }

    private GameState createGameState(String roomId) {
        List<String> turnOrder = Arrays.asList("host", "player1", "player2");
        return new GameState(roomId, 3, 90, turnOrder);
    }
}
