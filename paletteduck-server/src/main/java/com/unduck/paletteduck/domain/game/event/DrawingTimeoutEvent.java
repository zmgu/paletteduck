package com.unduck.paletteduck.domain.game.event;

import com.unduck.paletteduck.domain.game.dto.GameState;
import lombok.Getter;

/**
 * 그리기 시간 초과 이벤트
 */
@Getter
public class DrawingTimeoutEvent extends GameTimerEvent {
    private final GameState gameState;

    public DrawingTimeoutEvent(String roomId, String gameSessionId, int turnNumber, GameState gameState) {
        super(roomId, gameSessionId, turnNumber);
        this.gameState = gameState;
    }
}
