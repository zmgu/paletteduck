package com.unduck.paletteduck.domain.room.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomPlayer {
    private String playerId;
    private String nickname;
    private boolean isHost;
    private boolean isReady;
    private PlayerRole role = PlayerRole.PLAYER;
    private int score = 0;
    private int totalLikes = 0;
    private int totalDislikes = 0;
    private Long joinedAt; // 방 입장 시간 (타임스탬프)
}