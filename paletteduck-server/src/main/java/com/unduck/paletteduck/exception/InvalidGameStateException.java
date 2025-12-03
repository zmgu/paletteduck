package com.unduck.paletteduck.exception;

/**
 * 잘못된 게임 상태일 때 발생하는 예외
 */
public class InvalidGameStateException extends BusinessException {
    public InvalidGameStateException() {
        super(ErrorCode.INVALID_GAME_STATE);
    }

    public InvalidGameStateException(String message) {
        super(ErrorCode.INVALID_GAME_STATE, message);
    }
}
