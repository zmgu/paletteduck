package com.unduck.paletteduck.domain.room.constant;

public class RoomConstants {

    // 참가자 제한
    public static final int MIN_PLAYERS = 2;
    public static final int DEFAULT_MAX_PLAYERS = 10;
    public static final int MAX_PLAYERS = 20;

    // 관전자 제한
    public static final int MAX_SPECTATORS = 20;

    // 라운드 제한
    public static final int MIN_ROUNDS = 2;
    public static final int MAX_ROUNDS = 10;
    public static final int DEFAULT_ROUNDS = 3;

    // 단어 선택지
    public static final int MIN_WORD_CHOICES = 2;
    public static final int MAX_WORD_CHOICES = 4;
    public static final int DEFAULT_WORD_CHOICES = 3;

    // 그리기 시간
    public static final int MIN_DRAW_TIME = 30;
    public static final int MAX_DRAW_TIME = 240;
    public static final int DEFAULT_DRAW_TIME = 80;

    // Redis TTL
    public static final long ROOM_TTL_HOURS = 24;

    private RoomConstants() {
        // 인스턴스화 방지
    }
}
