package com.unduck.paletteduck.domain.game.event;

import com.unduck.paletteduck.domain.game.dto.GameState;
import lombok.Getter;

/**
 * 카운트다운 완료 이벤트
 */
@Getter
public class CountdownCompletedEvent {
    private final String roomId;
    private final GameState gameState;

    public CountdownCompletedEvent(String roomId, GameState gameState) {
        this.roomId = roomId;
        this.gameState = gameState;
    }
}
