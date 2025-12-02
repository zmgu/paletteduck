package com.unduck.paletteduck.domain.room.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorMessage {
    private String errorCode;
    private String message;
    private String targetPlayerId;  // 에러 메시지 대상 플레이어 ID

    public ErrorMessage(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
}
