package com.unduck.paletteduck.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomPlayer {
    private String playerId;
    private String nickname;
    private boolean isHost;
    private boolean isReady;
}