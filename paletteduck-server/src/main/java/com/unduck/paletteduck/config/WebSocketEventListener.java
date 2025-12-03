package com.unduck.paletteduck.config;

import com.unduck.paletteduck.config.constants.WebSocketTopics;
import com.unduck.paletteduck.domain.chat.dto.ChatMessage;
import com.unduck.paletteduck.domain.chat.dto.ChatType;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.dto.TurnEndReason;
import com.unduck.paletteduck.domain.game.service.GameService;
import com.unduck.paletteduck.domain.room.dto.PlayerRole;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.domain.room.dto.RoomPlayer;
import com.unduck.paletteduck.domain.room.dto.RoomStatus;
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
    private final RoomPlayerService roomPlayerService;
    private final GameService gameService;
    private final com.unduck.paletteduck.domain.game.service.TurnManager turnManager;
    private final com.unduck.paletteduck.domain.game.service.GamePhaseManager phaseManager;
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
                    PlayerRole leavingRole = leavingPlayer != null ? leavingPlayer.getRole() : null;

                    log.info("Player leaving - playerId: {}, nickname: {}, role: {}, roomStatus: {}",
                            playerId, leavingNickname, leavingRole, roomInfo.getStatus());

                    // 방 나가기 처리 - roomPlayerService 사용
                    RoomInfo updatedRoomInfo = roomPlayerService.leaveRoom(roomIdToLeave, playerId);

                    if (updatedRoomInfo != null) {
                        // WebSocket으로 방 정보 갱신 브로드캐스트
                        messagingTemplate.convertAndSend(WebSocketTopics.room(roomIdToLeave), updatedRoomInfo);

                        // 퇴장 메시지 브로드캐스트 (관전자 포함 모든 플레이어)
                        ChatMessage chatMessage = ChatMessage.builder()
                                .playerId("")
                                .nickname("")
                                .message(leavingNickname + "님이 방을 나갔습니다.")
                                .type(ChatType.SYSTEM)
                                .timestamp(System.currentTimeMillis())
                                .build();

                        messagingTemplate.convertAndSend(WebSocketTopics.roomChat(roomIdToLeave), chatMessage);

                        log.info("Broadcasted leave message - roomId: {}, nickname: {}, role: {}, roomStatus: {}",
                                roomIdToLeave, leavingNickname, leavingRole, updatedRoomInfo.getStatus());

                        // 게임 중인 경우 GameState 업데이트
                        if (updatedRoomInfo.getStatus() == RoomStatus.PLAYING) {
                            GameState gameState = gameService.getGameState(roomIdToLeave);
                            if (gameState != null) {
                                // 퇴장한 플레이어가 참가자인 경우에만 처리
                                if (leavingPlayer != null && leavingPlayer.getRole() == PlayerRole.PLAYER) {
                                    // GameState의 players 리스트에서 제거
                                    boolean playerRemoved = gameState.getPlayers() != null &&
                                        gameState.getPlayers().removeIf(p -> p.getPlayerId().equals(playerId));

                                    // turnOrder에서 제거
                                    boolean turnOrderRemoved = gameState.getTurnOrder() != null &&
                                        gameState.getTurnOrder().remove(playerId);

                                    if (playerRemoved || turnOrderRemoved) {
                                        log.info("Removed player from GameState - playerId: {}, nickname: {}", playerId, leavingNickname);

                                        // 현재 출제자가 퇴장한 경우 턴 종료
                                        if (gameState.getCurrentTurn() != null &&
                                            gameState.getCurrentTurn().getDrawerId().equals(playerId)) {
                                            log.info("Current drawer left - ending turn immediately. RoomId: {}", roomIdToLeave);
                                            turnManager.endTurn(roomIdToLeave, gameState, TurnEndReason.DRAWER_LEFT);
                                        } else {
                                            // GameState 저장 및 브로드캐스트
                                            gameService.updateGameState(roomIdToLeave, gameState);
                                            messagingTemplate.convertAndSend(WebSocketTopics.gameState(roomIdToLeave), gameState);
                                        }
                                    }
                                }

                                // 남은 플레이어 수 확인
                                long remainingPlayers = updatedRoomInfo.getPlayers().stream()
                                        .filter(p -> p.getRole() == PlayerRole.PLAYER)
                                        .count();

                                // 플레이어가 1명 이하만 남았다면 게임 종료
                                if (remainingPlayers <= 1) {
                                    log.info("Only {} player(s) remaining - ending game. RoomId: {}", remainingPlayers, roomIdToLeave);
                                    phaseManager.endGame(roomIdToLeave, gameState);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
