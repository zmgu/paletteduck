package com.unduck.paletteduck.domain.room.controller;

import com.unduck.paletteduck.config.constants.WebSocketTopics;
import com.unduck.paletteduck.domain.chat.dto.ChatMessage;
import com.unduck.paletteduck.domain.chat.dto.ChatType;
import com.unduck.paletteduck.domain.game.constants.GameConstants;
import com.unduck.paletteduck.domain.room.dto.RoomCreateRequest;
import com.unduck.paletteduck.domain.room.dto.RoomCreateResponse;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.domain.room.dto.RoomListResponse;
import com.unduck.paletteduck.domain.room.service.RoomPlayerService;
import com.unduck.paletteduck.domain.room.service.RoomService;
import com.unduck.paletteduck.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<RoomCreateResponse> createRoom(
            @RequestHeader("Authorization") String token,
            @RequestBody(required = false) RoomCreateRequest request) {
        String jwt = token.replace("Bearer ", "");
        String playerId = jwtUtil.getPlayerIdFromToken(jwt);
        String nickname = jwtUtil.getNicknameFromToken(jwt);

        log.info("Create room request received - request: {}, request.isPublic: {}",
                request, request != null ? request.isPublic() : "null");

        boolean isPublic = request != null ? request.isPublic() : true;

        log.info("Create room - playerId: {}, nickname: {}, isPublic: {}", playerId, nickname, isPublic);
        RoomCreateResponse response = roomService.createRoom(playerId, nickname, isPublic);
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
                    .playerId(GameConstants.SystemPlayer.ID)
                    .nickname(GameConstants.SystemPlayer.NAME)
                    .message(nickname + "님이 입장했습니다.")
                    .type(ChatType.SYSTEM)
                    .isCorrect(false)
                    .timestamp(System.currentTimeMillis())
                    .build();

            messagingTemplate.convertAndSend(WebSocketTopics.roomChat(roomId), joinMessage);

            // 방 정보 갱신 브로드캐스트
            RoomInfo updatedRoomInfo = roomService.getRoomInfo(roomId);
            messagingTemplate.convertAndSend(WebSocketTopics.room(roomId), updatedRoomInfo);
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

    @PostMapping("/random")
    public ResponseEntity<?> joinRandomRoom(@RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        String playerId = jwtUtil.getPlayerIdFromToken(jwt);
        String nickname = jwtUtil.getNicknameFromToken(jwt);

        log.info("Random room join request - playerId: {}, nickname: {}", playerId, nickname);

        // 입장 가능한 랜덤 공개방 찾기
        RoomInfo randomRoom = roomService.findRandomPublicRoom();

        if (randomRoom == null) {
            log.info("No available public rooms - playerId: {}", playerId);
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("사용 가능한 공개방이 없습니다."));
        }

        String roomId = randomRoom.getRoomId();
        log.info("Random room found - roomId: {}, playerId: {}", roomId, playerId);

        // 방 입장 처리 (기존 joinRoom 로직 재사용)
        boolean alreadyInRoom = randomRoom.getPlayers().stream()
                .anyMatch(p -> p.getPlayerId().equals(playerId));

        if (!alreadyInRoom) {
            roomPlayerService.joinRoom(roomId, playerId, nickname);

            // 입장 메시지 브로드캐스트
            ChatMessage joinMessage = ChatMessage.builder()
                    .messageId(java.util.UUID.randomUUID().toString())
                    .playerId(GameConstants.SystemPlayer.ID)
                    .nickname(GameConstants.SystemPlayer.NAME)
                    .message(nickname + "님이 입장했습니다.")
                    .type(ChatType.SYSTEM)
                    .isCorrect(false)
                    .timestamp(System.currentTimeMillis())
                    .build();

            messagingTemplate.convertAndSend(WebSocketTopics.roomChat(roomId), joinMessage);

            // 방 정보 갱신 브로드캐스트
            RoomInfo updatedRoomInfo = roomService.getRoomInfo(roomId);
            messagingTemplate.convertAndSend(WebSocketTopics.room(roomId), updatedRoomInfo);
        }

        log.info("Random room join successful - roomId: {}, playerId: {}", roomId, playerId);
        return ResponseEntity.ok(new RandomJoinResponse(roomId, randomRoom.getInviteCode()));
    }

    @GetMapping("/list")
    public ResponseEntity<List<RoomListResponse>> getRoomList() {
        log.info("Get public room list request");
        List<RoomListResponse> roomList = roomService.getPublicRoomList();
        log.info("Returning {} public rooms", roomList.size());
        return ResponseEntity.ok(roomList);
    }

    @PostMapping("/join-by-code")
    public ResponseEntity<?> joinByInviteCode(
            @RequestHeader("Authorization") String token,
            @RequestBody InviteCodeRequest request) {
        String jwt = token.replace("Bearer ", "");
        String playerId = jwtUtil.getPlayerIdFromToken(jwt);
        String nickname = jwtUtil.getNicknameFromToken(jwt);

        log.info("Join by invite code request - inviteCode: {}, playerId: {}, nickname: {}",
                request.inviteCode(), playerId, nickname);

        // 초대코드로 방 찾기
        RoomInfo room = roomService.findRoomByInviteCode(request.inviteCode());

        if (room == null) {
            log.info("Room not found for invite code: {}", request.inviteCode());
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("초대코드에 해당하는 방을 찾을 수 없습니다."));
        }

        String roomId = room.getRoomId();
        log.info("Room found by invite code - roomId: {}, inviteCode: {}", roomId, request.inviteCode());

        // 방 입장 처리
        boolean alreadyInRoom = room.getPlayers().stream()
                .anyMatch(p -> p.getPlayerId().equals(playerId));

        if (!alreadyInRoom) {
            roomPlayerService.joinRoom(roomId, playerId, nickname);

            // 입장 메시지 브로드캐스트
            ChatMessage joinMessage = ChatMessage.builder()
                    .messageId(java.util.UUID.randomUUID().toString())
                    .playerId(GameConstants.SystemPlayer.ID)
                    .nickname(GameConstants.SystemPlayer.NAME)
                    .message(nickname + "님이 입장했습니다.")
                    .type(ChatType.SYSTEM)
                    .isCorrect(false)
                    .timestamp(System.currentTimeMillis())
                    .build();

            messagingTemplate.convertAndSend(WebSocketTopics.roomChat(roomId), joinMessage);

            // 방 정보 갱신 브로드캐스트
            RoomInfo updatedRoomInfo = roomService.getRoomInfo(roomId);
            messagingTemplate.convertAndSend(WebSocketTopics.room(roomId), updatedRoomInfo);
        }

        log.info("Join by invite code successful - roomId: {}, playerId: {}", roomId, playerId);
        return ResponseEntity.ok(new JoinByCodeResponse(roomId, room.getInviteCode()));
    }

    // 에러 응답용 DTO
    private record ErrorResponse(String message) {}

    // 랜덤 입장 응답용 DTO
    private record RandomJoinResponse(String roomId, String inviteCode) {}

    // 초대코드 입력 요청용 DTO
    private record InviteCodeRequest(String inviteCode) {}

    // 초대코드 입장 응답용 DTO
    private record JoinByCodeResponse(String roomId, String inviteCode) {}
}