package com.unduck.paletteduck.domain.chat.constants;

public final class ChatConstants {

    private ChatConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    // 채팅 메시지 템플릿
    public static final class Message {
        public static final String CORRECT_ANSWER = "🎉 정답입니다!";
        public static final String PLAYER_GUESSED_FORMAT = "%s님이 정답을 맞췄습니다!";

        private Message() {}
    }
}
