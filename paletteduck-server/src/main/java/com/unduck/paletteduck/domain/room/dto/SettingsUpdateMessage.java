package com.unduck.paletteduck.domain.room.dto;

import com.unduck.paletteduck.domain.game.dto.GameSettings;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SettingsUpdateMessage {
    private String playerId;
    private GameSettings settings;
}