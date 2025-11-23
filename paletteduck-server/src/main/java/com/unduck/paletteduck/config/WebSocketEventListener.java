package com.unduck.paletteduck.config;

import com.unduck.paletteduck.config.constants.WebSocketTopics;
import com.unduck.paletteduck.domain.chat.dto.ChatMessage;
import com.unduck.paletteduck.domain.chat.dto.ChatType;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.domain.room.dto.RoomPlayer;
import com.unduck.paletteduck.domain.room.service.RoomPlayerService;
import com.unduck.paletteduck.domain.room.service.RoomService;
import com.unduck.paletteduck.domain.room.service.SessionMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SessionMappingService sessionMappingService;
    private final RoomService roomService;
    private final RoomPlayerService roomPlayerService; // 추가
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        log.info("WebSocket CONNECT - sessionId: {}", sessionId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        log.info("WebSocket DISCONNECT - sessionId: {}", sessionId);

        String value = sessionMappingService.getPlayerAndRoom(sessionId);
        if (value != null) {
            String[] parts = value.split(":");
            String playerId = parts[0];
            String roomId = parts.length > 1 ? parts[1] : null;

            log.info("Player disconnected - playerId: {}, roomId: {}", playerId, roomId);

            String roomIdToLeave = sessionMappingService.removeSession(sessionId);
            if (roomIdToLeave != null) {
                log.info("Auto leaving room - playerId: {}, roomId: {}", playerId, roomIdToLeave);

                // 방 정보 가져오기
                RoomInfo roomInfo = roomService.getRoomInfo(roomIdToLeave);
                if (roomInfo != null) {
                    RoomPlayer leavingPlayer = roomInfo.getPlayers().stream()
                            .filter(p -> p.getPlayerId().equals(playerId))
                            .findFirst()
                            .orElse(null);

                    String leavingNickname = leavingPlayer != null ? leavingPlayer.getNickname() : "Unknown";

                    // 방 나가기 처리 - roomPlayerService 사용
                    RoomInfo updatedRoomInfo = roomPlayerService.leaveRoom(roomIdToLeave, playerId);

                    if (updatedRoomInfo != null) {
                        // WebSocket으로 방 정보 갱신 브로드캐스트
                        messagingTemplate.convertAndSend(WebSocketTopics.room(roomIdToLeave), updatedRoomInfo);

                        // 퇴장 메시지 브로드캐스트
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.setPlayerId("");
                        chatMessage.setNickname("");
                        chatMessage.setMessage(leavingNickname + "님이 방을 나갔습니다.");
                        chatMessage.setType(ChatType.SYSTEM);
                        chatMessage.setTimestamp(System.currentTimeMillis());

                        messagingTemplate.convertAndSend(WebSocketTopics.roomChat(roomIdToLeave), chatMessage);

                        log.info("Broadcasted leave message - roomId: {}, nickname: {}", roomIdToLeave, leavingNickname);
                    }
                }
            }
        }
    }
}
