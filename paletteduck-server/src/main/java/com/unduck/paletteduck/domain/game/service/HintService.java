package com.unduck.paletteduck.domain.game.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
public class HintService {

    // 한글 초성 배열
    private static final String[] CHOSUNG = {
        "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ",
        "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
    };

    private final Random random = new Random();

    /**
     * 랜덤 초성 위치를 공개합니다
     * @param word 정답 단어
     * @param revealedPositions 이미 공개된 초성 위치
     * @return 공개된 위치 (null이면 더 이상 공개할 위치가 없음)
     */
    public Integer revealRandomChosung(String word, Set<Integer> revealedPositions) {
        if (word == null || word.isEmpty()) {
            return null;
        }

        // 아직 공개되지 않은 위치 찾기
        List<Integer> availablePositions = new ArrayList<>();
        for (int i = 0; i < word.length(); i++) {
            if (!revealedPositions.contains(i)) {
                availablePositions.add(i);
            }
        }

        // 공개할 위치가 없으면 null 반환
        if (availablePositions.isEmpty()) {
            return null;
        }

        // 랜덤 위치 선택
        int randomIndex = random.nextInt(availablePositions.size());
        return availablePositions.get(randomIndex);
    }

    /**
     * 랜덤 글자 위치를 공개합니다 (최대 글자수-1까지)
     * @param word 정답 단어
     * @param revealedPositions 이미 공개된 글자 위치
     * @return 공개된 위치 (null이면 더 이상 공개할 수 없음)
     */
    public Integer revealRandomLetter(String word, Set<Integer> revealedPositions) {
        if (word == null || word.isEmpty()) {
            return null;
        }

        // 최대 공개 가능 개수는 글자수-1
        int maxReveals = word.length() - 1;
        if (revealedPositions.size() >= maxReveals) {
            return null; // 더 이상 공개할 수 없음
        }

        // 아직 공개되지 않은 위치 찾기
        List<Integer> availablePositions = new ArrayList<>();
        for (int i = 0; i < word.length(); i++) {
            if (!revealedPositions.contains(i)) {
                availablePositions.add(i);
            }
        }

        // 공개할 위치가 없으면 null 반환
        if (availablePositions.isEmpty()) {
            return null;
        }

        // 랜덤 위치 선택
        int randomIndex = random.nextInt(availablePositions.size());
        return availablePositions.get(randomIndex);
    }

    /**
     * 글자수 힌트를 생성합니다
     * @param word 정답 단어
     * @return 글자수 힌트 배열
     */
    public String[] generateWordLengthHint(String word) {
        if (word == null || word.isEmpty()) {
            return new String[0];
        }

        String[] hint = new String[word.length()];
        for (int i = 0; i < word.length(); i++) {
            hint[i] = "_";
        }
        return hint;
    }

    /**
     * 현재 공개된 정보를 바탕으로 힌트 배열을 생성합니다
     * @param word 정답 단어
     * @param revealedChosungPositions 공개된 초성 위치
     * @param revealedLetterPositions 공개된 글자 위치
     * @return 힌트 배열 (각 위치에 글자, 초성, 또는 "_")
     */
    public String[] generateHintArray(String word, Set<Integer> revealedChosungPositions, Set<Integer> revealedLetterPositions) {
        if (word == null || word.isEmpty()) {
            return new String[0];
        }

        String[] hint = new String[word.length()];

        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);

            // 글자가 공개된 경우
            if (revealedLetterPositions.contains(i)) {
                hint[i] = String.valueOf(ch);
            }
            // 초성이 공개된 경우
            else if (revealedChosungPositions.contains(i)) {
                hint[i] = extractChosung(ch);
            }
            // 아직 공개되지 않은 경우
            else {
                hint[i] = "_";
            }
        }

        return hint;
    }

    /**
     * 현재 공개된 정보를 바탕으로 힌트 문자열을 생성합니다 (레거시)
     * @param word 정답 단어
     * @param revealedChosungPositions 공개된 초성 위치
     * @param revealedLetterPositions 공개된 글자 위치
     * @return 힌트 문자열
     */
    public String generateHintDisplay(String word, Set<Integer> revealedChosungPositions, Set<Integer> revealedLetterPositions) {
        String[] hintArray = generateHintArray(word, revealedChosungPositions, revealedLetterPositions);
        return String.join(" ", hintArray);
    }

    /**
     * 문자에서 초성을 추출합니다
     */
    private String extractChosung(char ch) {
        if (ch >= '가' && ch <= '힣') {
            // 한글인 경우 초성 추출
            int unicode = ch - '가';
            int chosungIndex = unicode / (21 * 28);
            return CHOSUNG[chosungIndex];
        } else if (ch >= 'a' && ch <= 'z') {
            // 영문 소문자인 경우 그대로 표시
            return String.valueOf(ch);
        } else if (ch >= 'A' && ch <= 'Z') {
            // 영문 대문자인 경우 그대로 표시
            return String.valueOf(ch);
        } else if (ch >= '0' && ch <= '9') {
            // 숫자인 경우 그대로 표시
            return String.valueOf(ch);
        } else {
            // 그 외 문자는 '_'로 표시
            return "_";
        }
    }
}
