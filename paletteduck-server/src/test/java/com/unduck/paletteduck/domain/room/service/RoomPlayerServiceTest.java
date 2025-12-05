package com.unduck.paletteduck.domain.room.service;

import com.unduck.paletteduck.domain.room.dto.PlayerRole;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.domain.room.dto.RoomPlayer;
import com.unduck.paletteduck.domain.room.dto.RoomStatus;
import com.unduck.paletteduck.domain.game.dto.GameSettings;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

@DisplayName("RoomPlayerService 테스트")
@ExtendWith(MockitoExtension.class)
class RoomPlayerServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomValidator roomValidator;

    @InjectMocks
    private RoomPlayerService roomPlayerService;

    @Test
    @Tag("room-join")
    @DisplayName("joinRoom - 정상: 방에 입장하고 PLAYER 역할을 받아야 한다")
    void joinRoom_shouldAddPlayerWithPlayerRole() {
        // given
        String roomId = "room1";
        String playerId = "newPlayer"; // 겹치지 않는 ID 사용
        String nickname = "테스터";
        RoomInfo roomInfo = createRoomInfo(roomId, 1, 10, RoomStatus.WAITING);

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);
        doNothing().when(roomValidator).validateJoinRoom(any(RoomInfo.class));

        // when
        roomPlayerService.joinRoom(roomId, playerId, nickname);

        // then
        ArgumentCaptor<RoomInfo> captor = ArgumentCaptor.forClass(RoomInfo.class);
        verify(roomRepository).save(eq(roomId), captor.capture());

        RoomInfo savedRoom = captor.getValue();
        assertThat(savedRoom.getPlayers()).hasSize(2);

        RoomPlayer newPlayer = savedRoom.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);

        assertThat(newPlayer).isNotNull();
        assertThat(newPlayer.getNickname()).isEqualTo(nickname);
        assertThat(newPlayer.getRole()).isEqualTo(PlayerRole.PLAYER);
        assertThat(newPlayer.isReady()).isFalse();
        assertThat(newPlayer.isHost()).isFalse();
        assertThat(newPlayer.getJoinedAt()).isGreaterThan(0);
    }

    @Test
    @Tag("room-join")
    @DisplayName("joinRoom - 이미 입장한 플레이어는 중복 입장되지 않아야 한다")
    void joinRoom_alreadyJoined_shouldNotDuplicate() {
        // given
        String roomId = "room1";
        String playerId = "player1";
        String nickname = "테스터";
        RoomInfo roomInfo = createRoomInfo(roomId, 1, 10, RoomStatus.WAITING);

        // 이미 방에 있는 플레이어 추가
        RoomPlayer existingPlayer = RoomPlayer.builder()
                .playerId(playerId)
                .nickname(nickname)
                .role(PlayerRole.PLAYER)
                .build();
        roomInfo.getPlayers().add(existingPlayer);

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);

        int initialSize = roomInfo.getPlayers().size();

        // when
        roomPlayerService.joinRoom(roomId, playerId, nickname);

        // then
        verify(roomRepository, never()).save(any(), any());
        assertThat(roomInfo.getPlayers()).hasSize(initialSize);
    }

    @Test
    @Tag("room-join")
    @DisplayName("joinRoom - 게임 진행 중이면 SPECTATOR 역할로 입장해야 한다")
    void joinRoom_playingStatus_shouldAssignSpectatorRole() {
        // given
        String roomId = "room1";
        String playerId = "newPlayer"; // 겹치지 않는 ID 사용
        String nickname = "테스터";
        RoomInfo roomInfo = createRoomInfo(roomId, 3, 10, RoomStatus.PLAYING);

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);
        doNothing().when(roomValidator).validateJoinRoom(any(RoomInfo.class));

        // when
        roomPlayerService.joinRoom(roomId, playerId, nickname);

        // then
        ArgumentCaptor<RoomInfo> captor = ArgumentCaptor.forClass(RoomInfo.class);
        verify(roomRepository).save(eq(roomId), captor.capture());

        RoomInfo savedRoom = captor.getValue();
        RoomPlayer newPlayer = savedRoom.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);

        assertThat(newPlayer).isNotNull();
        assertThat(newPlayer.getRole()).isEqualTo(PlayerRole.SPECTATOR);
    }

    @Test
    @Tag("room-join")
    @DisplayName("joinRoom - 최대 참가자 수 초과 시 SPECTATOR 역할로 입장해야 한다")
    void joinRoom_maxPlayersReached_shouldAssignSpectatorRole() {
        // given
        String roomId = "room1";
        String playerId = "newPlayer"; // 겹치지 않는 ID 사용
        String nickname = "테스터";
        RoomInfo roomInfo = createRoomInfo(roomId, 5, 5, RoomStatus.WAITING); // 이미 5명 참가

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);
        doNothing().when(roomValidator).validateJoinRoom(any(RoomInfo.class));

        // when
        roomPlayerService.joinRoom(roomId, playerId, nickname);

        // then
        ArgumentCaptor<RoomInfo> captor = ArgumentCaptor.forClass(RoomInfo.class);
        verify(roomRepository).save(eq(roomId), captor.capture());

        RoomInfo savedRoom = captor.getValue();
        RoomPlayer newPlayer = savedRoom.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);

        assertThat(newPlayer).isNotNull();
        assertThat(newPlayer.getRole()).isEqualTo(PlayerRole.SPECTATOR);
    }

    @Test
    @Tag("room-join")
    @DisplayName("joinRoom - 방이 없으면 예외를 발생시켜야 한다")
    void joinRoom_roomNotFound_shouldThrowException() {
        // given
        String roomId = "nonexistent";
        String playerId = "player1";
        String nickname = "테스터";

        when(roomRepository.findById(roomId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> roomPlayerService.joinRoom(roomId, playerId, nickname))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Room not found");
    }

    @Test
    @Tag("room-leave")
    @DisplayName("leaveRoom - 정상: 플레이어가 방을 나가야 한다")
    void leaveRoom_shouldRemovePlayer() {
        // given
        String roomId = "room1";
        String playerId = "playerLeaving"; // 겹치지 않는 ID 사용
        RoomInfo roomInfo = createRoomInfo(roomId, 2, 10, RoomStatus.WAITING);

        RoomPlayer leavingPlayer = RoomPlayer.builder()
                .playerId(playerId)
                .nickname("떠나는플레이어")
                .role(PlayerRole.PLAYER)
                .isHost(false)
                .build();
        roomInfo.getPlayers().add(leavingPlayer);

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);

        int initialSize = roomInfo.getPlayers().size();

        // when
        RoomInfo result = roomPlayerService.leaveRoom(roomId, playerId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPlayers()).hasSize(initialSize - 1);
        assertThat(result.getPlayers()).noneMatch(p -> p.getPlayerId().equals(playerId));
        verify(roomRepository).save(eq(roomId), eq(roomInfo));
    }

    @Test
    @Tag("room-leave")
    @DisplayName("leaveRoom - 방장이 나가면 다른 플레이어가 방장이 되어야 한다")
    void leaveRoom_hostLeaves_shouldTransferHost() {
        // given
        String roomId = "room1";
        String hostId = "host";
        RoomInfo roomInfo = createRoomInfo(roomId, 0, 10, RoomStatus.WAITING);

        // 방장
        RoomPlayer host = RoomPlayer.builder()
                .playerId(hostId)
                .nickname("방장")
                .role(PlayerRole.PLAYER)
                .isHost(true)
                .joinedAt(1000L)
                .build();

        // 다른 플레이어들
        RoomPlayer player1 = RoomPlayer.builder()
                .playerId("player1")
                .nickname("플레이어1")
                .role(PlayerRole.PLAYER)
                .isHost(false)
                .joinedAt(2000L)
                .build();

        RoomPlayer player2 = RoomPlayer.builder()
                .playerId("player2")
                .nickname("플레이어2")
                .role(PlayerRole.PLAYER)
                .isHost(false)
                .joinedAt(3000L)
                .build();

        roomInfo.getPlayers().add(host);
        roomInfo.getPlayers().add(player1);
        roomInfo.getPlayers().add(player2);

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);

        // when
        RoomInfo result = roomPlayerService.leaveRoom(roomId, hostId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPlayers()).hasSize(2);

        // 가장 먼저 들어온 플레이어가 방장이 되어야 함
        RoomPlayer newHost = result.getPlayers().stream()
                .filter(RoomPlayer::isHost)
                .findFirst()
                .orElse(null);

        assertThat(newHost).isNotNull();
        assertThat(newHost.getPlayerId()).isEqualTo("player1");
        assertThat(newHost.isReady()).isFalse();
    }

    @Test
    @Tag("room-leave")
    @DisplayName("leaveRoom - 마지막 플레이어가 나가면 방이 삭제되어야 한다")
    void leaveRoom_lastPlayerLeaves_shouldDeleteRoom() {
        // given
        String roomId = "room1";
        String playerId = "host";
        RoomInfo roomInfo = createRoomInfo(roomId, 0, 10, RoomStatus.WAITING);

        RoomPlayer host = RoomPlayer.builder()
                .playerId(playerId)
                .nickname("방장")
                .role(PlayerRole.PLAYER)
                .isHost(true)
                .build();
        roomInfo.getPlayers().add(host);

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);

        // when
        RoomInfo result = roomPlayerService.leaveRoom(roomId, playerId);

        // then
        assertThat(result).isNull();
        verify(roomRepository).delete(roomId);
    }

    @Test
    @Tag("room-leave")
    @DisplayName("leaveRoom - 방이 없으면 null을 반환해야 한다")
    void leaveRoom_roomNotFound_shouldReturnNull() {
        // given
        String roomId = "nonexistent";
        String playerId = "player1";

        when(roomRepository.findById(roomId)).thenReturn(null);

        // when
        RoomInfo result = roomPlayerService.leaveRoom(roomId, playerId);

        // then
        assertThat(result).isNull();
        verify(roomRepository, never()).save(any(), any());
        verify(roomRepository, never()).delete(any());
    }

    @Test
    @Tag("room-leave")
    @DisplayName("leaveRoom - 플레이어가 방에 없으면 null을 반환해야 한다")
    void leaveRoom_playerNotFound_shouldReturnNull() {
        // given
        String roomId = "room1";
        String playerId = "nonexistent";
        RoomInfo roomInfo = createRoomInfo(roomId, 2, 10, RoomStatus.WAITING);

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);

        // when
        RoomInfo result = roomPlayerService.leaveRoom(roomId, playerId);

        // then
        assertThat(result).isNull();
        verify(roomRepository, never()).save(any(), any());
    }

    @Test
    @Tag("role-change")
    @DisplayName("changeRole - 정상: 역할을 변경하고 준비 상태를 해제해야 한다")
    void changeRole_shouldChangeRoleAndResetReady() {
        // given
        String roomId = "room1";
        String playerId = "player1";
        PlayerRole newRole = PlayerRole.SPECTATOR;
        RoomInfo roomInfo = createRoomInfo(roomId, 0, 10, RoomStatus.WAITING);

        RoomPlayer player = RoomPlayer.builder()
                .playerId(playerId)
                .nickname("플레이어1")
                .role(PlayerRole.PLAYER)
                .isHost(false)
                .isReady(true)
                .build();
        roomInfo.getPlayers().add(player);

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);

        // when
        roomPlayerService.changeRole(roomId, playerId, newRole);

        // then
        assertThat(player.getRole()).isEqualTo(PlayerRole.SPECTATOR);
        assertThat(player.isReady()).isFalse();
        verify(roomRepository).save(eq(roomId), eq(roomInfo));
    }

    @Test
    @Tag("role-change")
    @DisplayName("changeRole - 방이 없으면 예외를 발생시켜야 한다")
    void changeRole_roomNotFound_shouldThrowException() {
        // given
        String roomId = "nonexistent";
        String playerId = "player1";
        PlayerRole newRole = PlayerRole.SPECTATOR;

        when(roomRepository.findById(roomId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> roomPlayerService.changeRole(roomId, playerId, newRole))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Room not found");
    }

    @Test
    @Tag("role-change")
    @DisplayName("changeRole - 플레이어가 없으면 예외를 발생시켜야 한다")
    void changeRole_playerNotFound_shouldThrowException() {
        // given
        String roomId = "room1";
        String playerId = "nonexistent";
        PlayerRole newRole = PlayerRole.SPECTATOR;
        RoomInfo roomInfo = createRoomInfo(roomId, 2, 10, RoomStatus.WAITING);

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);

        // when & then
        assertThatThrownBy(() -> roomPlayerService.changeRole(roomId, playerId, newRole))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Player not found");
    }

    @Test
    @Tag("ready-toggle")
    @DisplayName("toggleReady - 정상: 준비 상태를 토글해야 한다")
    void toggleReady_shouldToggleReadyState() {
        // given
        String roomId = "room1";
        String playerId = "player1";
        RoomInfo roomInfo = createRoomInfo(roomId, 0, 10, RoomStatus.WAITING);

        RoomPlayer player = RoomPlayer.builder()
                .playerId(playerId)
                .nickname("플레이어1")
                .role(PlayerRole.PLAYER)
                .isHost(false)
                .isReady(false)
                .build();
        roomInfo.getPlayers().add(player);

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);

        // when - false -> true
        roomPlayerService.toggleReady(roomId, playerId);

        // then
        assertThat(player.isReady()).isTrue();
        verify(roomRepository).save(eq(roomId), eq(roomInfo));

        // when - true -> false
        roomPlayerService.toggleReady(roomId, playerId);

        // then
        assertThat(player.isReady()).isFalse();
        verify(roomRepository, times(2)).save(eq(roomId), eq(roomInfo));
    }

    @Test
    @Tag("ready-toggle")
    @DisplayName("toggleReady - 방장은 준비 상태를 토글할 수 없어야 한다")
    void toggleReady_host_shouldNotToggle() {
        // given
        String roomId = "room1";
        String hostId = "host";
        RoomInfo roomInfo = createRoomInfo(roomId, 0, 10, RoomStatus.WAITING);

        RoomPlayer host = RoomPlayer.builder()
                .playerId(hostId)
                .nickname("방장")
                .role(PlayerRole.PLAYER)
                .isHost(true)
                .isReady(false)
                .build();
        roomInfo.getPlayers().add(host);

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);

        // when
        roomPlayerService.toggleReady(roomId, hostId);

        // then
        assertThat(host.isReady()).isFalse();
        verify(roomRepository, never()).save(any(), any());
    }

    @Test
    @Tag("ready-toggle")
    @DisplayName("toggleReady - 방이 없으면 예외를 발생시켜야 한다")
    void toggleReady_roomNotFound_shouldThrowException() {
        // given
        String roomId = "nonexistent";
        String playerId = "player1";

        when(roomRepository.findById(roomId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> roomPlayerService.toggleReady(roomId, playerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Room not found");
    }

    @Test
    @Tag("ready-toggle")
    @DisplayName("toggleReady - 플레이어가 없으면 예외를 발생시켜야 한다")
    void toggleReady_playerNotFound_shouldThrowException() {
        // given
        String roomId = "room1";
        String playerId = "nonexistent";
        RoomInfo roomInfo = createRoomInfo(roomId, 2, 10, RoomStatus.WAITING);

        when(roomRepository.findById(roomId)).thenReturn(roomInfo);

        // when & then
        assertThatThrownBy(() -> roomPlayerService.toggleReady(roomId, playerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Player not found");
    }

    // Helper methods

    private RoomInfo createRoomInfo(String roomId, int playerCount, int maxPlayers, RoomStatus status) {
        List<RoomPlayer> players = new ArrayList<>();

        // 플레이어 추가 (PLAYER 역할만)
        for (int i = 0; i < playerCount; i++) {
            players.add(RoomPlayer.builder()
                    .playerId("player" + i)
                    .nickname("플레이어" + i)
                    .isHost(i == 0)
                    .role(PlayerRole.PLAYER)
                    .joinedAt(System.currentTimeMillis() + i)
                    .build());
        }

        GameSettings settings = new GameSettings();
        settings.setMaxPlayers(maxPlayers);
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
}
