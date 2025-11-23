package com.unduck.paletteduck.domain.chat.dto;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String messageId;
    private String playerId;
    private String nickname;
    private String message;
    private ChatType type = ChatType.NORMAL;
    private Boolean isCorrect;
    private Boolean senderIsCorrect;  // 발신자가 정답을 맞춘 상태인지 여부
    private long timestamp;
}
