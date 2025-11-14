package com.unduck.paletteduck.controller;

import com.unduck.paletteduck.dto.RoomCreateResponse;
import com.unduck.paletteduck.dto.RoomInfo;
import com.unduck.paletteduck.service.RoomService;
import com.unduck.paletteduck.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoomController {

    private final RoomService roomService;
    private final JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<RoomCreateResponse> createRoom(@RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        String playerId = jwtUtil.getPlayerIdFromToken(jwt);
        String nickname = jwtUtil.getNicknameFromToken(jwt);

        RoomCreateResponse response = roomService.createRoom(playerId, nickname);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomInfo> getRoomInfo(@PathVariable String roomId) {
        RoomInfo roomInfo = roomService.getRoomInfo(roomId);

        if (roomInfo == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(roomInfo);
    }

    @PostMapping("/{roomId}/join")
    public ResponseEntity<Void> joinRoom(
            @PathVariable String roomId,
            @RequestHeader("Authorization") String token
    ) {
        String jwt = token.replace("Bearer ", "");
        String playerId = jwtUtil.getPlayerIdFromToken(jwt);
        String nickname = jwtUtil.getNicknameFromToken(jwt);

        roomService.joinRoom(roomId, playerId, nickname);
        return ResponseEntity.ok().build();
    }
}