package com.unduck.paletteduck.domain.game.controller;

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
    private String roomId;
    private String token;
    private Map<String, Object> data;

    @PostMapping("/{roomId}/drawing")
    public ResponseEntity<Void> uploadDrawing(
            @PathVariable String roomId,
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> data) {
        this.roomId = roomId;
        this.token = token;
        this.data = data;

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
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/game/drawing", data);

        return ResponseEntity.ok().build();
    }
}
