package com.unduck.paletteduck.exception;

/**
 * 게임 상태를 찾을 수 없을 때 발생하는 예외
 */
public class GameNotFoundException extends BusinessException {
    public GameNotFoundException() {
        super(ErrorCode.GAME_NOT_FOUND);
    }

    public GameNotFoundException(String roomId) {
        super(ErrorCode.GAME_NOT_FOUND, "게임 상태를 찾을 수 없습니다: " + roomId);
    }
}
