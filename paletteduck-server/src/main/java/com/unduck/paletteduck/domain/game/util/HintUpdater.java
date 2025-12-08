package com.unduck.paletteduck.domain.game.util;

import com.unduck.paletteduck.domain.game.dto.TurnInfo;
import com.unduck.paletteduck.domain.game.service.HintService;

/**
 * 힌트 배열 및 문자열 업데이트를 위한 유틸리티 클래스
 */
public final class HintUpdater {

    private HintUpdater() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * TurnInfo의 힌트 배열과 현재 힌트 문자열을 업데이트합니다
     *
     * @param turnInfo 업데이트할 TurnInfo
     * @param word 정답 단어
     * @param hintService HintService 인스턴스
     */
    public static void updateHints(TurnInfo turnInfo, String word, HintService hintService) {
        if (turnInfo == null || word == null || hintService == null) {
            return;
        }

        String[] hintArray = hintService.generateHintArray(word,
                turnInfo.getRevealedChosungPositions(),
                turnInfo.getRevealedLetterPositions());
        turnInfo.setHintArray(hintArray);

        String hint = hintService.generateHintDisplay(word,
                turnInfo.getRevealedChosungPositions(),
                turnInfo.getRevealedLetterPositions());
        turnInfo.setCurrentHint(hint);
    }
}
