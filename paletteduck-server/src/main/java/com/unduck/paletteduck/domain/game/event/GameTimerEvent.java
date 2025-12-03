package com.unduck.paletteduck.domain.game.event;

import lombok.Getter;

/**
 * 게임 타이머 이벤트 기본 클래스
 */
@Getter
public abstract class GameTimerEvent {
    private final String roomId;
    private final String gameSessionId;
    private final int turnNumber;

    protected GameTimerEvent(String roomId, String gameSessionId, int turnNumber) {
        this.roomId = roomId;
        this.gameSessionId = gameSessionId;
        this.turnNumber = turnNumber;
    }
}
