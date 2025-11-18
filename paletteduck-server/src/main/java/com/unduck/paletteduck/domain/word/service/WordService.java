package com.unduck.paletteduck.domain.word.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordService {

    private final ObjectMapper objectMapper;
    private Map<String, List<String>> wordsByDifficulty;
    private final Random random = new Random();

    @PostConstruct
    public void loadWords() {
        try {
            ClassPathResource resource = new ClassPathResource("words.json");
            wordsByDifficulty = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<Map<String, List<String>>>() {}
            );
            log.info("Words loaded - Easy: {}, Medium: {}, Hard: {}",
                    wordsByDifficulty.get("easy").size(),
                    wordsByDifficulty.get("medium").size(),
                    wordsByDifficulty.get("hard").size());
        } catch (IOException e) {
            log.error("Failed to load words.json", e);
            throw new RuntimeException("Failed to load words", e);
        }
    }

    public List<String> getRandomWords(int count) {
        List<String> allWords = new ArrayList<>();
        allWords.addAll(wordsByDifficulty.get("easy"));
        allWords.addAll(wordsByDifficulty.get("medium"));
        allWords.addAll(wordsByDifficulty.get("hard"));

        Collections.shuffle(allWords, random);
        return allWords.subList(0, Math.min(count, allWords.size()));
    }

    public List<String> getMixedWords(int count) {
        int easyCount = (int) (count * 0.4);
        int mediumCount = (int) (count * 0.4);
        int hardCount = count - easyCount - mediumCount;

        List<String> result = new ArrayList<>();
        result.addAll(getRandomByDifficulty("easy", easyCount));
        result.addAll(getRandomByDifficulty("medium", mediumCount));
        result.addAll(getRandomByDifficulty("hard", hardCount));

        Collections.shuffle(result, random);
        return result;
    }

    private List<String> getRandomByDifficulty(String difficulty, int count) {
        List<String> words = new ArrayList<>(wordsByDifficulty.get(difficulty));
        Collections.shuffle(words, random);
        return words.subList(0, Math.min(count, words.size()));
    }
}