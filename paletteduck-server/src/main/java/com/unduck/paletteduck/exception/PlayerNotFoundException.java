package com.unduck.paletteduck.exception;

/**
 * 플레이어를 찾을 수 없을 때 발생하는 예외
 */
public class PlayerNotFoundException extends BusinessException {
    public PlayerNotFoundException() {
        super(ErrorCode.PLAYER_NOT_FOUND);
    }

    public PlayerNotFoundException(String playerId) {
        super(ErrorCode.PLAYER_NOT_FOUND, "플레이어를 찾을 수 없습니다: " + playerId);
    }
}
