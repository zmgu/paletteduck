package com.unduck.paletteduck.domain.room.dto;

import com.unduck.paletteduck.domain.game.dto.GameSettings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsUpdateRequest {
    private GameSettings settings;
}
