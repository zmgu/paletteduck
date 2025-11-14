package com.unduck.paletteduck.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomCreateResponse {
    private String roomId;
    private String inviteCode;
}