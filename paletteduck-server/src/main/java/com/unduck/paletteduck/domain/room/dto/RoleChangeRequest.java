package com.unduck.paletteduck.domain.room.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleChangeRequest {
    @NotNull(message = "역할은 필수입니다")
    private PlayerRole newRole;
}
