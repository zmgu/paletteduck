package com.unduck.paletteduck.domain.player.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PlayerJoinResponse {
    private String token;
    private String playerId;
    private String nickname;
}
