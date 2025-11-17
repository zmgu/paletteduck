package com.unduck.paletteduck.domain.game.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameSettings {
    private int maxPlayers = 10;
    private int rounds = 3;
    private int wordChoices = 3;
    private GameMode mode = GameMode.NORMAL;
    private int drawTime = 60;
    private int maxSpectators = 20;
}
