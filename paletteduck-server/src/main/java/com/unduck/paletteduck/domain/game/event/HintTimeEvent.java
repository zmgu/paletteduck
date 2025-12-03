package com.unduck.paletteduck.domain.game.event;

import com.unduck.paletteduck.domain.game.dto.GameState;
import lombok.Getter;

/**
 * 힌트 제공 시간 도달 이벤트
 */
@Getter
public class HintTimeEvent extends GameTimerEvent {
    private final GameState gameState;
    private final int hintLevel;

    public HintTimeEvent(String roomId, String gameSessionId, int turnNumber, GameState gameState, int hintLevel) {
        super(roomId, gameSessionId, turnNumber);
        this.gameState = gameState;
        this.hintLevel = hintLevel;
    }
}
