package com.unduck.paletteduck.domain.room.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * 게임 종료 후 대기방 복귀를 추적하는 DTO
 * Redis에 저장되어 90초 타이머 및 복귀 상태를 관리
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnToWaitingTracker {
    private String roomId;
    private Long gameEndTime; // 게임 종료 시간 (90초 타이머 시작 시간)
    private String originalHostId; // 게임 종료 시점의 방장 ID
    private Set<String> returnedPlayerIds = new HashSet<>(); // 이미 복귀한 플레이어 ID 목록
    private boolean anyoneReturned = false; // 한 명이라도 수동 복귀했는지 여부

    public ReturnToWaitingTracker(String roomId, String originalHostId) {
        this.roomId = roomId;
        this.gameEndTime = System.currentTimeMillis();
        this.originalHostId = originalHostId;
        this.returnedPlayerIds = new HashSet<>();
        this.anyoneReturned = false;
    }

    public void addReturnedPlayer(String playerId) {
        this.returnedPlayerIds.add(playerId);
        this.anyoneReturned = true;
    }

    public boolean hasPlayerReturned(String playerId) {
        return returnedPlayerIds.contains(playerId);
    }

    public long getTimeElapsed() {
        return System.currentTimeMillis() - gameEndTime;
    }

    public long getTimeRemaining() {
        long elapsed = getTimeElapsed();
        long autoReturnDelay = 20000; // 20초 (테스트용)
        return Math.max(0, autoReturnDelay - elapsed);
    }
}
