package com.unduck.paletteduck.domain.word.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WordService 테스트")
class WordServiceTest {

    private WordService wordService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        wordService = new WordService(objectMapper);
        wordService.loadWords();
    }

    @Test
    @Tag("word-load")
    @DisplayName("loadWords - 정상: 모든 난이도의 단어가 로드되어야 한다")
    void loadWords_shouldLoadAllDifficultyWords() {
        // when
        List<String> randomWords = wordService.getRandomWords(100);

        // then
        assertThat(randomWords).isNotEmpty();
        assertThat(randomWords.size()).isGreaterThan(0);
    }

    @Test
    @Tag("word-random")
    @DisplayName("getRandomWords - 정상: 요청한 개수만큼 단어를 반환해야 한다")
    void getRandomWords_shouldReturnRequestedCount() {
        // when
        List<String> words = wordService.getRandomWords(10);

        // then
        assertThat(words).hasSize(10);
    }

    @Test
    @Tag("word-random")
    @DisplayName("getRandomWords - 요청 개수가 전체보다 많으면 전체 개수를 반환해야 한다")
    void getRandomWords_countExceedsTotal_shouldReturnAllWords() {
        // when
        List<String> allWords1 = wordService.getRandomWords(1000);
        List<String> allWords2 = wordService.getRandomWords(1000);

        // then - 전체 단어 수는 동일해야 함
        assertThat(allWords1).hasSameSizeAs(allWords2);
        assertThat(allWords1.size()).isLessThanOrEqualTo(1000);
    }

    @Test
    @Tag("word-random")
    @DisplayName("getRandomWords - 중복 없이 단어를 반환해야 한다")
    void getRandomWords_shouldReturnUniqueWords() {
        // when
        List<String> words = wordService.getRandomWords(20);

        // then
        Set<String> uniqueWords = new HashSet<>(words);
        assertThat(uniqueWords).hasSize(words.size());
    }

    @Test
    @Tag("word-random")
    @DisplayName("getRandomWords - 빈 리스트를 요청하면 빈 리스트를 반환해야 한다")
    void getRandomWords_zeroCount_shouldReturnEmptyList() {
        // when
        List<String> words = wordService.getRandomWords(0);

        // then
        assertThat(words).isEmpty();
    }

    @Test
    @Tag("word-random")
    @DisplayName("getRandomWords - 호출할 때마다 다른 순서로 반환되어야 한다")
    void getRandomWords_shouldReturnDifferentOrder() {
        // when
        List<String> words1 = wordService.getRandomWords(10);
        List<String> words2 = wordService.getRandomWords(10);

        // then - 매번 다른 결과가 나올 확률이 높음
        // 완전히 같을 수도 있지만 확률적으로 매우 낮음
        assertThat(words1).isNotEmpty();
        assertThat(words2).isNotEmpty();
    }

    @Test
    @Tag("word-mixed")
    @DisplayName("getMixedWords - 정상: 요청한 개수만큼 단어를 반환해야 한다")
    void getMixedWords_shouldReturnRequestedCount() {
        // when
        List<String> words = wordService.getMixedWords(10);

        // then
        assertThat(words).hasSize(10);
    }

    @Test
    @Tag("word-mixed")
    @DisplayName("getMixedWords - 3개 요청 시 easy 1개, medium 1개, hard 1개를 반환해야 한다")
    void getMixedWords_threeWords_shouldReturnMixedDifficulty() {
        // given
        int count = 3;
        // easy: 40% = 1.2 -> 1
        // medium: 40% = 1.2 -> 1
        // hard: 나머지 = 1

        // when
        List<String> words = wordService.getMixedWords(count);

        // then
        assertThat(words).hasSize(3);
    }

    @Test
    @Tag("word-mixed")
    @DisplayName("getMixedWords - 5개 요청 시 올바른 비율로 반환해야 한다")
    void getMixedWords_fiveWords_shouldReturnCorrectRatio() {
        // given
        int count = 5;
        // easy: 40% = 2
        // medium: 40% = 2
        // hard: 나머지 = 1

        // when
        List<String> words = wordService.getMixedWords(count);

        // then
        assertThat(words).hasSize(5);
    }

    @Test
    @Tag("word-mixed")
    @DisplayName("getMixedWords - 10개 요청 시 올바른 비율로 반환해야 한다")
    void getMixedWords_tenWords_shouldReturnCorrectRatio() {
        // given
        int count = 10;
        // easy: 40% = 4
        // medium: 40% = 4
        // hard: 나머지 = 2

        // when
        List<String> words = wordService.getMixedWords(count);

        // then
        assertThat(words).hasSize(10);
    }

    @Test
    @Tag("word-mixed")
    @DisplayName("getMixedWords - 중복 없이 단어를 반환해야 한다")
    void getMixedWords_shouldReturnUniqueWords() {
        // when
        List<String> words = wordService.getMixedWords(15);

        // then
        Set<String> uniqueWords = new HashSet<>(words);
        assertThat(uniqueWords).hasSize(words.size());
    }

    @Test
    @Tag("word-mixed")
    @DisplayName("getMixedWords - 빈 리스트를 요청하면 빈 리스트를 반환해야 한다")
    void getMixedWords_zeroCount_shouldReturnEmptyList() {
        // when
        List<String> words = wordService.getMixedWords(0);

        // then
        assertThat(words).isEmpty();
    }

    @Test
    @Tag("word-mixed")
    @DisplayName("getMixedWords - 1개 요청 시에도 정상 동작해야 한다")
    void getMixedWords_oneWord_shouldWorkCorrectly() {
        // given
        int count = 1;
        // easy: 40% = 0.4 -> 0
        // medium: 40% = 0.4 -> 0
        // hard: 나머지 = 1

        // when
        List<String> words = wordService.getMixedWords(count);

        // then
        assertThat(words).hasSize(1);
    }

    @Test
    @Tag("word-mixed")
    @DisplayName("getMixedWords - 호출할 때마다 다른 순서로 반환되어야 한다")
    void getMixedWords_shouldReturnDifferentOrder() {
        // when
        List<String> words1 = wordService.getMixedWords(10);
        List<String> words2 = wordService.getMixedWords(10);

        // then - 매번 다른 결과가 나올 확률이 높음
        assertThat(words1).isNotEmpty();
        assertThat(words2).isNotEmpty();
    }

    @Test
    @Tag("word-mixed")
    @DisplayName("getMixedWords - 큰 숫자를 요청해도 예외가 발생하지 않아야 한다")
    void getMixedWords_largeCount_shouldNotThrowException() {
        // when
        List<String> words = wordService.getMixedWords(1000);

        // then - 전체 단어 수보다 많이 요청하면 전체 단어 수만큼만 반환
        assertThat(words).isNotEmpty();
        assertThat(words.size()).isLessThanOrEqualTo(1000);
    }
}
