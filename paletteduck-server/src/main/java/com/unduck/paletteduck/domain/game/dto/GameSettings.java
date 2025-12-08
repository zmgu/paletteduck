package com.unduck.paletteduck.domain.game.dto;

import com.unduck.paletteduck.domain.room.constants.RoomConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameSettings {
    @Min(value = RoomConstants.MIN_PLAYERS, message = "최대 참가자는 최소 2명이어야 합니다")
    @Max(value = RoomConstants.MAX_PLAYERS, message = "최대 참가자는 최대 20명이어야 합니다")
    private int maxPlayers = RoomConstants.DEFAULT_MAX_PLAYERS;

    @Min(value = RoomConstants.MIN_ROUNDS, message = "라운드는 최소 2회여야 합니다")
    @Max(value = RoomConstants.MAX_ROUNDS, message = "라운드는 최대 10회여야 합니다")
    private int rounds = RoomConstants.DEFAULT_ROUNDS;

    @Min(value = RoomConstants.MIN_WORD_CHOICES, message = "단어 선택지는 최소 2개여야 합니다")
    @Max(value = RoomConstants.MAX_WORD_CHOICES, message = "단어 선택지는 최대 4개여야 합니다")
    private int wordChoices = RoomConstants.DEFAULT_WORD_CHOICES;

    @Min(value = RoomConstants.MIN_DRAW_TIME, message = "그리기 시간은 최소 30초여야 합니다")
    @Max(value = RoomConstants.MAX_DRAW_TIME, message = "그리기 시간은 최대 240초여야 합니다")
    private int drawTime = RoomConstants.DEFAULT_DRAW_TIME;

    @Min(value = 0, message = "최대 관전자는 0명 이상이어야 합니다")
    @Max(value = RoomConstants.MAX_SPECTATORS, message = "최대 관전자는 최대 20명이어야 합니다")
    private int maxSpectators = RoomConstants.MAX_SPECTATORS;
}
