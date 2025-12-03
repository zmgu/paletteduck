package com.unduck.paletteduck.domain.game.event;

import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.dto.TurnEndReason;
import lombok.Getter;

/**
 * 턴 종료 이벤트
 */
@Getter
public class TurnEndEvent {
    private final String roomId;
    private final GameState gameState;
    private final TurnEndReason reason;

    public TurnEndEvent(String roomId, GameState gameState, TurnEndReason reason) {
        this.roomId = roomId;
        this.gameState = gameState;
        this.reason = reason;
    }
}
