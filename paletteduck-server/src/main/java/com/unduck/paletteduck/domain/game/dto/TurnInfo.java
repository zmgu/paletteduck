package com.unduck.paletteduck.domain.game.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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

    public TurnInfo(int turnNumber, String drawerId, String drawerNickname) {
        this.turnNumber = turnNumber;
        this.drawerId = drawerId;
        this.drawerNickname = drawerNickname;
        this.correctPlayerIds = new ArrayList<>();
        this.wordChoices = new ArrayList<>();
    }
}