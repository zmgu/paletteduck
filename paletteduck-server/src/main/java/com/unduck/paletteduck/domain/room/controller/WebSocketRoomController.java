package com.unduck.paletteduck.domain.room.controller;

import com.unduck.paletteduck.domain.chat.dto.RoleChangeMessage;
import com.unduck.paletteduck.domain.room.dto.*;
import com.unduck.paletteduck.domain.room.service.RoomService;
import com.unduck.paletteduck.domain.chat.dto.ChatMessage;
import com.unduck.paletteduck.domain.room.service.SessionMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketRoomController {

    private final RoomService roomService;
    private final SessionMappingService sessionMappingService;

    @MessageMapping("/room/{roomId}/register")
    public void registerSession(@DestinationVariable String roomId,
                                @Payload String playerId,
                                @Header("simpSessionId") String sessionId) {
        log.info("Register session - roomId: {}, playerId: {}, sessionId: {}", roomId, playerId, sessionId);
        sessionMappingService.addSession(sessionId, playerId, roomId);
    }

    @MessageMapping("/room/{roomId}/update")
    @SendTo("/topic/room/{roomId}")
    public RoomInfo updateRoom(@DestinationVariable String roomId) {
        return roomService.getRoomInfo(roomId);
    }

    @MessageMapping("/room/{roomId}/ready")
    @SendTo("/topic/room/{roomId}")
    public RoomInfo toggleReady(@DestinationVariable String roomId, @Payload String playerId) {
        roomService.toggleReady(roomId, playerId);
        return roomService.getRoomInfo(roomId);
    }

    @MessageMapping("/room/{roomId}/role")
    @SendTo("/topic/room/{roomId}")
    public RoomInfo changeRole(@DestinationVariable String roomId, @Payload RoleChangeMessage message) {
        roomService.changeRole(roomId, message.getPlayerId(), message.getNewRole());
        return roomService.getRoomInfo(roomId);
    }

    @MessageMapping("/room/{roomId}/settings")
    @SendTo("/topic/room/{roomId}")
    public RoomInfo updateSettings(@DestinationVariable String roomId, @Payload SettingsUpdateMessage message) {
        roomService.updateSettings(roomId, message.getPlayerId(), message.getSettings());
        return roomService.getRoomInfo(roomId);
    }

    @MessageMapping("/room/{roomId}/start")
    @SendTo("/topic/room/{roomId}/start")
    public RoomInfo startGame(@DestinationVariable String roomId, @Payload String playerId) {
        log.info("Start game request - roomId: {}, playerId: {}", roomId, playerId);
        roomService.startGame(roomId, playerId);
        return roomService.getRoomInfo(roomId);
    }

    @MessageMapping("/room/{roomId}/chat")
    @SendTo("/topic/room/{roomId}/chat")
    public ChatMessage sendChat(@DestinationVariable String roomId, @Payload ChatMessage message) {
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
}