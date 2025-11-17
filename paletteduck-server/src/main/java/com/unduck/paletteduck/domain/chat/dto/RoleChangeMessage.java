package com.unduck.paletteduck.domain.chat.dto;

import com.unduck.paletteduck.domain.room.dto.PlayerRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RoleChangeMessage {
    private String playerId;
    private PlayerRole newRole;
}
