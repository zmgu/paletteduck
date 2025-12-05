package com.unduck.paletteduck.domain.room.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unduck.paletteduck.domain.game.dto.GameSettings;
import com.unduck.paletteduck.domain.room.dto.*;
import com.unduck.paletteduck.domain.room.service.RoomPlayerService;
import com.unduck.paletteduck.domain.room.service.RoomService;
import com.unduck.paletteduck.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("RoomController 테스트")
@WebMvcTest(RoomController.class)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomService roomService;

    @MockBean
    private RoomPlayerService roomPlayerService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    @Tag("api-room-create")
    @DisplayName("createRoom - 정상: 공개방을 생성해야 한다")
    void createRoom_public_shouldCreateRoom() throws Exception {
        // given
        String token = "Bearer test-jwt-token";
        String playerId = "player1";
        String nickname = "테스터";

        RoomCreateRequest request = new RoomCreateRequest(true);
        RoomCreateResponse response = new RoomCreateResponse("room1", "INVITE123");

        when(jwtUtil.getPlayerIdFromToken("test-jwt-token")).thenReturn(playerId);
        when(jwtUtil.getNicknameFromToken("test-jwt-token")).thenReturn(nickname);
        when(roomService.createRoom(playerId, nickname, true)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/room/create")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value("room1"))
                .andExpect(jsonPath("$.inviteCode").value("INVITE123"));

        verify(roomService).createRoom(playerId, nickname, true);
    }

    @Test
    @Tag("api-room-create")
    @DisplayName("createRoom - 비공개방을 생성해야 한다")
    void createRoom_private_shouldCreatePrivateRoom() throws Exception {
        // given
        String token = "Bearer test-jwt-token";
        String playerId = "player1";
        String nickname = "테스터";

        RoomCreateRequest request = new RoomCreateRequest(false);
        RoomCreateResponse response = new RoomCreateResponse("room1", "INVITE123");

        when(jwtUtil.getPlayerIdFromToken("test-jwt-token")).thenReturn(playerId);
        when(jwtUtil.getNicknameFromToken("test-jwt-token")).thenReturn(nickname);
        when(roomService.createRoom(playerId, nickname, false)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/room/create")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value("room1"))
                .andExpect(jsonPath("$.inviteCode").value("INVITE123"));

        verify(roomService).createRoom(playerId, nickname, false);
    }

    @Test
    @Tag("api-room-info")
    @DisplayName("getRoomInfo - 정상: 방 정보를 반환해야 한다")
    void getRoomInfo_shouldReturnRoomInfo() throws Exception {
        // given
        String roomId = "room1";
        RoomInfo roomInfo = createRoomInfo(roomId);

        when(roomService.getRoomInfo(roomId)).thenReturn(roomInfo);

        // when & then
        mockMvc.perform(get("/api/room/{roomId}", roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(roomId))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(roomService).getRoomInfo(roomId);
    }

    @Test
    @Tag("api-room-info")
    @DisplayName("getRoomInfo - 방이 없으면 404를 반환해야 한다")
    void getRoomInfo_notFound_shouldReturn404() throws Exception {
        // given
        String roomId = "nonexistent";
        when(roomService.getRoomInfo(roomId)).thenReturn(null);

        // when & then
        mockMvc.perform(get("/api/room/{roomId}", roomId))
                .andExpect(status().isNotFound());

        verify(roomService).getRoomInfo(roomId);
    }

    @Test
    @Tag("api-room-join")
    @DisplayName("joinRoom - 정상: 방에 입장해야 한다")
    void joinRoom_shouldJoinRoom() throws Exception {
        // given
        String roomId = "room1";
        String token = "Bearer test-jwt-token";
        String playerId = "player1";
        String nickname = "테스터";

        RoomInfo roomInfo = createRoomInfo(roomId);
        RoomInfo updatedRoomInfo = createRoomInfo(roomId);

        when(jwtUtil.getPlayerIdFromToken("test-jwt-token")).thenReturn(playerId);
        when(jwtUtil.getNicknameFromToken("test-jwt-token")).thenReturn(nickname);
        when(roomService.getRoomInfo(roomId)).thenReturn(roomInfo, updatedRoomInfo);

        // when & then
        mockMvc.perform(post("/api/room/{roomId}/join", roomId)
                        .header("Authorization", token))
                .andExpect(status().isOk());

        verify(roomPlayerService).joinRoom(roomId, playerId, nickname);
    }

    @Test
    @Tag("api-room-leave")
    @DisplayName("leaveRoom - 정상: 방을 퇴장해야 한다")
    void leaveRoom_shouldLeaveRoom() throws Exception {
        // given
        String roomId = "room1";
        String token = "Bearer test-jwt-token";
        String playerId = "player1";

        when(jwtUtil.getPlayerIdFromToken("test-jwt-token")).thenReturn(playerId);

        // when & then
        mockMvc.perform(post("/api/room/{roomId}/leave", roomId)
                        .header("Authorization", token))
                .andExpect(status().isOk());

        verify(roomPlayerService).leaveRoom(roomId, playerId);
    }

    @Test
    @Tag("api-room-list")
    @DisplayName("getRoomList - 정상: 공개방 목록을 반환해야 한다")
    void getRoomList_shouldReturnPublicRooms() throws Exception {
        // given
        List<RoomListResponse> roomList = Arrays.asList(
                RoomListResponse.builder()
                        .roomId("room1")
                        .currentPlayers(3)
                        .maxPlayers(10)
                        .status(RoomStatus.WAITING)
                        .build(),
                RoomListResponse.builder()
                        .roomId("room2")
                        .currentPlayers(5)
                        .maxPlayers(10)
                        .status(RoomStatus.WAITING)
                        .build()
        );

        when(roomService.getPublicRoomList()).thenReturn(roomList);

        // when & then
        mockMvc.perform(get("/api/room/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].roomId").value("room1"))
                .andExpect(jsonPath("$[0].currentPlayers").value(3))
                .andExpect(jsonPath("$[1].roomId").value("room2"));

        verify(roomService).getPublicRoomList();
    }

    @Test
    @Tag("api-room-random")
    @DisplayName("joinRandomRoom - 정상: 랜덤 공개방에 입장해야 한다")
    void joinRandomRoom_shouldJoinRandomRoom() throws Exception {
        // given
        String token = "Bearer test-jwt-token";
        String playerId = "player1";
        String nickname = "테스터";
        String roomId = "room1";

        RoomInfo randomRoom = createRoomInfo(roomId);
        RoomInfo updatedRoomInfo = createRoomInfo(roomId);

        when(jwtUtil.getPlayerIdFromToken("test-jwt-token")).thenReturn(playerId);
        when(jwtUtil.getNicknameFromToken("test-jwt-token")).thenReturn(nickname);
        when(roomService.findRandomPublicRoom()).thenReturn(randomRoom);
        when(roomService.getRoomInfo(roomId)).thenReturn(updatedRoomInfo);

        // when & then
        mockMvc.perform(post("/api/room/random")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(roomId))
                .andExpect(jsonPath("$.inviteCode").value("INVITE123"));

        verify(roomService).findRandomPublicRoom();
        verify(roomPlayerService).joinRoom(roomId, playerId, nickname);
    }

    @Test
    @Tag("api-room-random")
    @DisplayName("joinRandomRoom - 사용 가능한 방이 없으면 404를 반환해야 한다")
    void joinRandomRoom_noAvailableRooms_shouldReturn404() throws Exception {
        // given
        String token = "Bearer test-jwt-token";
        String playerId = "player1";
        String nickname = "테스터";

        when(jwtUtil.getPlayerIdFromToken("test-jwt-token")).thenReturn(playerId);
        when(jwtUtil.getNicknameFromToken("test-jwt-token")).thenReturn(nickname);
        when(roomService.findRandomPublicRoom()).thenReturn(null);

        // when & then
        mockMvc.perform(post("/api/room/random")
                        .header("Authorization", token))
                .andExpect(status().isNotFound());

        verify(roomService).findRandomPublicRoom();
    }

    @Test
    @Tag("api-room-invite")
    @DisplayName("joinByInviteCode - 정상: 초대코드로 방에 입장해야 한다")
    void joinByInviteCode_shouldJoinRoom() throws Exception {
        // given
        String token = "Bearer test-jwt-token";
        String playerId = "player1";
        String nickname = "테스터";
        String inviteCode = "INVITE123";
        String roomId = "room1";

        RoomInfo room = createRoomInfo(roomId);
        room.setInviteCode(inviteCode);
        RoomInfo updatedRoomInfo = createRoomInfo(roomId);

        when(jwtUtil.getPlayerIdFromToken("test-jwt-token")).thenReturn(playerId);
        when(jwtUtil.getNicknameFromToken("test-jwt-token")).thenReturn(nickname);
        when(roomService.findRoomByInviteCode(inviteCode)).thenReturn(room);
        when(roomService.getRoomInfo(roomId)).thenReturn(updatedRoomInfo);

        // when & then
        mockMvc.perform(post("/api/room/join-by-code")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inviteCode\":\"" + inviteCode + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(roomId))
                .andExpect(jsonPath("$.inviteCode").value(inviteCode));

        verify(roomService).findRoomByInviteCode(inviteCode);
        verify(roomPlayerService).joinRoom(roomId, playerId, nickname);
    }

    @Test
    @Tag("api-room-invite")
    @DisplayName("joinByInviteCode - 초대코드에 해당하는 방이 없으면 404를 반환해야 한다")
    void joinByInviteCode_roomNotFound_shouldReturn404() throws Exception {
        // given
        String token = "Bearer test-jwt-token";
        String playerId = "player1";
        String nickname = "테스터";
        String inviteCode = "INVALID";

        when(jwtUtil.getPlayerIdFromToken("test-jwt-token")).thenReturn(playerId);
        when(jwtUtil.getNicknameFromToken("test-jwt-token")).thenReturn(nickname);
        when(roomService.findRoomByInviteCode(inviteCode)).thenReturn(null);

        // when & then
        mockMvc.perform(post("/api/room/join-by-code")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inviteCode\":\"" + inviteCode + "\"}"))
                .andExpect(status().isNotFound());

        verify(roomService).findRoomByInviteCode(inviteCode);
    }

    // Helper methods

    private RoomInfo createRoomInfo(String roomId) {
        List<RoomPlayer> players = new ArrayList<>();
        players.add(RoomPlayer.builder()
                .playerId("host")
                .nickname("방장")
                .isHost(true)
                .role(PlayerRole.PLAYER)
                .build());

        GameSettings settings = new GameSettings();
        settings.setMaxPlayers(10);
        settings.setRounds(3);
        settings.setDrawTime(90);
        settings.setWordChoices(3);

        RoomInfo roomInfo = new RoomInfo();
        roomInfo.setRoomId(roomId);
        roomInfo.setInviteCode("INVITE123");
        roomInfo.setPlayers(players);
        roomInfo.setSettings(settings);
        roomInfo.setStatus(RoomStatus.WAITING);
        roomInfo.setPublic(true);

        return roomInfo;
    }
}
