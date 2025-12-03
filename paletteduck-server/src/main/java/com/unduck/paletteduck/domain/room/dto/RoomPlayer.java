package com.unduck.paletteduck.domain.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomPlayer {
    private String playerId;
    private String nickname;
    private boolean isHost;
    private boolean isReady;
    @Builder.Default
    private PlayerRole role = PlayerRole.PLAYER;
    @Builder.Default
    private int score = 0;
    @Builder.Default
    private int totalLikes = 0;
    @Builder.Default
    private int totalDislikes = 0;
    private Long joinedAt; // 방 입장 시간 (타임스탬프)
}