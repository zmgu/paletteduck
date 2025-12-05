package com.unduck.paletteduck.domain.room.service;

import com.unduck.paletteduck.domain.game.dto.GameSettings;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.repository.GameRepository;
import com.unduck.paletteduck.domain.room.dto.*;
import com.unduck.paletteduck.domain.room.repository.RoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("RoomService 테스트")
@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private RoomService roomService;

    @Test
    @Tag("room-create")
    @DisplayName("createRoom - 정상: 공개방을 생성해야 한다")
    void createRoom_public_shouldCreatePublicRoom() {
        // given
        String playerId = "player1";
        String nickname = "테스터";
        boolean isPublic = true;

        // when
        RoomCreateResponse response = roomService.createRoom(playerId, nickname, isPublic);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRoomId()).isNotNull();
        assertThat(response.getInviteCode()).isNotNull();
        verify(roomRepository).save(anyString(), any(RoomInfo.class));
        verify(roomRepository).findById(anyString());
    }

    @Test
    @Tag("room-create")
    @DisplayName("createRoom - 정상: 비공개방을 생성해야 한다")
    void createRoom_private_shouldCreatePrivateRoom() {
        // given
        String playerId = "player1";
        String nickname = "테스터";
        boolean isPublic = false;

        // when
        RoomCreateResponse response = roomService.createRoom(playerId, nickname, isPublic);

        // then
        assertThat(response).isNotNull();
        verify(roomRepository).save(anyString(), any(RoomInfo.class));
    }

    @Test
    @Tag("room-random-match")
    @DisplayName("findRandomPublicRoom - 정상: 사용 가능한 공개방을 반환해야 한다")
    void findRandomPublicRoom_shouldReturnAvailableRoom() {
        // given
        List<RoomInfo> rooms = Arrays.asList(
                createRoomInfo("room1", true, RoomStatus.WAITING, 3, 10),
                createRoomInfo("room2", true, RoomStatus.WAITING, 5, 10)
        );
        when(roomRepository.findAll()).thenReturn(rooms);

        // when
        RoomInfo result = roomService.findRandomPublicRoom();

        // then
        assertThat(result).isNotNull();
        assertThat(result.isPublic()).isTrue();
        assertThat(result.getStatus()).isEqualTo(RoomStatus.WAITING);
    }

    @Test
    @Tag("room-random-match")
    @DisplayName("findRandomPublicRoom - 사용 가능한 방이 없으면 null을 반환해야 한다")
    void findRandomPublicRoom_noAvailableRooms_shouldReturnNull() {
        // given
        List<RoomInfo> rooms = Arrays.asList(
                createRoomInfo("room1", false, RoomStatus.WAITING, 3, 10), // 비공개
                createRoomInfo("room2", true, RoomStatus.PLAYING, 5, 10), // 진행 중
                createRoomInfo("room3", true, RoomStatus.WAITING, 10, 10) // 가득참
        );
        when(roomRepository.findAll()).thenReturn(rooms);

        // when
        RoomInfo result = roomService.findRandomPublicRoom();

        // then
        assertThat(result).isNull();
    }

    @Test
    @Tag("room-availability")
    @DisplayName("isRoomAvailable - 공개방, WAITING, 자리 있음 -> true")
    void isRoomAvailable_publicWaitingNotFull_shouldReturnTrue() {
        // given
        List<RoomInfo> rooms = Arrays.asList(
                createRoomInfo("room1", true, RoomStatus.WAITING, 3, 10)
        );
        when(roomRepository.findAll()).thenReturn(rooms);

        // when
        RoomInfo result = roomService.findRandomPublicRoom();

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @Tag("room-availability")
    @DisplayName("isRoomAvailable - 비공개방 -> false")
    void isRoomAvailable_privateRoom_shouldReturnFalse() {
        // given
        List<RoomInfo> rooms = Arrays.asList(
                createRoomInfo("room1", false, RoomStatus.WAITING, 3, 10)
        );
        when(roomRepository.findAll()).thenReturn(rooms);

        // when
        RoomInfo result = roomService.findRandomPublicRoom();

        // then
        assertThat(result).isNull();
    }

    @Test
    @Tag("room-availability")
    @DisplayName("isRoomAvailable - PLAYING 상태 -> false")
    void isRoomAvailable_playingStatus_shouldReturnFalse() {
        // given
        List<RoomInfo> rooms = Arrays.asList(
                createRoomInfo("room1", true, RoomStatus.PLAYING, 3, 10)
        );
        when(roomRepository.findAll()).thenReturn(rooms);

        // when
        RoomInfo result = roomService.findRandomPublicRoom();

        // then
        assertThat(result).isNull();
    }

    @Test
    @Tag("room-availability")
    @DisplayName("isRoomAvailable - 방이 가득참 -> false")
    void isRoomAvailable_roomFull_shouldReturnFalse() {
        // given
        List<RoomInfo> rooms = Arrays.asList(
                createRoomInfo("room1", true, RoomStatus.WAITING, 10, 10)
        );
        when(roomRepository.findAll()).thenReturn(rooms);

        // when
        RoomInfo result = roomService.findRandomPublicRoom();

        // then
        assertThat(result).isNull();
    }

    @Test
    @Tag("room-availability")
    @DisplayName("countParticipants - 관전자를 제외한 참가자 수를 반환해야 한다")
    void countParticipants_shouldExcludeSpectators() {
        // given
        RoomInfo room = createRoomInfo("room1", true, RoomStatus.WAITING, 3, 10);
        // 관전자 추가
        RoomPlayer spectator = RoomPlayer.builder()
                .playerId("spectator1")
                .nickname("관전자")
                .role(PlayerRole.SPECTATOR)
                .build();
        room.getPlayers().add(spectator);

        List<RoomInfo> rooms = Arrays.asList(room);
        when(roomRepository.findAll()).thenReturn(rooms);

        // when
        RoomInfo result = roomService.findRandomPublicRoom();

        // then
        assertThat(result).isNotNull(); // 관전자는 인원수에 포함되지 않으므로 여전히 입장 가능
    }

    @Test
    @Tag("room-list")
    @DisplayName("getPublicRoomList - 공개방만 필터링해야 한다")
    void getPublicRoomList_shouldFilterPublicRoomsOnly() {
        // given
        List<RoomInfo> rooms = Arrays.asList(
                createRoomInfo("room1", true, RoomStatus.WAITING, 3, 10),
                createRoomInfo("room2", false, RoomStatus.WAITING, 5, 10),
                createRoomInfo("room3", true, RoomStatus.PLAYING, 7, 10)
        );
        when(roomRepository.findAll()).thenReturn(rooms);

        // when
        List<RoomListResponse> result = roomService.getPublicRoomList();

        // then
        assertThat(result).hasSize(2); // 공개방 2개만
        assertThat(result).allMatch(r -> r.getRoomId().equals("room1") || r.getRoomId().equals("room3"));
    }

    @Test
    @Tag("room-search")
    @DisplayName("findRoomByInviteCode - 정상: 초대코드로 방을 찾아야 한다")
    void findRoomByInviteCode_shouldFindRoom() {
        // given
        String inviteCode = "ABC123";
        RoomInfo room = createRoomInfo("room1", true, RoomStatus.WAITING, 3, 10);
        room.setInviteCode(inviteCode);
        List<RoomInfo> rooms = Arrays.asList(room);
        when(roomRepository.findAll()).thenReturn(rooms);

        // when
        RoomInfo result = roomService.findRoomByInviteCode(inviteCode);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getInviteCode()).isEqualTo(inviteCode);
    }

    @Test
    @Tag("room-search")
    @DisplayName("findRoomByInviteCode - 못 찾으면 null을 반환해야 한다")
    void findRoomByInviteCode_notFound_shouldReturnNull() {
        // given
        String inviteCode = "NOTFOUND";
        List<RoomInfo> rooms = Arrays.asList(
                createRoomInfo("room1", true, RoomStatus.WAITING, 3, 10)
        );
        when(roomRepository.findAll()).thenReturn(rooms);

        // when
        RoomInfo result = roomService.findRoomByInviteCode(inviteCode);

        // then
        assertThat(result).isNull();
    }

    @Test
    @Tag("room-management")
    @DisplayName("getRoomInfo - 정상: 방 정보를 조회해야 한다")
    void getRoomInfo_shouldReturnRoomInfo() {
        // given
        String roomId = "room1";
        RoomInfo roomInfo = createRoomInfo(roomId, true, RoomStatus.WAITING, 3, 10);
        when(roomRepository.findById(roomId)).thenReturn(roomInfo);

        // when
        RoomInfo result = roomService.getRoomInfo(roomId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRoomId()).isEqualTo(roomId);
        verify(roomRepository).findById(roomId);
    }

    @Test
    @Tag("room-management")
    @DisplayName("getRoomInfo - 방이 없으면 null을 반환해야 한다")
    void getRoomInfo_notFound_shouldReturnNull() {
        // given
        String roomId = "nonexistent";
        when(roomRepository.findById(roomId)).thenReturn(null);

        // when
        RoomInfo result = roomService.getRoomInfo(roomId);

        // then
        assertThat(result).isNull();
    }

    @Test
    @Tag("room-management")
    @DisplayName("saveRoomInfo - 정상: 방 정보를 저장해야 한다")
    void saveRoomInfo_shouldSaveRoom() {
        // given
        String roomId = "room1";
        RoomInfo roomInfo = createRoomInfo(roomId, true, RoomStatus.WAITING, 3, 10);

        // when
        roomService.saveRoomInfo(roomId, roomInfo);

        // then
        verify(roomRepository).save(roomId, roomInfo);
    }

    @Test
    @Tag("room-management")
    @DisplayName("deleteRoom - 정상: 방을 삭제해야 한다")
    void deleteRoom_shouldDeleteRoom() {
        // given
        String roomId = "room1";

        // when
        roomService.deleteRoom(roomId);

        // then
        verify(roomRepository).delete(roomId);
    }

    @Test
    @Tag("room-list")
    @DisplayName("getPublicRoomList - 정상: 최신순으로 정렬되어야 한다")
    void getPublicRoomList_shouldSortByCreatedAtDescending() {
        // given
        RoomInfo oldRoom = createRoomInfo("room1", true, RoomStatus.WAITING, 3, 10);
        RoomInfo newRoom = createRoomInfo("room2", true, RoomStatus.WAITING, 3, 10);

        // 방장의 joinedAt을 다르게 설정
        oldRoom.getPlayers().get(0).setJoinedAt(1000L);
        newRoom.getPlayers().get(0).setJoinedAt(2000L);

        List<RoomInfo> rooms = Arrays.asList(oldRoom, newRoom);
        when(roomRepository.findAll()).thenReturn(rooms);

        // when
        List<RoomListResponse> result = roomService.getPublicRoomList();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRoomId()).isEqualTo("room2"); // 최신 방이 먼저
        assertThat(result.get(1).getRoomId()).isEqualTo("room1");
    }

    @Test
    @Tag("room-list")
    @DisplayName("getPublicRoomList - 게임 진행 중인 방은 라운드 정보를 포함해야 한다")
    void getPublicRoomList_playingRoom_shouldIncludeRoundInfo() {
        // given
        RoomInfo playingRoom = createRoomInfo("room1", true, RoomStatus.PLAYING, 3, 10);
        List<String> turnOrder = Arrays.asList("host", "player1", "player2");
        GameState gameState = new GameState("room1", 3, 90, turnOrder);
        gameState.setCurrentRound(2);

        List<RoomInfo> rooms = Arrays.asList(playingRoom);
        when(roomRepository.findAll()).thenReturn(rooms);
        when(gameRepository.findById("room1")).thenReturn(gameState);

        // when
        List<RoomListResponse> result = roomService.getPublicRoomList();

        // then
        assertThat(result).hasSize(1);
        RoomListResponse response = result.get(0);
        assertThat(response.getCurrentRound()).isEqualTo(2);
        assertThat(response.getTotalRounds()).isEqualTo(3);
    }

    @Test
    @Tag("room-list")
    @DisplayName("getPublicRoomList - 대기 중인 방은 라운드 정보가 null이어야 한다")
    void getPublicRoomList_waitingRoom_shouldHaveNullRoundInfo() {
        // given
        RoomInfo waitingRoom = createRoomInfo("room1", true, RoomStatus.WAITING, 3, 10);
        List<RoomInfo> rooms = Arrays.asList(waitingRoom);
        when(roomRepository.findAll()).thenReturn(rooms);

        // when
        List<RoomListResponse> result = roomService.getPublicRoomList();

        // then
        assertThat(result).hasSize(1);
        RoomListResponse response = result.get(0);
        assertThat(response.getCurrentRound()).isNull();
        assertThat(response.getTotalRounds()).isNull();
    }

    // Helper methods

    private RoomInfo createRoomInfo(String roomId, boolean isPublic, RoomStatus status, int playerCount, int maxPlayers) {
        List<RoomPlayer> players = new ArrayList<>();

        // 방장 추가
        players.add(RoomPlayer.builder()
                .playerId("host")
                .nickname("방장")
                .isHost(true)
                .role(PlayerRole.PLAYER)
                .joinedAt(System.currentTimeMillis())
                .build());

        // 나머지 플레이어 추가
        for (int i = 1; i < playerCount; i++) {
            players.add(RoomPlayer.builder()
                    .playerId("player" + i)
                    .nickname("플레이어" + i)
                    .isHost(false)
                    .role(PlayerRole.PLAYER)
                    .build());
        }

        GameSettings settings = new GameSettings();
        settings.setMaxPlayers(maxPlayers);
        settings.setRounds(3);
        settings.setDrawTime(90);
        settings.setWordChoices(3);

        RoomInfo roomInfo = new RoomInfo();
        roomInfo.setRoomId(roomId);
        roomInfo.setInviteCode(roomId);
        roomInfo.setPlayers(players);
        roomInfo.setSettings(settings);
        roomInfo.setStatus(status);
        roomInfo.setPublic(isPublic);

        return roomInfo;
    }
}
