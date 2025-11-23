package com.unduck.paletteduck.domain.game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private String playerId;
    private String nickname;
    private Integer score;
    private Boolean isCorrect;
    private Integer totalLikes;      // 전체 추천 수
    private Integer totalDislikes;   // 전체 비추천 수
}
