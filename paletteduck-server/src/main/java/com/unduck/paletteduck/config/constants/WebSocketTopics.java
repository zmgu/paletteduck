package com.unduck.paletteduck.config.constants;

public final class WebSocketTopics {

    private WebSocketTopics() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    private static final String TOPIC_PREFIX = "/topic/room/";

    public static String room(String roomId) {
        return TOPIC_PREFIX + roomId;
    }

    public static String roomChat(String roomId) {
        return TOPIC_PREFIX + roomId + "/chat";
    }

    public static String roomStart(String roomId) {
        return TOPIC_PREFIX + roomId + "/start";
    }

    public static String gameState(String roomId) {
        return TOPIC_PREFIX + roomId + "/game/state";
    }

    public static String gameStart(String roomId) {
        return TOPIC_PREFIX + roomId + "/game/start";
    }

    public static String gameDraw(String roomId) {
        return TOPIC_PREFIX + roomId + "/game/draw";
    }

    public static String gameDrawing(String roomId) {
        return TOPIC_PREFIX + roomId + "/game/drawing";
    }

    public static String gameClear(String roomId) {
        return TOPIC_PREFIX + roomId + "/game/clear";
    }
}
