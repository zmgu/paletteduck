package com.unduck.paletteduck.domain.game.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TurnInfo {
    private int turnNumber;                 // 현재 턴 (1부터 시작)
    private String drawerId;                // 출제자 playerId
    private String drawerNickname;          // 출제자 닉네임
    private String word;                    // 정답 단어 (선택 전 null)
    private List<String> wordChoices;       // 단어 선택지 (출제자만 볼 수 있음)
    private int timeLeft;                   // 남은 시간 (초)
    private List<String> correctPlayerIds;  // 정답 맞춘 playerId 목록
    private int hintLevel;                  // 현재 힌트 레벨 (0: 없음, 1: 글자수, 2: 초성)
    private String currentHint;             // 현재 힌트 메시지 (레거시)
    private String[] hintArray;             // 힌트 배열 (각 위치에 글자, 초성, 또는 "_")
    private Set<Integer> revealedChosungPositions;  // 공개된 초성 위치 (인덱스)
    private Set<Integer> revealedLetterPositions;   // 공개된 글자 위치 (인덱스)
    private Map<String, VoteType> votes;    // 투표 정보 (voterId -> VoteType)
    private Map<String, Integer> turnScores;  // 이번 턴에서 획득한 점수 (playerId -> score)

    public TurnInfo(int turnNumber, String drawerId, String drawerNickname) {
        this.turnNumber = turnNumber;
        this.drawerId = drawerId;
        this.drawerNickname = drawerNickname;
        this.correctPlayerIds = new ArrayList<>();
        this.wordChoices = new ArrayList<>();
        this.hintLevel = 0;
        this.currentHint = null;
        this.hintArray = null;
        this.revealedChosungPositions = new HashSet<>();
        this.revealedLetterPositions = new HashSet<>();
        this.votes = new HashMap<>();
        this.turnScores = new HashMap<>();
    }
}