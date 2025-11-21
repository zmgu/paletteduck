package com.unduck.paletteduck.domain.game.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TurnInfo {
    private Integer turnNumber;
    private String drawerId;
    private String drawerName;
    private String word;
    private List<String> wordChoices;
    private Integer timeLeft;
    private Integer revealedHints;  // ✅ 추가: 공개된 힌트 개수 (0 ~ 단어길이)

    public TurnInfo(Integer turnNumber, String drawerId, String drawerName) {
        this.turnNumber = turnNumber;
        this.drawerId = drawerId;
        this.drawerName = drawerName;
        this.revealedHints = 0;  // ✅ 초기값
    }
}