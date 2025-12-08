package com.unduck.paletteduck.domain.chat.service;

import com.unduck.paletteduck.domain.chat.dto.ChatMessage;
import com.unduck.paletteduck.domain.chat.dto.ChatType;
import com.unduck.paletteduck.domain.game.constants.GameConstants;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ChatMessageFactory {

    /**
     * 플레이어 입장 메시지 생성
     */
    public ChatMessage createPlayerJoinMessage(String nickname) {
        return ChatMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .playerId(GameConstants.SystemPlayer.ID)
                .nickname(GameConstants.SystemPlayer.NAME)
                .message(nickname + "님이 입장했습니다.")
                .type(ChatType.SYSTEM)
                .isCorrect(false)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 플레이어 퇴장 메시지 생성
     */
    public ChatMessage createPlayerLeaveMessage(String nickname) {
        return ChatMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .playerId(GameConstants.SystemPlayer.ID)
                .nickname(GameConstants.SystemPlayer.NAME)
                .message(nickname + "님이 퇴장했습니다.")
                .type(ChatType.SYSTEM)
                .isCorrect(false)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 시스템 메시지 생성
     */
    public ChatMessage createSystemMessage(String message) {
        return ChatMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .playerId(GameConstants.SystemPlayer.ID)
                .nickname(GameConstants.SystemPlayer.NAME)
                .message(message)
                .type(ChatType.SYSTEM)
                .isCorrect(false)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
