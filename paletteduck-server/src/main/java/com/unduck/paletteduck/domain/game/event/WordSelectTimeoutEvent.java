package com.unduck.paletteduck.domain.game.event;

import com.unduck.paletteduck.domain.game.dto.GameState;
import lombok.Getter;

/**
 * 단어 선택 시간 초과 이벤트
 */
@Getter
public class WordSelectTimeoutEvent extends GameTimerEvent {
    private final GameState gameState;

    public WordSelectTimeoutEvent(String roomId, String gameSessionId, int turnNumber, GameState gameState) {
        super(roomId, gameSessionId, turnNumber);
        this.gameState = gameState;
    }
}
