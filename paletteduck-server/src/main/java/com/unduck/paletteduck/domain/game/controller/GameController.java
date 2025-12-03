package com.unduck.paletteduck.domain.game.controller;

import com.unduck.paletteduck.config.constants.WebSocketTopics;
import com.unduck.paletteduck.domain.game.dto.GamePhase;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.service.GameService;
import com.unduck.paletteduck.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;
    private final JwtUtil jwtUtil;

    /**
     * 게임 상태 조회 (게임 진행 중인 방에 입장 시 사용)
     */
    @GetMapping("/{roomId}/state")
    public ResponseEntity<GameState> getGameState(@PathVariable String roomId) {
        GameState gameState = gameService.getGameState(roomId);
        if (gameState == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(gameState);
    }

    @PostMapping("/{roomId}/drawing")
    public ResponseEntity<Void> uploadDrawing(
            @PathVariable String roomId,
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> data) {
        String jwt = token.replace("Bearer ", "");
        String playerId = jwtUtil.getPlayerIdFromToken(jwt);

        GameState gameState = gameService.getGameState(roomId);
        if (gameState == null || gameState.getPhase() != GamePhase.DRAWING) {
            return ResponseEntity.badRequest().build();
        }

        if (!gameState.getCurrentTurn().getDrawerId().equals(playerId)) {
            log.warn("Unauthorized drawing upload - playerId: {}", playerId);
            return ResponseEntity.status(403).build();
        }

        data.put("playerId", playerId);
        messagingTemplate.convertAndSend(WebSocketTopics.gameDrawing(roomId), data);

        return ResponseEntity.ok().build();
    }
}
