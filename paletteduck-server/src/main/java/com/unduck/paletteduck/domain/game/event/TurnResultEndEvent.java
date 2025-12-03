package com.unduck.paletteduck.domain.game.event;

import com.unduck.paletteduck.domain.game.dto.GameState;
import lombok.Getter;

/**
 * 턴 결과 표시 종료 이벤트
 */
@Getter
public class TurnResultEndEvent {
    private final String roomId;
    private final GameState gameState;

    public TurnResultEndEvent(String roomId, GameState gameState) {
        this.roomId = roomId;
        this.gameState = gameState;
    }
}
