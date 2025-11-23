package com.unduck.paletteduck.domain.game.service;

import com.unduck.paletteduck.domain.game.dto.GameState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AnswerValidationService {

    public boolean checkAnswer(GameState gameState, String userAnswer) {
        if (gameState.getCurrentTurn() == null || gameState.getCurrentTurn().getWord() == null) {
            return false;
        }

        String correctAnswer = gameState.getCurrentTurn().getWord();

        // 대소문자 무시, 공백 제거
        correctAnswer = normalizeAnswer(correctAnswer);
        userAnswer = normalizeAnswer(userAnswer);

        return correctAnswer.equals(userAnswer);
    }

    private String normalizeAnswer(String answer) {
        return answer.toLowerCase().replaceAll("\\s+", "");
    }
}
