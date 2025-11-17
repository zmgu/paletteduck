package com.unduck.paletteduck.domain.room.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoleChangeRequest {
    private PlayerRole newRole;
}
