package com.unduck.paletteduck.domain.game.service;

import com.unduck.paletteduck.domain.game.constants.GameConstants;
import com.unduck.paletteduck.domain.game.dto.GamePhase;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.dto.Player;
import com.unduck.paletteduck.domain.game.dto.TurnInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GameScoringService 테스트")
class GameScoringServiceTest {

    private GameScoringService gameScoringService;

    @BeforeEach
    void setUp() {
        gameScoringService = new GameScoringService();
    }

    @Test
    @Tag("scoring-integration")
    @DisplayName("handleCorrectAnswer - 정상: 1등 정답자는 300점을 획득해야 한다")
    void handleCorrectAnswer_firstPlayer_shouldEarn300Points() {
        // given
        GameState gameState = createGameState(3);
        Player player = gameState.getPlayers().get(1); // 정답자 (플레이어2)

        // when
        gameScoringService.handleCorrectAnswer(gameState, player);

        // then
        assertThat(player.getIsCorrect()).isTrue();
        assertThat(player.getScore()).isEqualTo(300);
    }

    @Test
    @Tag("scoring-edge-case")
    @DisplayName("handleCorrectAnswer - null 플레이어인 경우 아무 작업도 하지 않아야 한다")
    void handleCorrectAnswer_nullPlayer_shouldDoNothing() {
        // given
        GameState gameState = createGameState(3);

        // when & then - 예외가 발생하지 않아야 함
        gameScoringService.handleCorrectAnswer(gameState, null);
    }

    @Test
    @Tag("scoring-integration")
    @DisplayName("handleCorrectAnswer - 기존 점수에 새 점수가 누적되어야 한다")
    void handleCorrectAnswer_shouldAccumulateScore() {
        // given
        GameState gameState = createGameState(3);
        Player player = gameState.getPlayers().get(1);
        player.setScore(100); // 기존 점수

        // when
        gameScoringService.handleCorrectAnswer(gameState, player);

        // then
        assertThat(player.getScore()).isEqualTo(400); // 100 + 300
    }

    @Test
    @Tag("scoring-answerer")
    @DisplayName("calculateAnswererScore - 1등은 300점을 받아야 한다")
    void calculateAnswererScore_firstPlace_shouldReturn300() {
        // given
        GameState gameState = createGameState(3);

        // when
        Player player = gameState.getPlayers().get(1);
        gameScoringService.handleCorrectAnswer(gameState, player);

        // then
        assertThat(player.getScore()).isEqualTo(GameConstants.Score.FIRST_CORRECT);
    }

    @Test
    @Tag("scoring-answerer")
    @DisplayName("calculateAnswererScore - 2등은 220점을 받아야 한다")
    void calculateAnswererScore_secondPlace_shouldReturn220() {
        // given
        GameState gameState = createGameState(4);
        Player player1 = gameState.getPlayers().get(1);
        Player player2 = gameState.getPlayers().get(2);

        // when
        gameScoringService.handleCorrectAnswer(gameState, player1);
        gameScoringService.handleCorrectAnswer(gameState, player2);

        // then
        assertThat(player2.getScore()).isEqualTo(GameConstants.Score.SECOND_CORRECT);
    }

    @Test
    @Tag("scoring-answerer")
    @DisplayName("calculateAnswererScore - 3등은 160점을 받아야 한다")
    void calculateAnswererScore_thirdPlace_shouldReturn160() {
        // given
        GameState gameState = createGameState(5);

        // when
        markPlayersCorrect(gameState, 3);
        Player thirdPlayer = gameState.getPlayers().stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsCorrect()))
                .skip(2)
                .findFirst()
                .orElse(null);

        // then
        assertThat(thirdPlayer).isNotNull();
        assertThat(thirdPlayer.getScore()).isEqualTo(GameConstants.Score.THIRD_CORRECT);
    }

    @Test
    @Tag("scoring-answerer")
    @DisplayName("calculateAnswererScore - 4등은 120점을 받아야 한다")
    void calculateAnswererScore_fourthPlace_shouldReturn120() {
        // given
        GameState gameState = createGameState(6);

        // when
        markPlayersCorrect(gameState, 4);
        Player fourthPlayer = gameState.getPlayers().stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsCorrect()))
                .skip(3)
                .findFirst()
                .orElse(null);

        // then
        assertThat(fourthPlayer).isNotNull();
        assertThat(fourthPlayer.getScore()).isEqualTo(GameConstants.Score.FOURTH_CORRECT);
    }

    @Test
    @Tag("scoring-answerer")
    @DisplayName("calculateAnswererScore - 5등은 90점을 받아야 한다")
    void calculateAnswererScore_fifthPlace_shouldReturn90() {
        // given
        GameState gameState = createGameState(7);

        // when
        markPlayersCorrect(gameState, 5);
        Player fifthPlayer = gameState.getPlayers().stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsCorrect()))
                .skip(4)
                .findFirst()
                .orElse(null);

        // then
        assertThat(fifthPlayer).isNotNull();
        assertThat(fifthPlayer.getScore()).isEqualTo(GameConstants.Score.FIFTH_CORRECT);
    }

    @Test
    @Tag("scoring-answerer")
    @DisplayName("calculateAnswererScore - 6등 이후는 70점을 받아야 한다")
    void calculateAnswererScore_sixthOrLater_shouldReturn70() {
        // given
        GameState gameState = createGameState(8);

        // when
        markPlayersCorrect(gameState, 6);
        Player sixthPlayer = gameState.getPlayers().stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsCorrect()))
                .skip(5)
                .findFirst()
                .orElse(null);

        // then
        assertThat(sixthPlayer).isNotNull();
        assertThat(sixthPlayer.getScore()).isEqualTo(GameConstants.Score.SIXTH_OR_LATER_CORRECT);
    }

    @Test
    @Tag("scoring-answerer")
    @DisplayName("calculateAnswererScore - 초성 힌트 1개당 25점 차감되어야 한다")
    void calculateAnswererScore_withChosungHint_shouldDeduct25Points() {
        // given
        GameState gameState = createGameState(3);
        TurnInfo turnInfo = gameState.getCurrentTurn();
        Set<Integer> chosungPositions = new HashSet<>();
        chosungPositions.add(0);
        turnInfo.setRevealedChosungPositions(chosungPositions);

        // when
        Player player = gameState.getPlayers().get(1);
        gameScoringService.handleCorrectAnswer(gameState, player);

        // then
        assertThat(player.getScore()).isEqualTo(300 - 25); // 275점
    }

    @Test
    @Tag("scoring-answerer")
    @DisplayName("calculateAnswererScore - 글자 힌트 1개당 50점 차감되어야 한다")
    void calculateAnswererScore_withLetterHint_shouldDeduct50Points() {
        // given
        GameState gameState = createGameState(3);
        TurnInfo turnInfo = gameState.getCurrentTurn();
        Set<Integer> letterPositions = new HashSet<>();
        letterPositions.add(0);
        turnInfo.setRevealedLetterPositions(letterPositions);

        // when
        Player player = gameState.getPlayers().get(1);
        gameScoringService.handleCorrectAnswer(gameState, player);

        // then
        assertThat(player.getScore()).isEqualTo(300 - 50); // 250점
    }

    @Test
    @Tag("scoring-answerer")
    @DisplayName("calculateAnswererScore - 초성 2개와 글자 1개 힌트로 150점 차감되어야 한다")
    void calculateAnswererScore_withMultipleHints_shouldDeductCorrectAmount() {
        // given
        GameState gameState = createGameState(3);
        TurnInfo turnInfo = gameState.getCurrentTurn();
        Set<Integer> chosungPositions = new HashSet<>();
        chosungPositions.add(0);
        chosungPositions.add(1);
        Set<Integer> letterPositions = new HashSet<>();
        letterPositions.add(2);
        turnInfo.setRevealedChosungPositions(chosungPositions);
        turnInfo.setRevealedLetterPositions(letterPositions);

        // when
        Player player = gameState.getPlayers().get(1);
        gameScoringService.handleCorrectAnswer(gameState, player);

        // then
        assertThat(player.getScore()).isEqualTo(300 - 25 * 2 - 50); // 200점
    }

    @Test
    @Tag("scoring-edge-case")
    @DisplayName("calculateAnswererScore - 페널티로 인해 점수가 음수가 되면 0점이 되어야 한다")
    void calculateAnswererScore_negativeScore_shouldReturnZero() {
        // given
        GameState gameState = createGameState(8); // 출제자 + 7명 (6등 이후 테스트를 위해)
        TurnInfo turnInfo = gameState.getCurrentTurn();

        // 6등 이후 (70점)에 글자 힌트 2개 (100점 차감) = -30점 -> 0점
        markPlayersCorrect(gameState, 5); // 5명을 먼저 정답 처리

        Set<Integer> letterPositions = new HashSet<>();
        letterPositions.add(0);
        letterPositions.add(1);
        turnInfo.setRevealedLetterPositions(letterPositions);

        // when
        Player player = gameState.getPlayers().get(6);
        gameScoringService.handleCorrectAnswer(gameState, player);

        // then
        assertThat(player.getScore()).isEqualTo(0);
    }

    @Test
    @Tag("scoring-drawer-bonus")
    @DisplayName("awardDrawerBonus - 2-4명일 때 150% 배율이 적용되어야 한다")
    void awardDrawerBonus_2to4Players_shouldApply150PercentMultiplier() {
        // given
        GameState gameState = createGameState(4); // 출제자 + 3명 = 4명
        Player drawer = gameState.getPlayers().get(0);
        Player guesser = gameState.getPlayers().get(1);

        // when
        gameScoringService.handleCorrectAnswer(gameState, guesser);

        // then
        // 1등 정답자의 출제자 점수: 50 * 1.5 * 0.90 (조기 정답 패널티) = 67.5 -> 68
        assertThat(drawer.getScore()).isEqualTo(68);
    }

    @Test
    @Tag("scoring-drawer-bonus")
    @DisplayName("awardDrawerBonus - 5-7명일 때 120% 배율이 적용되어야 한다")
    void awardDrawerBonus_5to7Players_shouldApply120PercentMultiplier() {
        // given
        GameState gameState = createGameState(6); // 출제자 + 5명
        Player drawer = gameState.getPlayers().get(0);
        Player guesser = gameState.getPlayers().get(1);

        // when
        gameScoringService.handleCorrectAnswer(gameState, guesser);

        // then
        // 1등 정답자의 출제자 점수: 50 * 1.2 = 60
        assertThat(drawer.getScore()).isEqualTo(60);
    }

    @Test
    @Tag("scoring-drawer-bonus")
    @DisplayName("awardDrawerBonus - 8-10명일 때 100% 배율이 적용되어야 한다")
    void awardDrawerBonus_8to10Players_shouldApply100PercentMultiplier() {
        // given
        GameState gameState = createGameState(9); // 출제자 + 8명
        Player drawer = gameState.getPlayers().get(0);
        Player guesser = gameState.getPlayers().get(1);

        // when
        gameScoringService.handleCorrectAnswer(gameState, guesser);

        // then
        // 1등 정답자의 출제자 점수: 50 * 1.0 = 50
        assertThat(drawer.getScore()).isEqualTo(50);
    }

    @Test
    @Tag("scoring-drawer-bonus")
    @DisplayName("awardDrawerBonus - 11-15명일 때 80% 배율이 적용되어야 한다")
    void awardDrawerBonus_11to15Players_shouldApply80PercentMultiplier() {
        // given
        GameState gameState = createGameState(12); // 출제자 + 11명
        Player drawer = gameState.getPlayers().get(0);
        Player guesser = gameState.getPlayers().get(1);

        // when
        gameScoringService.handleCorrectAnswer(gameState, guesser);

        // then
        // 1등 정답자의 출제자 점수: 50 * 0.8 = 40
        assertThat(drawer.getScore()).isEqualTo(40);
    }

    @Test
    @Tag("scoring-drawer-bonus")
    @DisplayName("awardDrawerBonus - 16-20명일 때 65% 배율이 적용되어야 한다")
    void awardDrawerBonus_16to20Players_shouldApply65PercentMultiplier() {
        // given
        GameState gameState = createGameState(17); // 출제자 + 16명
        Player drawer = gameState.getPlayers().get(0);
        Player guesser = gameState.getPlayers().get(1);

        // when
        gameScoringService.handleCorrectAnswer(gameState, guesser);

        // then
        // 1등 정답자의 출제자 점수: 50 * 0.65 = 32.5 -> 33 (반올림)
        assertThat(drawer.getScore()).isEqualTo(33);
    }

    @Test
    @Tag("scoring-hint-penalty")
    @DisplayName("calculateHintMultiplier - 힌트 없으면 100% 배율이어야 한다")
    void calculateHintMultiplier_noHints_shouldReturn100Percent() {
        // given
        GameState gameState = createGameState(4); // 출제자 + 3명 = 4명
        Player drawer = gameState.getPlayers().get(0);
        Player guesser = gameState.getPlayers().get(1);

        // when
        gameScoringService.handleCorrectAnswer(gameState, guesser);

        // then
        // 1등: 50 * 1.5 (player count) * 1.0 (hint) * 0.90 (패널티) = 67.5 -> 68
        assertThat(drawer.getScore()).isEqualTo(68);
    }

    @Test
    @Tag("scoring-hint-penalty")
    @DisplayName("calculateHintMultiplier - 초성 1개면 85% 배율이어야 한다")
    void calculateHintMultiplier_oneChosung_shouldReturn85Percent() {
        // given
        GameState gameState = createGameState(3);
        TurnInfo turnInfo = gameState.getCurrentTurn();
        Set<Integer> chosungPositions = new HashSet<>();
        chosungPositions.add(0);
        turnInfo.setRevealedChosungPositions(chosungPositions);

        Player drawer = gameState.getPlayers().get(0);
        Player guesser = gameState.getPlayers().get(1);

        // when
        gameScoringService.handleCorrectAnswer(gameState, guesser);

        // then
        // 1등: 50 * 1.5 (player count) * 0.85 (hint) = 63.75 -> 64
        assertThat(drawer.getScore()).isEqualTo(64);
    }

    @Test
    @Tag("scoring-hint-penalty")
    @DisplayName("calculateHintMultiplier - 초성 2개면 70% 배율이어야 한다")
    void calculateHintMultiplier_twoChosung_shouldReturn70Percent() {
        // given
        GameState gameState = createGameState(3);
        TurnInfo turnInfo = gameState.getCurrentTurn();
        Set<Integer> chosungPositions = new HashSet<>();
        chosungPositions.add(0);
        chosungPositions.add(1);
        turnInfo.setRevealedChosungPositions(chosungPositions);

        Player drawer = gameState.getPlayers().get(0);
        Player guesser = gameState.getPlayers().get(1);

        // when
        gameScoringService.handleCorrectAnswer(gameState, guesser);

        // then
        // 1등: 50 * 1.5 (player count) * 0.70 (hint) = 52.5 -> 53
        assertThat(drawer.getScore()).isEqualTo(53);
    }

    @Test
    @Tag("scoring-hint-penalty")
    @DisplayName("calculateHintMultiplier - 초성 3개 이상이면 60% 배율이어야 한다")
    void calculateHintMultiplier_threeOrMoreChosung_shouldReturn60Percent() {
        // given
        GameState gameState = createGameState(3);
        TurnInfo turnInfo = gameState.getCurrentTurn();
        Set<Integer> chosungPositions = new HashSet<>();
        chosungPositions.add(0);
        chosungPositions.add(1);
        chosungPositions.add(2);
        turnInfo.setRevealedChosungPositions(chosungPositions);

        Player drawer = gameState.getPlayers().get(0);
        Player guesser = gameState.getPlayers().get(1);

        // when
        gameScoringService.handleCorrectAnswer(gameState, guesser);

        // then
        // 1등: 50 * 1.5 (player count) * 0.60 (hint) = 45
        assertThat(drawer.getScore()).isEqualTo(45);
    }

    @Test
    @Tag("scoring-hint-penalty")
    @DisplayName("calculateHintMultiplier - 글자 1개면 50%가 곱해져야 한다")
    void calculateHintMultiplier_oneLetter_shouldMultiply50Percent() {
        // given
        GameState gameState = createGameState(3);
        TurnInfo turnInfo = gameState.getCurrentTurn();
        Set<Integer> letterPositions = new HashSet<>();
        letterPositions.add(0);
        turnInfo.setRevealedLetterPositions(letterPositions);

        Player drawer = gameState.getPlayers().get(0);
        Player guesser = gameState.getPlayers().get(1);

        // when
        gameScoringService.handleCorrectAnswer(gameState, guesser);

        // then
        // 1등: 50 * 1.5 (player count) * 1.0 (no chosung) * 0.5 (letter) = 37.5 -> 38
        assertThat(drawer.getScore()).isEqualTo(38);
    }

    @Test
    @Tag("scoring-hint-penalty")
    @DisplayName("calculateHintMultiplier - 글자 2개 이상이면 70%가 곱해져야 한다")
    void calculateHintMultiplier_twoOrMoreLetters_shouldMultiply70Percent() {
        // given
        GameState gameState = createGameState(3);
        TurnInfo turnInfo = gameState.getCurrentTurn();
        Set<Integer> letterPositions = new HashSet<>();
        letterPositions.add(0);
        letterPositions.add(1);
        turnInfo.setRevealedLetterPositions(letterPositions);

        Player drawer = gameState.getPlayers().get(0);
        Player guesser = gameState.getPlayers().get(1);

        // when
        gameScoringService.handleCorrectAnswer(gameState, guesser);

        // then
        // 1등: 50 * 1.5 (player count) * 1.0 (no chosung) * 0.7 (letters) = 52.5 -> 53
        assertThat(drawer.getScore()).isEqualTo(53);
    }

    @Test
    @Tag("scoring-hint-penalty")
    @DisplayName("calculateHintMultiplier - 초성 3개와 글자 2개면 최소 30% 배율이 보장되어야 한다")
    void calculateHintMultiplier_manyHints_shouldGuaranteeMinimum30Percent() {
        // given
        GameState gameState = createGameState(4); // 출제자 + 3명 = 4명
        TurnInfo turnInfo = gameState.getCurrentTurn();
        Set<Integer> chosungPositions = new HashSet<>();
        chosungPositions.add(0);
        chosungPositions.add(1);
        chosungPositions.add(2);
        Set<Integer> letterPositions = new HashSet<>();
        letterPositions.add(3);
        letterPositions.add(4);
        turnInfo.setRevealedChosungPositions(chosungPositions);
        turnInfo.setRevealedLetterPositions(letterPositions);

        Player drawer = gameState.getPlayers().get(0);
        Player guesser = gameState.getPlayers().get(1);

        // when
        gameScoringService.handleCorrectAnswer(gameState, guesser);

        // then
        // 1등: 50 * 1.5 (player count) * 0.60 (chosung 3) * 0.70 (letter 2) = 31.5 -> 32
        // 힌트 사용으로 조기 정답 패널티 없음
        assertThat(drawer.getScore()).isEqualTo(32);
    }

    @Test
    @Tag("scoring-early-penalty")
    @DisplayName("getEarlyCorrectPenalty - 힌트 사용 시 패널티가 없어야 한다")
    void getEarlyCorrectPenalty_withHints_shouldReturnNoPenalty() {
        // given
        GameState gameState = createGameState(5);
        TurnInfo turnInfo = gameState.getCurrentTurn();
        Set<Integer> chosungPositions = new HashSet<>();
        chosungPositions.add(0);
        turnInfo.setRevealedChosungPositions(chosungPositions);

        Player drawer = gameState.getPlayers().get(0);

        // 4명 중 3명 정답 (75%) - 힌트가 있으므로 패널티 없음
        markPlayersCorrect(gameState, 3);

        // when
        int drawerScoreBefore = drawer.getScore() != null ? drawer.getScore() : 0;
        Player lastGuesser = gameState.getPlayers().get(4);
        gameScoringService.handleCorrectAnswer(gameState, lastGuesser);
        int drawerScoreGain = drawer.getScore() - drawerScoreBefore;

        // then
        // 4등: 35 * 1.5 (4명) * 0.85 (초성 1) * 1.0 (패널티 없음) = 44.625 -> 45
        assertThat(drawerScoreGain).isEqualTo(45);
    }

    @Test
    @Tag("scoring-early-penalty")
    @DisplayName("getEarlyCorrectPenalty - 힌트 없고 정답률 70% 이상이면 30% 패널티가 적용되어야 한다")
    void getEarlyCorrectPenalty_noHints70PercentCorrect_shouldApply30PercentPenalty() {
        // given
        GameState gameState = createGameState(5); // 출제자 + 4명
        Player drawer = gameState.getPlayers().get(0);

        // 4명 중 3명 정답 (75%)
        markPlayersCorrect(gameState, 3);

        // when
        int drawerScoreBefore = drawer.getScore() != null ? drawer.getScore() : 0;
        Player lastGuesser = gameState.getPlayers().get(4);
        gameScoringService.handleCorrectAnswer(gameState, lastGuesser);
        int drawerScoreGain = drawer.getScore() - drawerScoreBefore;

        // then
        // 4등: 35 * 1.5 (4명) * 1.0 (힌트 없음) * 0.70 (패널티) = 36.75 -> 37
        assertThat(drawerScoreGain).isEqualTo(37);
    }

    @Test
    @Tag("scoring-early-penalty")
    @DisplayName("getEarlyCorrectPenalty - 힌트 없고 정답률 50-70%이면 20% 패널티가 적용되어야 한다")
    void getEarlyCorrectPenalty_noHints50to70PercentCorrect_shouldApply20PercentPenalty() {
        // given
        GameState gameState = createGameState(6); // 출제자 + 5명
        Player drawer = gameState.getPlayers().get(0);

        // 5명 중 3명 정답 (60%)
        markPlayersCorrect(gameState, 3);

        // when
        int drawerScoreBefore = drawer.getScore() != null ? drawer.getScore() : 0;
        Player lastGuesser = gameState.getPlayers().get(4);
        gameScoringService.handleCorrectAnswer(gameState, lastGuesser);
        int drawerScoreGain = drawer.getScore() - drawerScoreBefore;

        // then
        // 4등: 35 * 1.2 (5명) * 1.0 (힌트 없음) * 0.70 (패널티) = 29.4 -> 29
        assertThat(drawerScoreGain).isEqualTo(29);
    }

    @Test
    @Tag("scoring-early-penalty")
    @DisplayName("getEarlyCorrectPenalty - 힌트 없고 정답률 30-50%이면 10% 패널티가 적용되어야 한다")
    void getEarlyCorrectPenalty_noHints30to50PercentCorrect_shouldApply10PercentPenalty() {
        // given
        GameState gameState = createGameState(6); // 출제자 + 5명
        Player drawer = gameState.getPlayers().get(0);

        // 5명 중 2명 정답 (40%)
        markPlayersCorrect(gameState, 2);

        // when
        int drawerScoreBefore = drawer.getScore() != null ? drawer.getScore() : 0;
        Player lastGuesser = gameState.getPlayers().get(3);
        gameScoringService.handleCorrectAnswer(gameState, lastGuesser);
        int drawerScoreGain = drawer.getScore() - drawerScoreBefore;

        // then
        // 3등: 40 * 1.2 (5명) * 1.0 (힌트 없음) * 0.80 (패널티) = 38.4 -> 38
        assertThat(drawerScoreGain).isEqualTo(38);
    }

    @Test
    @Tag("scoring-early-penalty")
    @DisplayName("getEarlyCorrectPenalty - 힌트 없고 정답률 30% 미만이면 패널티가 없어야 한다")
    void getEarlyCorrectPenalty_noHintsBelow30PercentCorrect_shouldApplyNoPenalty() {
        // given
        GameState gameState = createGameState(6); // 출제자 + 5명
        Player drawer = gameState.getPlayers().get(0);

        // 5명 중 1명 정답 (20%)
        Player guesser = gameState.getPlayers().get(1);

        // when
        gameScoringService.handleCorrectAnswer(gameState, guesser);

        // then
        // 1등: 50 * 1.2 (5명) * 1.0 (힌트 없음) * 1.0 (패널티 없음) = 60
        assertThat(drawer.getScore()).isEqualTo(60);
    }

    @Test
    @Tag("scoring-integration")
    @DisplayName("awardDrawerBonus - 출제자가 여러 정답자로부터 누적 점수를 받아야 한다")
    void awardDrawerBonus_multipleCorrectPlayers_shouldAccumulateScore() {
        // given
        GameState gameState = createGameState(6); // 출제자 + 5명
        Player drawer = gameState.getPlayers().get(0);

        // when
        gameScoringService.handleCorrectAnswer(gameState, gameState.getPlayers().get(1));
        gameScoringService.handleCorrectAnswer(gameState, gameState.getPlayers().get(2));
        gameScoringService.handleCorrectAnswer(gameState, gameState.getPlayers().get(3));

        // then
        // 1등: 50 * 1.2 * 1.0 (20% 정답률, 패널티 없음) = 60
        // 2등: 45 * 1.2 * 0.90 (40% 정답률, 10% 패널티) = 48.6 -> 49
        // 3등: 40 * 1.2 * 0.80 (60% 정답률, 20% 패널티) = 38.4 -> 38
        // 총합: 60 + 49 + 38 = 147
        assertThat(drawer.getScore()).isEqualTo(147);
    }

    // Helper methods

    /**
     * 테스트용 GameState 생성
     * @param totalPlayers 총 플레이어 수 (출제자 포함)
     */
    private GameState createGameState(int totalPlayers) {
        List<String> turnOrder = new ArrayList<>();
        List<Player> players = new ArrayList<>();

        // 출제자 (첫 번째 플레이어)
        String drawerId = "player1";
        turnOrder.add(drawerId);
        Player drawer = Player.builder()
                .playerId(drawerId)
                .nickname("출제자")
                .score(0)
                .isCorrect(false)
                .build();
        players.add(drawer);

        // 나머지 플레이어들
        for (int i = 2; i <= totalPlayers; i++) {
            String playerId = "player" + i;
            turnOrder.add(playerId);
            Player player = Player.builder()
                    .playerId(playerId)
                    .nickname("플레이어" + i)
                    .score(0)
                    .isCorrect(false)
                    .build();
            players.add(player);
        }

        GameState gameState = new GameState("room1", 3, 90, turnOrder);
        gameState.setPhase(GamePhase.DRAWING);
        gameState.setPlayers(players);

        TurnInfo turnInfo = new TurnInfo(1, drawerId, "출제자");
        turnInfo.setWord("테스트");
        gameState.setCurrentTurn(turnInfo);

        return gameState;
    }

    /**
     * 여러 플레이어를 정답 처리
     */
    private void markPlayersCorrect(GameState gameState, int count) {
        List<Player> nonDrawers = gameState.getPlayers().stream()
                .filter(p -> !p.getPlayerId().equals(gameState.getCurrentTurn().getDrawerId()))
                .toList();

        for (int i = 0; i < Math.min(count, nonDrawers.size()); i++) {
            Player player = nonDrawers.get(i);
            gameScoringService.handleCorrectAnswer(gameState, player);
        }
    }
}
