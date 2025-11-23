package com.unduck.paletteduck.domain.game.constants;

public final class GameConstants {

    private GameConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    // 점수 관련 상수
    public static final class Score {
        public static final int FIRST_CORRECT = 300;
        public static final int SECOND_CORRECT = 200;
        public static final int THIRD_CORRECT = 100;
        public static final int DRAWER_BONUS = 50;

        private Score() {}
    }

    // 타이밍 관련 상수
    public static final class Timing {
        public static final int WORD_SELECT_TIME = 15;
        public static final int COUNTDOWN_TIME = 3;
        public static final int ROUND_END_DELAY = 5000;  // 5초 (밀리초 단위)
        public static final int DRAWING_THROTTLE_MS = 50;
        public static final int FIRST_HINT_DELAY = 20;  // 첫 번째 힌트 (초성 일부) - 20초 후
        public static final int SECOND_HINT_DELAY = 40; // 두 번째 힌트 (전체 초성) - 40초 후

        private Timing() {}
    }

    // 게임 페이즈
    public static final class Phase {
        public static final String COUNTDOWN = "COUNTDOWN";
        public static final String WORD_SELECT = "WORD_SELECT";
        public static final String DRAWING = "DRAWING";
        public static final String ROUND_END = "ROUND_END";
        public static final String GAME_END = "GAME_END";

        private Phase() {}
    }

    // 시스템 플레이어
    public static final class SystemPlayer {
        public static final String ID = "system";
        public static final String NAME = "System";

        private SystemPlayer() {}
    }
}
