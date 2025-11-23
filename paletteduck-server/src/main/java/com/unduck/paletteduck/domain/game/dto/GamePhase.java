package com.unduck.paletteduck.domain.game.dto;

public enum GamePhase {
    COUNTDOWN,      // 게임 시작 카운트다운 (3초)
    WORD_SELECT,    // 출제자 단어 선택 (15초)
    DRAWING,        // 그림 그리기 (설정된 시간)
    TURN_RESULT,    // 턴 결과 (5초, 점수 및 추천/비추천 표시)
    ROUND_END,      // 라운드 종료 (5초, 정답 공개)
    GAME_END        // 게임 종료
}