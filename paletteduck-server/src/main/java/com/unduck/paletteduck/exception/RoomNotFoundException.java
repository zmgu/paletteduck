package com.unduck.paletteduck.exception;

/**
 * 방을 찾을 수 없을 때 발생하는 예외
 */
public class RoomNotFoundException extends BusinessException {
    public RoomNotFoundException() {
        super(ErrorCode.ROOM_NOT_FOUND);
    }

    public RoomNotFoundException(String roomId) {
        super(ErrorCode.ROOM_NOT_FOUND, "방을 찾을 수 없습니다: " + roomId);
    }
}
