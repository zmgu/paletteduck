package com.unduck.paletteduck.domain.room.controller;

import com.unduck.paletteduck.config.constants.WebSocketTopics;
import com.unduck.paletteduck.domain.chat.dto.ChatMessage;
import com.unduck.paletteduck.domain.chat.dto.RoleChangeMessage;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.room.dto.*;
import com.unduck.paletteduck.domain.room.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketRoomController {

    private final RoomService roomService;
    private final RoomPlayerService roomPlayerService;
    private final RoomGameService roomGameService;
    private final SessionMappingService sessionMappingService;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.unduck.paletteduck.domain.game.service.GameService gameService;

    @MessageMapping("/room/{roomId}/register")
    public void registerSession(@DestinationVariable String roomId,
                                @Payload String playerId,
                                SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        sessionMappingService.addSession(sessionId, playerId, roomId);
        log.debug("Session registered - roomId: {}, playerId: {}, sessionId: {}",
                roomId, playerId, sessionId);

        // 게임 진행 중이면 현재 GameState를 도중 참가자에게 전송
        RoomInfo roomInfo = roomService.getRoomInfo(roomId);
        if (roomInfo != null && roomInfo.getStatus() == RoomStatus.PLAYING) {
            GameState gameState = gameService.getGameState(roomId);
            if (gameState != null) {
                // 모든 구독자에게 브로드캐스트 (도중 참가자가 최신 상태를 받도록)
                messagingTemplate.convertAndSend(
                    WebSocketTopics.gameState(roomId),
                    gameState
                );
                log.info("Sent current GameState to mid-game joiner - roomId: {}, playerId: {}, turn: {}, drawingEvents: {}",
                    roomId, playerId,
                    gameState.getCurrentTurn() != null ? gameState.getCurrentTurn().getTurnNumber() : "N/A",
                    gameState.getCurrentTurn() != null && gameState.getCurrentTurn().getDrawingEvents() != null ?
                        gameState.getCurrentTurn().getDrawingEvents().size() : 0);
            }
        }
    }

    @MessageMapping("/room/{roomId}/update")
    public void updateRoomInfo(@DestinationVariable String roomId) {
        RoomInfo roomInfo = roomService.getRoomInfo(roomId);
        if (roomInfo != null) {
            messagingTemplate.convertAndSend(WebSocketTopics.room(roomId), roomInfo);
        }
    }

    @MessageMapping("/room/{roomId}/ready")
    public void toggleReady(@DestinationVariable String roomId, @Payload String playerId) {
        roomPlayerService.toggleReady(roomId, playerId);
        RoomInfo roomInfo = roomService.getRoomInfo(roomId);
        messagingTemplate.convertAndSend(WebSocketTopics.room(roomId), roomInfo);
    }

    @MessageMapping("/room/{roomId}/role")
    public void changeRole(@DestinationVariable String roomId,
                           @Payload RoleChangeMessage message) {
        roomPlayerService.changeRole(roomId, message.getPlayerId(), message.getNewRole());
        RoomInfo roomInfo = roomService.getRoomInfo(roomId);
        messagingTemplate.convertAndSend(WebSocketTopics.room(roomId), roomInfo);
    }

    @MessageMapping("/room/{roomId}/settings")
    public void updateSettings(@DestinationVariable String roomId,
                               @Payload SettingsUpdateMessage message) {
        roomGameService.updateSettings(roomId, message.getPlayerId(), message.getSettings());
        RoomInfo roomInfo = roomService.getRoomInfo(roomId);
        messagingTemplate.convertAndSend(WebSocketTopics.room(roomId), roomInfo);
    }

    @MessageMapping("/room/{roomId}/start")
    public void startGame(@DestinationVariable String roomId, @Payload String playerId) {
        GameState gameState = roomGameService.startGame(roomId, playerId);

        RoomInfo roomInfo = roomService.getRoomInfo(roomId);
        messagingTemplate.convertAndSend(WebSocketTopics.room(roomId), roomInfo);
        messagingTemplate.convertAndSend(WebSocketTopics.gameStart(roomId), gameState);

        log.info("Game start broadcast - room: {}, phase: {}", roomId, gameState.getPhase());
    }

    @MessageMapping("/room/{roomId}/chat")
    public void sendChat(@DestinationVariable String roomId, @Payload ChatMessage message) {
        ChatMessage timestampedMessage = ChatMessage.builder()
                .messageId(message.getMessageId())
                .playerId(message.getPlayerId())
                .nickname(message.getNickname())
                .message(message.getMessage())
                .type(message.getType())
                .isCorrect(message.getIsCorrect())
                .senderIsCorrect(message.getSenderIsCorrect())
                .timestamp(System.currentTimeMillis())
                .build();
        messagingTemplate.convertAndSend(WebSocketTopics.roomChat(roomId), timestampedMessage);
    }

    @MessageMapping("/room/{roomId}/return-to-waiting")
    public void returnToWaitingRoom(@DestinationVariable String roomId, @Payload String playerId) {
        try {
            RoomInfo roomInfo = roomGameService.handlePlayerReturnToWaiting(roomId, playerId);
            // handlePlayerReturnToWaiting()에서 이미 브로드캐스트하므로 여기서는 생략

            log.info("Player returned to waiting - room: {}, player: {}", roomId, playerId);
        } catch (IllegalStateException e) {
            // 예외 발생 시 방 전체에 에러 메시지 브로드캐스트 (클라이언트에서 필터링)
            log.warn("Failed to return to waiting - room: {}, player: {}, error: {}", roomId, playerId, e.getMessage());

            // 에러 메시지를 방 전체에 브로드캐스트 (targetPlayerId로 대상 지정)
            ErrorMessage errorMessage = new ErrorMessage("RETURN_TO_WAITING_FAILED", e.getMessage(), playerId);
            messagingTemplate.convertAndSend(
                WebSocketTopics.roomErrors(roomId),
                errorMessage
            );

            log.info("Error message sent to room - roomId: {}, targetPlayerId: {}", roomId, playerId);
        }
    }
}