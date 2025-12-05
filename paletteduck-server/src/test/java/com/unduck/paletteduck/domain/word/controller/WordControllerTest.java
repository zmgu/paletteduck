package com.unduck.paletteduck.domain.word.controller;

import com.unduck.paletteduck.domain.word.service.WordService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("WordController 테스트")
@WebMvcTest(WordController.class)
class WordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WordService wordService;

    @Test
    @Tag("api-word")
    @DisplayName("getRandomWords - 정상: 랜덤 단어 목록을 반환해야 한다")
    void getRandomWords_shouldReturnRandomWords() throws Exception {
        // given
        List<String> words = Arrays.asList("사과", "바나나", "포도");
        when(wordService.getMixedWords(3)).thenReturn(words);

        // when & then
        mockMvc.perform(get("/api/word/random")
                        .param("count", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("사과"))
                .andExpect(jsonPath("$[1]").value("바나나"))
                .andExpect(jsonPath("$[2]").value("포도"));

        verify(wordService).getMixedWords(3);
    }

    @Test
    @Tag("api-word")
    @DisplayName("getRandomWords - count 파라미터가 없으면 기본값 3을 사용해야 한다")
    void getRandomWords_noCountParam_shouldUseDefaultValue() throws Exception {
        // given
        List<String> words = Arrays.asList("사과", "바나나", "포도");
        when(wordService.getMixedWords(3)).thenReturn(words);

        // when & then
        mockMvc.perform(get("/api/word/random"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));

        verify(wordService).getMixedWords(3);
    }

    @Test
    @Tag("api-word")
    @DisplayName("getRandomWords - count 파라미터가 다른 값일 때 해당 개수만큼 반환해야 한다")
    void getRandomWords_customCount_shouldReturnRequestedCount() throws Exception {
        // given
        List<String> words = Arrays.asList("사과", "바나나", "포도", "딸기", "수박");
        when(wordService.getMixedWords(5)).thenReturn(words);

        // when & then
        mockMvc.perform(get("/api/word/random")
                        .param("count", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5));

        verify(wordService).getMixedWords(5);
    }

    @Test
    @Tag("api-word")
    @DisplayName("getRandomWords - count가 0일 때 빈 배열을 반환해야 한다")
    void getRandomWords_zeroCount_shouldReturnEmptyArray() throws Exception {
        // given
        List<String> words = Arrays.asList();
        when(wordService.getMixedWords(0)).thenReturn(words);

        // when & then
        mockMvc.perform(get("/api/word/random")
                        .param("count", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(wordService).getMixedWords(0);
    }

    @Test
    @Tag("api-word")
    @DisplayName("getRandomWords - count가 큰 값일 때도 정상 처리되어야 한다")
    void getRandomWords_largeCount_shouldWorkCorrectly() throws Exception {
        // given
        List<String> words = Arrays.asList("사과", "바나나", "포도");
        when(wordService.getMixedWords(100)).thenReturn(words);

        // when & then
        mockMvc.perform(get("/api/word/random")
                        .param("count", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(wordService).getMixedWords(100);
    }
}
