package com.unduck.paletteduck.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Room 관련
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "ROOM_001", "방을 찾을 수 없습니다."),
    ROOM_FULL(HttpStatus.BAD_REQUEST, "ROOM_002", "방이 가득 찼습니다."),
    ROOM_ALREADY_PLAYING(HttpStatus.BAD_REQUEST, "ROOM_003", "이미 게임이 진행 중입니다."),
    ROOM_NOT_PLAYING(HttpStatus.BAD_REQUEST, "ROOM_004", "게임이 진행 중이 아닙니다."),

    // Player 관련
    PLAYER_NOT_FOUND(HttpStatus.NOT_FOUND, "PLAYER_001", "플레이어를 찾을 수 없습니다."),
    PLAYER_ALREADY_EXISTS(HttpStatus.CONFLICT, "PLAYER_002", "이미 존재하는 플레이어입니다."),
    PLAYER_NOT_HOST(HttpStatus.FORBIDDEN, "PLAYER_003", "방장이 아닙니다."),
    PLAYER_NOT_DRAWER(HttpStatus.FORBIDDEN, "PLAYER_004", "출제자가 아닙니다."),

    // Game 관련
    GAME_NOT_FOUND(HttpStatus.NOT_FOUND, "GAME_001", "게임 상태를 찾을 수 없습니다."),
    INVALID_GAME_PHASE(HttpStatus.BAD_REQUEST, "GAME_002", "잘못된 게임 페이즈입니다."),
    INVALID_GAME_STATE(HttpStatus.BAD_REQUEST, "GAME_003", "잘못된 게임 상태입니다."),
    INSUFFICIENT_PLAYERS(HttpStatus.BAD_REQUEST, "GAME_004", "플레이어가 부족합니다."),

    // Return to Waiting 관련
    RETURN_TO_WAITING_FAILED(HttpStatus.BAD_REQUEST, "RETURN_001", "대기방 복귀에 실패했습니다."),
    SPECTATOR_RETURN_NOT_ALLOWED(HttpStatus.FORBIDDEN, "RETURN_002", "관전자는 대기방으로 복귀할 수 없습니다."),

    // 내부 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_001", "서버 내부 오류가 발생했습니다."),
    DATA_SERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_002", "데이터 직렬화 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
