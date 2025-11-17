package com.unduck.paletteduck.domain.player.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerJoinResponse {
    private String token;
    private String playerId;
    private String nickname;
}
