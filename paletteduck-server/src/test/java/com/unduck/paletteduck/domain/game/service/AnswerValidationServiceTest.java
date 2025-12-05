package com.unduck.paletteduck.domain.game.service;

import com.unduck.paletteduck.domain.game.dto.GamePhase;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.dto.TurnInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AnswerValidationService 테스트")
class AnswerValidationServiceTest {

    private AnswerValidationService answerValidationService;

    @BeforeEach
    void setUp() {
        answerValidationService = new AnswerValidationService();
    }

    @Test
    @Tag("answer-validation")
    @DisplayName("checkAnswer - 정확한 정답이면 true를 반환해야 한다")
    void checkAnswer_exactMatch_shouldReturnTrue() {
        // given
        GameState gameState = createGameState("사과");
        String userAnswer = "사과";

        // when
        boolean result = answerValidationService.checkAnswer(gameState, userAnswer);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @Tag("answer-validation")
    @DisplayName("checkAnswer - 대소문자가 다른 경우에도 true를 반환해야 한다")
    void checkAnswer_differentCase_shouldReturnTrue() {
        // given
        GameState gameState = createGameState("Apple");
        String userAnswer = "apple";

        // when
        boolean result = answerValidationService.checkAnswer(gameState, userAnswer);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @Tag("answer-validation")
    @DisplayName("checkAnswer - 공백이 포함된 경우 공백을 무시하고 비교해야 한다")
    void checkAnswer_withWhitespace_shouldIgnoreWhitespace() {
        // given
        GameState gameState = createGameState("hello world");
        String userAnswer = "helloworld";

        // when
        boolean result = answerValidationService.checkAnswer(gameState, userAnswer);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @Tag("answer-validation")
    @DisplayName("checkAnswer - 대소문자와 공백이 모두 다른 경우에도 true를 반환해야 한다")
    void checkAnswer_differentCaseAndWhitespace_shouldReturnTrue() {
        // given
        GameState gameState = createGameState("Hello World");
        String userAnswer = "hello world";

        // when
        boolean result = answerValidationService.checkAnswer(gameState, userAnswer);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @Tag("answer-validation")
    @DisplayName("checkAnswer - 틀린 답이면 false를 반환해야 한다")
    void checkAnswer_wrongAnswer_shouldReturnFalse() {
        // given
        GameState gameState = createGameState("사과");
        String userAnswer = "바나나";

        // when
        boolean result = answerValidationService.checkAnswer(gameState, userAnswer);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @Tag("answer-validation")
    @DisplayName("checkAnswer - 부분 정답은 false를 반환해야 한다")
    void checkAnswer_partialMatch_shouldReturnFalse() {
        // given
        GameState gameState = createGameState("사과");
        String userAnswer = "사";

        // when
        boolean result = answerValidationService.checkAnswer(gameState, userAnswer);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @Tag("answer-validation")
    @DisplayName("checkAnswer - currentTurn이 null이면 false를 반환해야 한다")
    void checkAnswer_nullCurrentTurn_shouldReturnFalse() {
        // given
        GameState gameState = new GameState("room1", 3, 90, new ArrayList<>());
        gameState.setCurrentTurn(null);
        String userAnswer = "사과";

        // when
        boolean result = answerValidationService.checkAnswer(gameState, userAnswer);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @Tag("answer-validation")
    @DisplayName("checkAnswer - word가 null이면 false를 반환해야 한다")
    void checkAnswer_nullWord_shouldReturnFalse() {
        // given
        GameState gameState = new GameState("room1", 3, 90, new ArrayList<>());
        TurnInfo turnInfo = new TurnInfo(1, "player1", "출제자");
        turnInfo.setWord(null);
        gameState.setCurrentTurn(turnInfo);
        String userAnswer = "사과";

        // when
        boolean result = answerValidationService.checkAnswer(gameState, userAnswer);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @Tag("answer-validation")
    @DisplayName("checkAnswer - 여러 공백이 포함된 경우 모두 제거하고 비교해야 한다")
    void checkAnswer_multipleSpaces_shouldRemoveAll() {
        // given
        GameState gameState = createGameState("hello   world");
        String userAnswer = "hello world";

        // when
        boolean result = answerValidationService.checkAnswer(gameState, userAnswer);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @Tag("answer-validation")
    @DisplayName("checkAnswer - 탭과 줄바꿈도 공백으로 처리되어야 한다")
    void checkAnswer_tabsAndNewlines_shouldBeTreatedAsWhitespace() {
        // given
        GameState gameState = createGameState("hello\tworld");
        String userAnswer = "hello\nworld";

        // when
        boolean result = answerValidationService.checkAnswer(gameState, userAnswer);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @Tag("answer-validation")
    @DisplayName("checkAnswer - 한글과 영문이 섞인 경우에도 올바르게 처리해야 한다")
    void checkAnswer_mixedKoreanEnglish_shouldWorkCorrectly() {
        // given
        GameState gameState = createGameState("Apple사과");
        String userAnswer = "APPLE사과";

        // when
        boolean result = answerValidationService.checkAnswer(gameState, userAnswer);

        // then
        assertThat(result).isTrue();
    }

    // Helper method
    private GameState createGameState(String word) {
        List<String> turnOrder = new ArrayList<>();
        turnOrder.add("player1");

        GameState gameState = new GameState("room1", 3, 90, turnOrder);
        gameState.setPhase(GamePhase.DRAWING);

        TurnInfo turnInfo = new TurnInfo(1, "player1", "출제자");
        turnInfo.setWord(word);
        gameState.setCurrentTurn(turnInfo);

        return gameState;
    }
}
