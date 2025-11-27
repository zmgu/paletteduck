package com.unduck.paletteduck.domain.game.service;

import com.unduck.paletteduck.domain.game.constants.GameConstants;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.dto.Player;
import com.unduck.paletteduck.domain.game.dto.TurnInfo;
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

        // 정답자 점수 계산 및 추가 (힌트 차감 적용)
        int earnedScore = calculateAnswererScore(gameState);
        int currentScore = player.getScore() != null ? player.getScore() : 0;
        player.setScore(currentScore + earnedScore);

        // 출제자 점수 추가 (누적 점수 × 배율)
        awardDrawerBonus(gameState);

        log.info("Player {} guessed correctly. Earned: {}, Total: {}",
                player.getNickname(), earnedScore, player.getScore());
    }

    /**
     * 정답자 점수 계산 (등수별 기본 점수 - 힌트 차감)
     */
    private int calculateAnswererScore(GameState gameState) {
        long correctCount = gameState.getPlayers().stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsCorrect()))
                .count();

        // 등수별 기본 점수
        int baseScore;
        if (correctCount == 1) {
            baseScore = GameConstants.Score.FIRST_CORRECT;
        } else if (correctCount == 2) {
            baseScore = GameConstants.Score.SECOND_CORRECT;
        } else if (correctCount == 3) {
            baseScore = GameConstants.Score.THIRD_CORRECT;
        } else if (correctCount == 4) {
            baseScore = GameConstants.Score.FOURTH_CORRECT;
        } else if (correctCount == 5) {
            baseScore = GameConstants.Score.FIFTH_CORRECT;
        } else {
            baseScore = GameConstants.Score.SIXTH_OR_LATER_CORRECT;
        }

        // 힌트 차감 계산
        int penalty = calculateHintPenalty(gameState.getCurrentTurn());

        // 최종 점수 (최소 0점)
        return Math.max(GameConstants.Score.MIN_SCORE, baseScore - penalty);
    }

    /**
     * 출제자 보너스 점수 부여 (정답자별 누적 기본 점수 × 배율들)
     */
    private void awardDrawerBonus(GameState gameState) {
        String drawerId = gameState.getCurrentTurn().getDrawerId();
        Player drawer = gameState.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(drawerId))
                .findFirst()
                .orElse(null);

        if (drawer == null) {
            return;
        }

        // 현재 정답자 순서
        long correctCount = gameState.getPlayers().stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsCorrect()))
                .count();

        // 이번 정답자의 기본 점수
        int answererBaseScore = getAnswererBaseScore((int) correctCount);

        // 배율 계산
        int totalPlayers = (int) gameState.getPlayers().stream()
                .filter(p -> !p.getPlayerId().equals(drawerId))
                .count();
        double playerCountMultiplier = getPlayerCountMultiplier(totalPlayers);
        double hintMultiplier = calculateHintMultiplier(gameState.getCurrentTurn());
        double earlyCorrectPenalty = getEarlyCorrectPenalty(gameState);

        // 출제자가 받을 점수
        double drawerScore = answererBaseScore * playerCountMultiplier *
                hintMultiplier * earlyCorrectPenalty;

        // 출제자 점수 증가
        int currentScore = drawer.getScore() != null ? drawer.getScore() : 0;
        drawer.setScore(currentScore + (int) Math.round(drawerScore));

        log.debug("Drawer bonus: {} (base: {}, player: {:.2f}x, hint: {:.2f}x, penalty: {:.2f}x)",
                (int) Math.round(drawerScore), answererBaseScore,
                playerCountMultiplier, hintMultiplier, earlyCorrectPenalty);
    }

    /**
     * 정답자 순서에 따른 기본 점수
     */
    private int getAnswererBaseScore(int answerOrder) {
        switch (answerOrder) {
            case 1: return GameConstants.Score.FIRST_ANSWERER;
            case 2: return GameConstants.Score.SECOND_ANSWERER;
            case 3: return GameConstants.Score.THIRD_ANSWERER;
            case 4: return GameConstants.Score.FOURTH_ANSWERER;
            case 5: return GameConstants.Score.FIFTH_ANSWERER;
            case 6: return GameConstants.Score.SIXTH_ANSWERER;
            default: return GameConstants.Score.SEVENTH_OR_LATER_ANSWERER;
        }
    }

    /**
     * 인원수에 따른 배율
     */
    private double getPlayerCountMultiplier(int playerCount) {
        if (playerCount <= 4) return 1.5;
        if (playerCount <= 7) return 1.2;
        if (playerCount <= 10) return 1.0;
        if (playerCount <= 15) return 0.8;
        return 0.65;
    }

    /**
     * 힌트 배율 계산 (출제자용)
     */
    private double calculateHintMultiplier(TurnInfo turnInfo) {
        if (turnInfo == null) {
            return GameConstants.Score.HINT_MULTIPLIER_NONE;
        }

        int chosungCount = turnInfo.getRevealedChosungPositions() != null ?
                turnInfo.getRevealedChosungPositions().size() : 0;
        int letterCount = turnInfo.getRevealedLetterPositions() != null ?
                turnInfo.getRevealedLetterPositions().size() : 0;

        double multiplier = GameConstants.Score.HINT_MULTIPLIER_NONE;

        // 초성 배율
        if (chosungCount >= 3) {
            multiplier = GameConstants.Score.HINT_MULTIPLIER_CHOSUNG_3;
        } else if (chosungCount == 2) {
            multiplier = GameConstants.Score.HINT_MULTIPLIER_CHOSUNG_2;
        } else if (chosungCount == 1) {
            multiplier = GameConstants.Score.HINT_MULTIPLIER_CHOSUNG_1;
        }

        // 글자 배율 (추가 곱하기)
        if (letterCount >= 2) {
            multiplier *= 0.70;
        } else if (letterCount == 1) {
            multiplier *= GameConstants.Score.HINT_MULTIPLIER_LETTER_1;
        }

        // 최소 배율 보장
        return Math.max(GameConstants.Score.MIN_HINT_MULTIPLIER, multiplier);
    }

    /**
     * 조기 정답 패널티 (힌트 없이 많이 맞추면 패널티)
     */
    private double getEarlyCorrectPenalty(GameState gameState) {
        TurnInfo turnInfo = gameState.getCurrentTurn();

        // 힌트를 1개라도 사용했으면 패널티 없음
        int totalHints = (turnInfo.getRevealedChosungPositions() != null ?
                turnInfo.getRevealedChosungPositions().size() : 0) +
                (turnInfo.getRevealedLetterPositions() != null ?
                        turnInfo.getRevealedLetterPositions().size() : 0);

        if (totalHints > 0) {
            return 1.0; // 패널티 없음
        }

        // 출제자를 제외한 플레이어 수
        String drawerId = turnInfo.getDrawerId();
        long totalPlayers = gameState.getPlayers().stream()
                .filter(p -> !p.getPlayerId().equals(drawerId))
                .count();

        // 정답자 수
        long correctCount = gameState.getPlayers().stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsCorrect()))
                .count();

        if (totalPlayers == 0) {
            return 1.0;
        }

        double correctRate = (double) correctCount / totalPlayers;

        // 정답률에 따른 패널티
        if (correctRate >= 0.70) {
            return 0.70; // -30%
        } else if (correctRate >= 0.50) {
            return 0.80; // -20%
        } else if (correctRate >= 0.30) {
            return 0.90; // -10%
        }

        return 1.0; // 패널티 없음
    }

    /**
     * 힌트 차감 점수 계산 (정답자용)
     */
    private int calculateHintPenalty(TurnInfo turnInfo) {
        if (turnInfo == null) {
            return 0;
        }

        int chosungCount = turnInfo.getRevealedChosungPositions() != null ?
                turnInfo.getRevealedChosungPositions().size() : 0;
        int letterCount = turnInfo.getRevealedLetterPositions() != null ?
                turnInfo.getRevealedLetterPositions().size() : 0;

        int chosungPenalty = chosungCount * GameConstants.Score.CHOSUNG_HINT_PENALTY;
        int letterPenalty = letterCount * GameConstants.Score.LETTER_HINT_PENALTY;

        return chosungPenalty + letterPenalty;
    }
}
