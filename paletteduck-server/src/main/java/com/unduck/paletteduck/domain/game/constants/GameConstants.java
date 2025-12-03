package com.unduck.paletteduck.domain.game.constants;

public final class GameConstants {

    private GameConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    // 점수 관련 상수
    public static final class Score {
        // 정답자 등수별 기본 점수 (힌트 차감 전)
        public static final int FIRST_CORRECT = 300;
        public static final int SECOND_CORRECT = 220;
        public static final int THIRD_CORRECT = 160;
        public static final int FOURTH_CORRECT = 120;
        public static final int FIFTH_CORRECT = 90;
        public static final int SIXTH_OR_LATER_CORRECT = 70;

        // 출제자 정답자별 누적 기본 점수
        public static final int FIRST_ANSWERER = 50;
        public static final int SECOND_ANSWERER = 45;
        public static final int THIRD_ANSWERER = 40;
        public static final int FOURTH_ANSWERER = 35;
        public static final int FIFTH_ANSWERER = 30;
        public static final int SIXTH_ANSWERER = 25;
        public static final int SEVENTH_OR_LATER_ANSWERER = 20;

        // 힌트 차감 점수 (정답자용)
        public static final int CHOSUNG_HINT_PENALTY = 25;
        public static final int LETTER_HINT_PENALTY = 50;

        // 힌트 배율 (출제자용)
        public static final double HINT_MULTIPLIER_NONE = 1.00;
        public static final double HINT_MULTIPLIER_CHOSUNG_1 = 0.85;
        public static final double HINT_MULTIPLIER_CHOSUNG_2 = 0.70;
        public static final double HINT_MULTIPLIER_CHOSUNG_3 = 0.60;
        public static final double HINT_MULTIPLIER_LETTER_1 = 0.50;
        public static final double MIN_HINT_MULTIPLIER = 0.30;

        // 최소 점수
        public static final int MIN_SCORE = 0;

        private Score() {}
    }

    // 타이밍 관련 상수
    public static final class Timing {
        public static final int WORD_SELECT_TIME = 20;
        public static final int COUNTDOWN_TIME = 3;
        public static final int ROUND_END_DELAY = 5000;  // 5초 (밀리초 단위)
        public static final int DRAWING_THROTTLE_MS = 50;
        public static final int FIRST_HINT_DELAY = 20;  // 첫 번째 힌트 (초성 일부) - 20초 후
        public static final int SECOND_HINT_DELAY = 40; // 두 번째 힌트 (전체 초성) - 40초 후
        public static final int AUTO_RETURN_TO_WAITING_TIME = 20; // 자동 대기방 복귀 시간 (초)
        public static final int ALL_CORRECT_TURN_END_DELAY = 100; // 모든 플레이어 정답 시 턴 종료 지연 (밀리초)

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
