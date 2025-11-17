package com.unduck.paletteduck.domain.room.controller;

import com.unduck.paletteduck.domain.room.dto.RoomCreateResponse;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.domain.room.service.RoomGameService;
import com.unduck.paletteduck.domain.room.service.RoomPlayerService;
import com.unduck.paletteduck.domain.room.service.RoomService;
import com.unduck.paletteduck.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class RoomController {

    private final RoomService roomService;
    private final RoomPlayerService roomPlayerService;
    private final RoomGameService roomGameService;
    private final JwtUtil jwtUtil;

    /**
     * 방 생성
     */
    @PostMapping("/create")
    public ResponseEntity<RoomCreateResponse> createRoom(@RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        String playerId = jwtUtil.getPlayerIdFromToken(jwt);
        String nickname = jwtUtil.getNicknameFromToken(jwt);

        log.info("Create room request - playerId: {}, nickname: {}", playerId, nickname);
        RoomCreateResponse response = roomService.createRoom(playerId, nickname);
        return ResponseEntity.ok(response);
    }

    /**
     * 방 정보 조회
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomInfo> getRoomInfo(@PathVariable String roomId) {
        log.info("Get room info - roomId: {}", roomId);
        RoomInfo roomInfo = roomService.getRoomInfo(roomId);

        if (roomInfo == null) {
            log.warn("Room not found - roomId: {}", roomId);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(roomInfo);
    }

    /**
     * 방 입장
     */
    @PostMapping("/{roomId}/join")
    public ResponseEntity<Void> joinRoom(
            @PathVariable String roomId,
            @RequestHeader("Authorization") String token
    ) {
        String jwt = token.replace("Bearer ", "");
        String playerId = jwtUtil.getPlayerIdFromToken(jwt);
        String nickname = jwtUtil.getNicknameFromToken(jwt);

        log.info("Join room request - roomId: {}, playerId: {}, nickname: {}", roomId, playerId, nickname);

        try {
            roomPlayerService.joinRoom(roomId, playerId, nickname);
            log.info("Join room successful");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Join room failed", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 방 나가기
     */
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(
            @PathVariable String roomId,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        String playerId = null;

        if (token != null && !token.isEmpty()) {
            String jwt = token.replace("Bearer ", "");
            playerId = jwtUtil.getPlayerIdFromToken(jwt);
        }

        log.info("Leave room request - roomId: {}, playerId: {}", roomId, playerId);

        if (playerId != null) {
            roomPlayerService.leaveRoom(roomId, playerId);
            log.info("Leave room successful");
        } else {
            log.warn("Leave room - no playerId provided");
        }

        return ResponseEntity.ok().build();
    }

    /**
     * sendBeacon용 방 나가기
     */
    @PostMapping("/{roomId}/leave-beacon")
    public ResponseEntity<Void> leaveRoomBeacon(
            @PathVariable String roomId,
            @RequestBody Map<String, String> body
    ) {
        String playerId = body.get("playerId");
        log.info("Leave room beacon - roomId: {}, playerId: {}", roomId, playerId);

        if (playerId != null) {
            roomPlayerService.leaveRoom(roomId, playerId);
            log.info("Leave room beacon successful");
        } else {
            log.warn("Leave room beacon - no playerId provided");
        }

        return ResponseEntity.ok().build();
    }
}