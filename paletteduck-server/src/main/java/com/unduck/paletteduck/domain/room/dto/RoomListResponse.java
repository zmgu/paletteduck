package com.unduck.paletteduck.domain.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomListResponse {
    private String roomId;
    private String inviteCode;
    private RoomStatus status;
    private int currentPlayers;  // 참가자 수 (관전자 제외)
    private int maxPlayers;
    private String hostNickname;
    private Integer currentRound;  // PLAYING일 때만 (nullable)
    private Integer totalRounds;   // PLAYING일 때만 (nullable)
    private Long createdAt;        // 방 생성 시간 (정렬용)
}
