package com.unduck.paletteduck.domain.player.controller;

import com.unduck.paletteduck.domain.player.dto.PlayerJoinRequest;
import com.unduck.paletteduck.domain.player.dto.PlayerJoinResponse;
import com.unduck.paletteduck.domain.player.service.PlayerService;
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