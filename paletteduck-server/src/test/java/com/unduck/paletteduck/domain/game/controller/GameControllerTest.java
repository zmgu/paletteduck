package com.unduck.paletteduck.domain.game.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unduck.paletteduck.domain.game.dto.GamePhase;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.dto.TurnInfo;
import com.unduck.paletteduck.domain.game.service.GameService;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("GameController 테스트")
@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GameService gameService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @Tag("api-game-state")
    @DisplayName("getGameState - 정상: 게임 상태를 반환해야 한다")
    void getGameState_shouldReturnGameState() throws Exception {
        // given
        String roomId = "room1";
        GameState gameState = createGameState(roomId);

        when(gameService.getGameState(roomId)).thenReturn(gameState);

        // when & then
        mockMvc.perform(get("/api/game/{roomId}/state", roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(roomId))
                .andExpect(jsonPath("$.phase").value("DRAWING"))
                .andExpect(jsonPath("$.currentRound").value(1))
                .andExpect(jsonPath("$.totalRounds").value(3));

        verify(gameService).getGameState(roomId);
    }

    @Test
    @Tag("api-game-state")
    @DisplayName("getGameState - 게임 상태가 없으면 404를 반환해야 한다")
    void getGameState_notFound_shouldReturn404() throws Exception {
        // given
        String roomId = "nonexistent";
        when(gameService.getGameState(roomId)).thenReturn(null);

        // when & then
        mockMvc.perform(get("/api/game/{roomId}/state", roomId))
                .andExpect(status().isNotFound());

        verify(gameService).getGameState(roomId);
    }

    @Test
    @Tag("api-game-drawing")
    @DisplayName("uploadDrawing - 정상: 출제자의 그림 업로드가 성공해야 한다")
    void uploadDrawing_validDrawer_shouldSucceed() throws Exception {
        // given
        String roomId = "room1";
        String playerId = "player1";
        String token = "Bearer test-jwt-token";

        GameState gameState = createGameState(roomId);
        gameState.getCurrentTurn().setDrawerId(playerId);

        Map<String, Object> drawData = new HashMap<>();
        drawData.put("x", 100);
        drawData.put("y", 200);

        when(jwtUtil.getPlayerIdFromToken("test-jwt-token")).thenReturn(playerId);
        when(gameService.getGameState(roomId)).thenReturn(gameState);

        // when & then
        mockMvc.perform(post("/api/game/{roomId}/drawing", roomId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(drawData)))
                .andExpect(status().isOk());

        verify(jwtUtil).getPlayerIdFromToken("test-jwt-token");
        verify(gameService).getGameState(roomId);
        verify(messagingTemplate).convertAndSend(anyString(), any(Map.class));
    }

    @Test
    @Tag("api-game-drawing")
    @DisplayName("uploadDrawing - 게임 상태가 없으면 400을 반환해야 한다")
    void uploadDrawing_noGameState_shouldReturn400() throws Exception {
        // given
        String roomId = "room1";
        String token = "Bearer test-jwt-token";
        Map<String, Object> drawData = new HashMap<>();

        when(jwtUtil.getPlayerIdFromToken("test-jwt-token")).thenReturn("player1");
        when(gameService.getGameState(roomId)).thenReturn(null);

        // when & then
        mockMvc.perform(post("/api/game/{roomId}/drawing", roomId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(drawData)))
                .andExpect(status().isBadRequest());

        verify(jwtUtil).getPlayerIdFromToken("test-jwt-token");
        verify(gameService).getGameState(roomId);
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @Tag("api-game-drawing")
    @DisplayName("uploadDrawing - DRAWING 페이즈가 아니면 400을 반환해야 한다")
    void uploadDrawing_wrongPhase_shouldReturn400() throws Exception {
        // given
        String roomId = "room1";
        String token = "Bearer test-jwt-token";
        Map<String, Object> drawData = new HashMap<>();

        GameState gameState = createGameState(roomId);
        gameState.setPhase(GamePhase.WORD_SELECT);

        when(jwtUtil.getPlayerIdFromToken("test-jwt-token")).thenReturn("player1");
        when(gameService.getGameState(roomId)).thenReturn(gameState);

        // when & then
        mockMvc.perform(post("/api/game/{roomId}/drawing", roomId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(drawData)))
                .andExpect(status().isBadRequest());

        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @Tag("api-game-drawing")
    @DisplayName("uploadDrawing - 출제자가 아닌 플레이어는 403을 반환해야 한다")
    void uploadDrawing_notDrawer_shouldReturn403() throws Exception {
        // given
        String roomId = "room1";
        String playerId = "player2"; // 출제자가 아님
        String token = "Bearer test-jwt-token";

        GameState gameState = createGameState(roomId);
        gameState.getCurrentTurn().setDrawerId("player1"); // 다른 플레이어가 출제자

        Map<String, Object> drawData = new HashMap<>();

        when(jwtUtil.getPlayerIdFromToken("test-jwt-token")).thenReturn(playerId);
        when(gameService.getGameState(roomId)).thenReturn(gameState);

        // when & then
        mockMvc.perform(post("/api/game/{roomId}/drawing", roomId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(drawData)))
                .andExpect(status().isForbidden());

        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @Tag("api-game-drawing")
    @DisplayName("uploadDrawing - playerId가 drawData에 추가되어야 한다")
    void uploadDrawing_shouldAddPlayerId() throws Exception {
        // given
        String roomId = "room1";
        String playerId = "player1";
        String token = "Bearer test-jwt-token";

        GameState gameState = createGameState(roomId);
        gameState.getCurrentTurn().setDrawerId(playerId);

        Map<String, Object> drawData = new HashMap<>();
        drawData.put("x", 100);

        when(jwtUtil.getPlayerIdFromToken("test-jwt-token")).thenReturn(playerId);
        when(gameService.getGameState(roomId)).thenReturn(gameState);

        // when & then
        mockMvc.perform(post("/api/game/{roomId}/drawing", roomId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(drawData)))
                .andExpect(status().isOk());

        // playerId가 추가된 데이터가 전송되는지 확인
        verify(messagingTemplate).convertAndSend(anyString(), argThat((Object data) -> {
            if (data instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) data;
                return playerId.equals(map.get("playerId"));
            }
            return false;
        }));
    }

    // Helper methods

    private GameState createGameState(String roomId) {
        GameState gameState = new GameState(roomId, 3, 90, Arrays.asList("player1", "player2", "player3"));
        gameState.setPhase(GamePhase.DRAWING);
        gameState.setCurrentRound(1);

        TurnInfo turnInfo = new TurnInfo(1, "player1", "플레이어1");
        turnInfo.setWord("사과");
        gameState.setCurrentTurn(turnInfo);

        return gameState;
    }
}
