package com.unduck.paletteduck.domain.game.controller;

import com.unduck.paletteduck.domain.game.dto.DrawData;
import com.unduck.paletteduck.domain.game.dto.GamePhase;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.service.GameService;
import com.unduck.paletteduck.domain.game.service.GameTimerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketGameController {

    private final GameService gameService;
    private final GameTimerService gameTimerService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/room/{roomId}/game/word/select")
    public void selectWord(@DestinationVariable String roomId,
                           @Payload Map<String, String> payload) {
        String playerId = payload.get("playerId");
        String word = payload.get("word");

        gameTimerService.selectWord(roomId, playerId, word);
        log.info("Word selection - room: {}, playerId: {}", roomId, playerId);
    }

    @MessageMapping("/room/{roomId}/game/draw")
    public void drawPath(@DestinationVariable String roomId, @Payload DrawData drawData) {
        GameState gameState = gameService.getGameState(roomId);
        if (gameState == null || gameState.getPhase() != GamePhase.DRAWING) {
            return;
        }

        if (!gameState.getCurrentTurn().getDrawerId().equals(drawData.getPlayerId())) {
            log.warn("Unauthorized draw attempt - playerId: {}", drawData.getPlayerId());
            return;
        }

        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/game/draw", drawData);
    }

    @MessageMapping("/room/{roomId}/game/clear")
    public void clearCanvas(@DestinationVariable String roomId, @Payload String playerId) {
        GameState gameState = gameService.getGameState(roomId);
        if (gameState == null || gameState.getPhase() != GamePhase.DRAWING) {
            return;
        }

        if (!gameState.getCurrentTurn().getDrawerId().equals(playerId)) {
            return;
        }

        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/game/clear", playerId);
    }
}