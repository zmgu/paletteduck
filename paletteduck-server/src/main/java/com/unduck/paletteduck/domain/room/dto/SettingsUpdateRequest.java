package com.unduck.paletteduck.domain.room.dto;

import com.unduck.paletteduck.domain.game.dto.GameSettings;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsUpdateRequest {
    @NotNull(message = "설정은 필수입니다")
    @Valid
    private GameSettings settings;
}
