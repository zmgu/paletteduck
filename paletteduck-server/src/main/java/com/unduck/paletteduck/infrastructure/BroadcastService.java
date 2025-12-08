package com.unduck.paletteduck.infrastructure;

import com.unduck.paletteduck.config.constants.WebSocketTopics;
import com.unduck.paletteduck.domain.chat.dto.ChatMessage;
import com.unduck.paletteduck.domain.game.dto.DrawData;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastRoomInfo(String roomId, RoomInfo roomInfo) {
        messagingTemplate.convertAndSend(WebSocketTopics.room(roomId), roomInfo);
        log.debug("Broadcasted room info - roomId: {}", roomId);
    }

    public void broadcastChatMessage(String roomId, ChatMessage message) {
        messagingTemplate.convertAndSend(WebSocketTopics.roomChat(roomId), message);
        log.debug("Broadcasted chat message - roomId: {}, type: {}", roomId, message.getType());
    }

    public void broadcastGameState(String roomId, GameState gameState) {
        messagingTemplate.convertAndSend(WebSocketTopics.gameState(roomId), gameState);
        log.debug("Broadcasted game state - roomId: {}, phase: {}", roomId, gameState.getPhase());
    }

    public void broadcastGameStart(String roomId, GameState gameState) {
        messagingTemplate.convertAndSend(WebSocketTopics.gameStart(roomId), gameState);
        log.info("Broadcasted game start - roomId: {}, phase: {}", roomId, gameState.getPhase());
    }

    public void broadcastDrawData(String roomId, DrawData drawData) {
        messagingTemplate.convertAndSend(WebSocketTopics.gameDraw(roomId), drawData);
    }

    public void broadcastDrawingData(String roomId, Map<String, Object> data) {
        messagingTemplate.convertAndSend(WebSocketTopics.gameDrawing(roomId), data);
    }

    public void broadcastClearCanvas(String roomId, String playerId) {
        Map<String, String> clearMessage = Map.of("playerId", playerId);
        messagingTemplate.convertAndSend(WebSocketTopics.gameClear(roomId), clearMessage);
        log.debug("Broadcasted canvas clear - roomId: {}, playerId: {}", roomId, playerId);
    }
}
