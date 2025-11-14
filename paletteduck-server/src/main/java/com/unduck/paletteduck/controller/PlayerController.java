package com.unduck.paletteduck.controller;

import com.unduck.paletteduck.dto.PlayerJoinRequest;
import com.unduck.paletteduck.dto.PlayerJoinResponse;
import com.unduck.paletteduck.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping("/join")
    public ResponseEntity<PlayerJoinResponse> join(@RequestBody PlayerJoinRequest request) {
        return ResponseEntity.ok(playerService.joinGame(request));
    }
}