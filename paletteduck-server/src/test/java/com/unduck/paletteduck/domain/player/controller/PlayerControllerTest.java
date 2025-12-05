package com.unduck.paletteduck.domain.player.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unduck.paletteduck.domain.player.dto.PlayerJoinRequest;
import com.unduck.paletteduck.domain.player.dto.PlayerJoinResponse;
import com.unduck.paletteduck.domain.player.service.PlayerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("PlayerController 테스트")
@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlayerService playerService;

    @Test
    @Tag("api-player-join")
    @DisplayName("join - 정상: 플레이어 가입이 성공해야 한다")
    void join_validRequest_shouldReturnToken() throws Exception {
        // given
        PlayerJoinRequest request = PlayerJoinRequest.builder()
                .nickname("테스터")
                .build();

        PlayerJoinResponse response = PlayerJoinResponse.builder()
                .token("test-jwt-token")
                .playerId("player123")
                .nickname("테스터")
                .build();

        when(playerService.joinGame(any(PlayerJoinRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/player/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.playerId").value("player123"))
                .andExpect(jsonPath("$.nickname").value("테스터"));

        verify(playerService).joinGame(any(PlayerJoinRequest.class));
    }

    @Test
    @Tag("api-player-join")
    @DisplayName("join - 닉네임이 한글일 때 성공해야 한다")
    void join_koreanNickname_shouldSucceed() throws Exception {
        // given
        PlayerJoinRequest request = PlayerJoinRequest.builder()
                .nickname("홍길동")
                .build();

        PlayerJoinResponse response = PlayerJoinResponse.builder()
                .token("test-token")
                .playerId("player123")
                .nickname("홍길동")
                .build();

        when(playerService.joinGame(any(PlayerJoinRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/player/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("홍길동"));
    }

    @Test
    @Tag("api-player-join")
    @DisplayName("join - 닉네임이 영문일 때 성공해야 한다")
    void join_englishNickname_shouldSucceed() throws Exception {
        // given
        PlayerJoinRequest request = PlayerJoinRequest.builder()
                .nickname("Player")
                .build();

        PlayerJoinResponse response = PlayerJoinResponse.builder()
                .token("test-token")
                .playerId("player123")
                .nickname("Player")
                .build();

        when(playerService.joinGame(any(PlayerJoinRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/player/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("Player"));
    }

    @Test
    @Tag("api-player-join")
    @DisplayName("join - 닉네임이 숫자 포함일 때 성공해야 한다")
    void join_alphanumericNickname_shouldSucceed() throws Exception {
        // given
        PlayerJoinRequest request = PlayerJoinRequest.builder()
                .nickname("Player123")
                .build();

        PlayerJoinResponse response = PlayerJoinResponse.builder()
                .token("test-token")
                .playerId("player123")
                .nickname("Player123")
                .build();

        when(playerService.joinGame(any(PlayerJoinRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/player/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("Player123"));
    }

    @Test
    @Tag("api-player-join")
    @DisplayName("join - 2자 닉네임은 성공해야 한다")
    void join_twoCharNickname_shouldSucceed() throws Exception {
        // given
        PlayerJoinRequest request = PlayerJoinRequest.builder()
                .nickname("홍길")
                .build();

        PlayerJoinResponse response = PlayerJoinResponse.builder()
                .token("test-token")
                .playerId("player123")
                .nickname("홍길")
                .build();

        when(playerService.joinGame(any(PlayerJoinRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/player/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("홍길"));
    }

    @Test
    @Tag("api-player-join")
    @DisplayName("join - 10자 닉네임은 성공해야 한다")
    void join_tenCharNickname_shouldSucceed() throws Exception {
        // given
        PlayerJoinRequest request = PlayerJoinRequest.builder()
                .nickname("1234567890") // 10자
                .build();

        PlayerJoinResponse response = PlayerJoinResponse.builder()
                .token("test-token")
                .playerId("player123")
                .nickname("1234567890")
                .build();

        when(playerService.joinGame(any(PlayerJoinRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/player/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("1234567890"));
    }
}
