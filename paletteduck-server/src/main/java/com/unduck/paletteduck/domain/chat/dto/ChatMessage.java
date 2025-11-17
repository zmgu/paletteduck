package com.unduck.paletteduck.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String playerId;
    private String nickname;
    private String message;
    private ChatType type = ChatType.NORMAL;
    private long timestamp;
}
