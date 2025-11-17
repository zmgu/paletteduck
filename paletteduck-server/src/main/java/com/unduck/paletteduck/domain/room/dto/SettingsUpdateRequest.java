package com.unduck.paletteduck.domain.room.dto;

import com.unduck.paletteduck.domain.game.dto.GameSettings;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SettingsUpdateRequest {
    private GameSettings settings;
}
