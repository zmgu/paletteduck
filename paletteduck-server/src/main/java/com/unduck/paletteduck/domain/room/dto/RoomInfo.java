package com.unduck.paletteduck.domain.room.dto;

import com.unduck.paletteduck.domain.game.dto.GameSettings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomInfo {
    private String roomId;
    private String inviteCode;
    private List<RoomPlayer> players;
    private GameSettings settings;
    private RoomStatus status = RoomStatus.WAITING;
}