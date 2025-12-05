package com.unduck.paletteduck.domain.game.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HintService 테스트")
class HintServiceTest {

    private HintService hintService;

    @BeforeEach
    void setUp() {
        hintService = new HintService();
    }

    @Test
    @Tag("hint-random-reveal")
    @DisplayName("revealRandomChosung - 정상: 공개되지 않은 위치 중 하나를 반환해야 한다")
    void revealRandomChosung_shouldReturnUnrevealedPosition() {
        // given
        String word = "사과";
        Set<Integer> revealedPositions = new HashSet<>();

        // when
        Integer position = hintService.revealRandomChosung(word, revealedPositions);

        // then
        assertThat(position).isNotNull();
        assertThat(position).isBetween(0, word.length() - 1);
    }

    @Test
    @Tag("hint-random-reveal")
    @DisplayName("revealRandomChosung - null 단어인 경우 null을 반환해야 한다")
    void revealRandomChosung_nullWord_shouldReturnNull() {
        // given
        Set<Integer> revealedPositions = new HashSet<>();

        // when
        Integer position = hintService.revealRandomChosung(null, revealedPositions);

        // then
        assertThat(position).isNull();
    }

    @Test
    @Tag("hint-random-reveal")
    @DisplayName("revealRandomChosung - 빈 단어인 경우 null을 반환해야 한다")
    void revealRandomChosung_emptyWord_shouldReturnNull() {
        // given
        String word = "";
        Set<Integer> revealedPositions = new HashSet<>();

        // when
        Integer position = hintService.revealRandomChosung(word, revealedPositions);

        // then
        assertThat(position).isNull();
    }

    @Test
    @Tag("hint-random-reveal")
    @DisplayName("revealRandomChosung - 모든 위치가 공개된 경우 null을 반환해야 한다")
    void revealRandomChosung_allPositionsRevealed_shouldReturnNull() {
        // given
        String word = "사과";
        Set<Integer> revealedPositions = new HashSet<>();
        revealedPositions.add(0);
        revealedPositions.add(1);

        // when
        Integer position = hintService.revealRandomChosung(word, revealedPositions);

        // then
        assertThat(position).isNull();
    }

    @Test
    @Tag("hint-random-reveal")
    @DisplayName("revealRandomChosung - 일부 위치가 공개된 경우 공개되지 않은 위치를 반환해야 한다")
    void revealRandomChosung_somePositionsRevealed_shouldReturnUnrevealedPosition() {
        // given
        String word = "바나나";
        Set<Integer> revealedPositions = new HashSet<>();
        revealedPositions.add(0);
        revealedPositions.add(2);

        // when
        Integer position = hintService.revealRandomChosung(word, revealedPositions);

        // then
        assertThat(position).isNotNull();
        assertThat(position).isIn(1);
    }

    @Test
    @Tag("hint-random-reveal")
    @DisplayName("revealRandomLetter - 정상: 공개되지 않은 위치 중 하나를 반환해야 한다")
    void revealRandomLetter_shouldReturnUnrevealedPosition() {
        // given
        String word = "사과";
        Set<Integer> revealedPositions = new HashSet<>();

        // when
        Integer position = hintService.revealRandomLetter(word, revealedPositions);

        // then
        assertThat(position).isNotNull();
        assertThat(position).isBetween(0, word.length() - 1);
    }

    @Test
    @Tag("hint-random-reveal")
    @DisplayName("revealRandomLetter - null 단어인 경우 null을 반환해야 한다")
    void revealRandomLetter_nullWord_shouldReturnNull() {
        // given
        Set<Integer> revealedPositions = new HashSet<>();

        // when
        Integer position = hintService.revealRandomLetter(null, revealedPositions);

        // then
        assertThat(position).isNull();
    }

    @Test
    @Tag("hint-random-reveal")
    @DisplayName("revealRandomLetter - 빈 단어인 경우 null을 반환해야 한다")
    void revealRandomLetter_emptyWord_shouldReturnNull() {
        // given
        String word = "";
        Set<Integer> revealedPositions = new HashSet<>();

        // when
        Integer position = hintService.revealRandomLetter(word, revealedPositions);

        // then
        assertThat(position).isNull();
    }

    @Test
    @Tag("hint-random-reveal")
    @DisplayName("revealRandomLetter - 최대 공개 개수 도달 시 null을 반환해야 한다")
    void revealRandomLetter_maxRevealsReached_shouldReturnNull() {
        // given
        String word = "사과";
        Set<Integer> revealedPositions = new HashSet<>();
        revealedPositions.add(0); // 최대 1개만 공개 가능 (2-1)

        // when
        Integer position = hintService.revealRandomLetter(word, revealedPositions);

        // then
        assertThat(position).isNull();
    }

    @Test
    @Tag("hint-random-reveal")
    @DisplayName("revealRandomLetter - 단일 문자 단어는 공개할 수 없어야 한다")
    void revealRandomLetter_singleCharWord_shouldReturnNull() {
        // given
        String word = "사";
        Set<Integer> revealedPositions = new HashSet<>();

        // when
        Integer position = hintService.revealRandomLetter(word, revealedPositions);

        // then
        assertThat(position).isNull(); // 1-1 = 0이므로 공개 불가
    }

    @Test
    @Tag("hint-random-reveal")
    @DisplayName("revealRandomLetter - 세 글자 단어는 최대 2개까지만 공개 가능해야 한다")
    void revealRandomLetter_threeCharWord_shouldAllowMaxTwoReveals() {
        // given
        String word = "바나나";
        Set<Integer> revealedPositions = new HashSet<>();
        revealedPositions.add(0);
        revealedPositions.add(1);

        // when
        Integer position = hintService.revealRandomLetter(word, revealedPositions);

        // then
        assertThat(position).isNull(); // 3-1 = 2이므로 더 이상 공개 불가
    }

    @Test
    @Tag("hint-array-generation")
    @DisplayName("generateWordLengthHint - 정상: 단어 길이만큼 밑줄 배열을 반환해야 한다")
    void generateWordLengthHint_shouldReturnUnderscoreArray() {
        // given
        String word = "사과";

        // when
        String[] hint = hintService.generateWordLengthHint(word);

        // then
        assertThat(hint).hasSize(2);
        assertThat(hint).containsExactly("_", "_");
    }

    @Test
    @Tag("hint-array-generation")
    @DisplayName("generateWordLengthHint - null 단어인 경우 빈 배열을 반환해야 한다")
    void generateWordLengthHint_nullWord_shouldReturnEmptyArray() {
        // when
        String[] hint = hintService.generateWordLengthHint(null);

        // then
        assertThat(hint).isEmpty();
    }

    @Test
    @Tag("hint-array-generation")
    @DisplayName("generateWordLengthHint - 빈 단어인 경우 빈 배열을 반환해야 한다")
    void generateWordLengthHint_emptyWord_shouldReturnEmptyArray() {
        // given
        String word = "";

        // when
        String[] hint = hintService.generateWordLengthHint(word);

        // then
        assertThat(hint).isEmpty();
    }

    @Test
    @Tag("hint-array-generation")
    @DisplayName("generateHintArray - 아무것도 공개되지 않은 경우 밑줄만 반환해야 한다")
    void generateHintArray_noReveals_shouldReturnUnderscores() {
        // given
        String word = "사과";
        Set<Integer> revealedChosung = new HashSet<>();
        Set<Integer> revealedLetters = new HashSet<>();

        // when
        String[] hint = hintService.generateHintArray(word, revealedChosung, revealedLetters);

        // then
        assertThat(hint).hasSize(2);
        assertThat(hint).containsExactly("_", "_");
    }

    @Test
    @Tag("hint-array-generation")
    @DisplayName("generateHintArray - 초성만 공개된 경우 초성을 반환해야 한다")
    void generateHintArray_chosungRevealed_shouldReturnChosung() {
        // given
        String word = "사과";
        Set<Integer> revealedChosung = new HashSet<>();
        revealedChosung.add(0); // '사'의 초성 'ㅅ'
        Set<Integer> revealedLetters = new HashSet<>();

        // when
        String[] hint = hintService.generateHintArray(word, revealedChosung, revealedLetters);

        // then
        assertThat(hint).hasSize(2);
        assertThat(hint[0]).isEqualTo("ㅅ");
        assertThat(hint[1]).isEqualTo("_");
    }

    @Test
    @Tag("hint-array-generation")
    @DisplayName("generateHintArray - 글자만 공개된 경우 글자를 반환해야 한다")
    void generateHintArray_letterRevealed_shouldReturnLetter() {
        // given
        String word = "사과";
        Set<Integer> revealedChosung = new HashSet<>();
        Set<Integer> revealedLetters = new HashSet<>();
        revealedLetters.add(1); // '과'

        // when
        String[] hint = hintService.generateHintArray(word, revealedChosung, revealedLetters);

        // then
        assertThat(hint).hasSize(2);
        assertThat(hint[0]).isEqualTo("_");
        assertThat(hint[1]).isEqualTo("과");
    }

    @Test
    @Tag("hint-array-generation")
    @DisplayName("generateHintArray - 글자와 초성이 모두 공개된 경우 글자를 우선해야 한다")
    void generateHintArray_bothRevealed_shouldPrioritizeLetter() {
        // given
        String word = "사과";
        Set<Integer> revealedChosung = new HashSet<>();
        revealedChosung.add(0);
        Set<Integer> revealedLetters = new HashSet<>();
        revealedLetters.add(0); // 같은 위치에 초성과 글자 모두 공개

        // when
        String[] hint = hintService.generateHintArray(word, revealedChosung, revealedLetters);

        // then
        assertThat(hint).hasSize(2);
        assertThat(hint[0]).isEqualTo("사"); // 글자 우선
        assertThat(hint[1]).isEqualTo("_");
    }

    @Test
    @Tag("hint-array-generation")
    @DisplayName("generateHintArray - null 단어인 경우 빈 배열을 반환해야 한다")
    void generateHintArray_nullWord_shouldReturnEmptyArray() {
        // given
        Set<Integer> revealedChosung = new HashSet<>();
        Set<Integer> revealedLetters = new HashSet<>();

        // when
        String[] hint = hintService.generateHintArray(null, revealedChosung, revealedLetters);

        // then
        assertThat(hint).isEmpty();
    }

    @Test
    @Tag("hint-array-generation")
    @DisplayName("generateHintArray - 영문 소문자의 초성은 그대로 표시되어야 한다")
    void generateHintArray_englishLowercase_shouldDisplayAsIs() {
        // given
        String word = "apple";
        Set<Integer> revealedChosung = new HashSet<>();
        revealedChosung.add(0); // 'a'
        Set<Integer> revealedLetters = new HashSet<>();

        // when
        String[] hint = hintService.generateHintArray(word, revealedChosung, revealedLetters);

        // then
        assertThat(hint[0]).isEqualTo("a");
    }

    @Test
    @Tag("hint-array-generation")
    @DisplayName("generateHintArray - 영문 대문자의 초성은 그대로 표시되어야 한다")
    void generateHintArray_englishUppercase_shouldDisplayAsIs() {
        // given
        String word = "Apple";
        Set<Integer> revealedChosung = new HashSet<>();
        revealedChosung.add(0); // 'A'
        Set<Integer> revealedLetters = new HashSet<>();

        // when
        String[] hint = hintService.generateHintArray(word, revealedChosung, revealedLetters);

        // then
        assertThat(hint[0]).isEqualTo("A");
    }

    @Test
    @Tag("hint-array-generation")
    @DisplayName("generateHintArray - 숫자의 초성은 그대로 표시되어야 한다")
    void generateHintArray_number_shouldDisplayAsIs() {
        // given
        String word = "123";
        Set<Integer> revealedChosung = new HashSet<>();
        revealedChosung.add(0); // '1'
        Set<Integer> revealedLetters = new HashSet<>();

        // when
        String[] hint = hintService.generateHintArray(word, revealedChosung, revealedLetters);

        // then
        assertThat(hint[0]).isEqualTo("1");
    }

    @Test
    @Tag("hint-array-generation")
    @DisplayName("generateHintArray - 한글 범위의 여러 글자에서 정확한 초성을 추출해야 한다")
    void generateHintArray_variousKoreanChars_shouldExtractCorrectChosung() {
        // given
        String word = "강남구";
        Set<Integer> revealedChosung = new HashSet<>();
        revealedChosung.add(0); // '강' -> 'ㄱ'
        revealedChosung.add(1); // '남' -> 'ㄴ'
        revealedChosung.add(2); // '구' -> 'ㄱ'
        Set<Integer> revealedLetters = new HashSet<>();

        // when
        String[] hint = hintService.generateHintArray(word, revealedChosung, revealedLetters);

        // then
        assertThat(hint).containsExactly("ㄱ", "ㄴ", "ㄱ");
    }

    @Test
    @Tag("hint-display")
    @DisplayName("generateHintDisplay - 힌트 배열을 공백으로 구분된 문자열로 반환해야 한다")
    void generateHintDisplay_shouldReturnSpaceSeparatedString() {
        // given
        String word = "사과";
        Set<Integer> revealedChosung = new HashSet<>();
        revealedChosung.add(0);
        Set<Integer> revealedLetters = new HashSet<>();

        // when
        String hintDisplay = hintService.generateHintDisplay(word, revealedChosung, revealedLetters);

        // then
        assertThat(hintDisplay).isEqualTo("ㅅ _");
    }
}
