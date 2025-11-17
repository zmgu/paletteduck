package com.unduck.paletteduck.domain.game.dto;

import com.unduck.paletteduck.domain.room.constant.RoomConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameSettings {
    private int maxPlayers = RoomConstants.DEFAULT_MAX_PLAYERS;
    private int rounds = RoomConstants.DEFAULT_ROUNDS;
    private int wordChoices = RoomConstants.DEFAULT_WORD_CHOICES;
    private GameMode mode = GameMode.NORMAL;
    private int drawTime = RoomConstants.DEFAULT_DRAW_TIME;
    private int maxSpectators = RoomConstants.MAX_SPECTATORS;
}
