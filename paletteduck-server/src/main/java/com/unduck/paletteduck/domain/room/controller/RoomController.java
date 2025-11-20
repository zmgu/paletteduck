package com.unduck.paletteduck.domain.room.controller;

import com.unduck.paletteduck.domain.chat.dto.ChatMessage;
import com.unduck.paletteduck.domain.chat.dto.ChatType;
import com.unduck.paletteduck.domain.room.dto.RoomCreateResponse;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.domain.room.service.RoomPlayerService;
import com.unduck.paletteduck.domain.room.service.RoomService;
import com.unduck.paletteduck.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final RoomPlayerService roomPlayerService;
    private final JwtUtil jwtUtil;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/create")
    public ResponseEntity<RoomCreateResponse> createRoom(@RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        String playerId = jwtUtil.getPlayerIdFromToken(jwt);
        String nickname = jwtUtil.getNicknameFromToken(jwt);

        log.info("Create room request - playerId: {}, nickname: {}", playerId, nickname);
        RoomCreateResponse response = roomService.createRoom(playerId, nickname);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomInfo> getRoomInfo(@PathVariable String roomId) {
        log.debug("Get room info - roomId: {}", roomId);
        RoomInfo roomInfo = roomService.getRoomInfo(roomId);
        if (roomInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(roomInfo);
    }

    @PostMapping("/{roomId}/join")
    public ResponseEntity<Void> joinRoom(
            @PathVariable String roomId,
            @RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        String playerId = jwtUtil.getPlayerIdFromToken(jwt);
        String nickname = jwtUtil.getNicknameFromToken(jwt);

        log.info("Join room request - roomId: {}, playerId: {}, nickname: {}", roomId, playerId, nickname);

        // 이미 방에 있는지 체크
        RoomInfo roomInfo = roomService.getRoomInfo(roomId);
        boolean alreadyInRoom = roomInfo != null &&
                roomInfo.getPlayers().stream()
                        .anyMatch(p -> p.getPlayerId().equals(playerId));

        if (!alreadyInRoom) {
            roomPlayerService.joinRoom(roomId, playerId, nickname);

            // 입장 메시지 브로드캐스트
            ChatMessage joinMessage = ChatMessage.builder()
                    .messageId(java.util.UUID.randomUUID().toString())
                    .playerId("system")
                    .nickname("System")
                    .message(nickname + "님이 입장했습니다.")
                    .type(ChatType.SYSTEM)
                    .isCorrect(false)
                    .timestamp(System.currentTimeMillis())
                    .build();

            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/chat", joinMessage);

            // 방 정보 갱신 브로드캐스트
            RoomInfo updatedRoomInfo = roomService.getRoomInfo(roomId);
            messagingTemplate.convertAndSend("/topic/room/" + roomId, updatedRoomInfo);
        }

        log.info("Join room successful");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(
            @PathVariable String roomId,
            @RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        String playerId = jwtUtil.getPlayerIdFromToken(jwt);

        log.info("Leave room request - roomId: {}, playerId: {}", roomId, playerId);
        roomPlayerService.leaveRoom(roomId, playerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomId}/leave-beacon")
    public ResponseEntity<Void> leaveRoomBeacon(
            @PathVariable String roomId,
            @RequestParam String playerId) {
        log.info("Leave room beacon - roomId: {}, playerId: {}", roomId, playerId);
        roomPlayerService.leaveRoom(roomId, playerId);
        return ResponseEntity.ok().build();
    }
}