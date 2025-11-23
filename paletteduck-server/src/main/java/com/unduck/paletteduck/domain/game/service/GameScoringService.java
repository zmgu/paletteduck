package com.unduck.paletteduck.domain.game.service;

import com.unduck.paletteduck.domain.game.constants.GameConstants;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.dto.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GameScoringService {

    public void handleCorrectAnswer(GameState gameState, Player player) {
        if (player == null) {
            log.error("Player not found in game state");
            return;
        }

        // 플레이어 정답 여부 업데이트
        player.setIsCorrect(true);

        // 정답자 점수 계산 및 추가
        int earnedScore = calculateAnswererScore(gameState);
        int currentScore = player.getScore() != null ? player.getScore() : 0;
        player.setScore(currentScore + earnedScore);

        // 출제자 점수 추가
        awardDrawerBonus(gameState);

        log.info("Player {} guessed correctly. Earned: {}, Total: {}",
                player.getNickname(), earnedScore, player.getScore());
    }

    private int calculateAnswererScore(GameState gameState) {
        long correctCount = gameState.getPlayers().stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsCorrect()))
                .count();

        if (correctCount == 1) {
            return GameConstants.Score.FIRST_CORRECT;
        } else if (correctCount == 2) {
            return GameConstants.Score.SECOND_CORRECT;
        } else if (correctCount == 3) {
            return GameConstants.Score.THIRD_CORRECT;
        }

        return 0;
    }

    private void awardDrawerBonus(GameState gameState) {
        String drawerId = gameState.getCurrentTurn().getDrawerId();
        Player drawer = gameState.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(drawerId))
                .findFirst()
                .orElse(null);

        if (drawer != null) {
            int currentScore = drawer.getScore() != null ? drawer.getScore() : 0;
            drawer.setScore(currentScore + GameConstants.Score.DRAWER_BONUS);
        }
    }
}
