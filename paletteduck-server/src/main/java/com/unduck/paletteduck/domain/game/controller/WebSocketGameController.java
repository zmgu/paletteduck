package com.unduck.paletteduck.domain.game.controller;

import com.unduck.paletteduck.domain.chat.dto.ChatMessage;
import com.unduck.paletteduck.domain.chat.dto.ChatType;
import com.unduck.paletteduck.domain.game.dto.DrawData;
import com.unduck.paletteduck.domain.game.dto.GamePhase;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.dto.Player;
import com.unduck.paletteduck.domain.game.service.GameService;
import com.unduck.paletteduck.domain.game.service.GameTimerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketGameController {

    private final GameService gameService;
    private final GameTimerService gameTimerService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/room/{roomId}/game/word/select")
    public void selectWord(@DestinationVariable String roomId,
                           @Payload Map<String, String> payload) {
        String playerId = payload.get("playerId");
        String word = payload.get("word");

        gameTimerService.selectWord(roomId, playerId, word);
        log.info("Word selection - room: {}, playerId: {}", roomId, playerId);
    }

    @MessageMapping("/room/{roomId}/game/draw")
    public void drawPath(@DestinationVariable String roomId, @Payload DrawData drawData) {
        GameState gameState = gameService.getGameState(roomId);
        if (gameState == null || gameState.getPhase() != GamePhase.DRAWING) {
            return;
        }

        if (!gameState.getCurrentTurn().getDrawerId().equals(drawData.getPlayerId())) {
            log.warn("Unauthorized draw attempt - playerId: {}", drawData.getPlayerId());
            return;
        }

        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/game/draw", drawData);
    }

    @MessageMapping("/room/{roomId}/game/clear")
    public void clearCanvas(@DestinationVariable String roomId, @Payload String playerId) {
        GameState gameState = gameService.getGameState(roomId);
        if (gameState == null || gameState.getPhase() != GamePhase.DRAWING) {
            return;
        }

        if (!gameState.getCurrentTurn().getDrawerId().equals(playerId)) {
            return;
        }

        // 객체로 감싸서 전송
        Map<String, String> clearMessage = Map.of("playerId", playerId);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/game/clear", clearMessage);
    }

    @MessageMapping("/room/{roomId}/game/drawing")
    public void streamDrawing(@DestinationVariable String roomId, @Payload Map<String, Object> data) {
        GameState gameState = gameService.getGameState(roomId);
        if (gameState == null || gameState.getPhase() != GamePhase.DRAWING) {
            return;
        }

        String playerId = (String) data.get("playerId");
        if (!gameState.getCurrentTurn().getDrawerId().equals(playerId)) {
            return;
        }

        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/game/drawing", data);
    }

    @MessageMapping("/room/{roomId}/game/chat")
    public void handleChat(@DestinationVariable String roomId, @Payload Map<String, Object> data) {
        log.info("=== Chat message received ===");
        log.info("RoomId: {}", roomId);

        String playerId = (String) data.get("playerId");
        String playerName = (String) data.get("playerName");
        String message = (String) data.get("message");

        GameState gameState = gameService.getGameState(roomId);
        if (gameState == null) {
            log.error("Game state not found for room: {}", roomId);
            return;
        }

        // 출제자는 채팅 불가
        if (gameState.getCurrentTurn() != null &&
                gameState.getCurrentTurn().getDrawerId().equals(playerId)) {
            log.warn("Drawer cannot send chat messages");
            return;
        }

        // 이미 정답 맞춘 사람은 채팅 불가
        Player player = gameState.getPlayers() != null ?
                gameState.getPlayers().stream()
                        .filter(p -> p.getPlayerId().equals(playerId))
                        .findFirst()
                        .orElse(null) : null;

        if (player != null && Boolean.TRUE.equals(player.getIsCorrect())) {
            log.warn("Player already guessed correctly");
            return;
        }

        // 정답 체크
        boolean isCorrect = checkAnswer(gameState, message);

        if (isCorrect) {
            // 정답 처리
            handleCorrectAnswer(roomId, gameState, player, playerName);

            // 정답 메시지 (본인에게)
            ChatMessage correctMsg = ChatMessage.builder()
                    .messageId(java.util.UUID.randomUUID().toString())
                    .playerId(playerId)
                    .nickname(playerName)
                    .message("🎉 정답입니다!")
                    .timestamp(System.currentTimeMillis())
                    .type(ChatType.CORRECT)
                    .isCorrect(true)
                    .build();

            log.info("=== Sending correct message ===");  // ✅ 로그 추가
            log.info("Message: {}", correctMsg);
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/chat", correctMsg);

            // 정답 공지 (다른 사람들에게)
            ChatMessage announceMsg = ChatMessage.builder()
                    .messageId(java.util.UUID.randomUUID().toString())
                    .playerId("system")
                    .nickname("System")
                    .message(playerName + "님이 정답을 맞췄습니다!")
                    .timestamp(System.currentTimeMillis())
                    .type(ChatType.SYSTEM)
                    .isCorrect(false)
                    .build();

            log.info("=== Sending announce message ===");  // ✅ 로그 추가
            log.info("Message: {}", announceMsg);
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/chat", announceMsg);

        } else {
            // 일반 채팅 메시지
            ChatMessage chatMsg = ChatMessage.builder()
                    .messageId(java.util.UUID.randomUUID().toString())
                    .playerId(playerId)
                    .nickname(playerName)
                    .message(message)
                    .timestamp(System.currentTimeMillis())
                    .type(ChatType.NORMAL)
                    .isCorrect(false)
                    .build();
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/chat", chatMsg);
        }
    }

    private boolean checkAnswer(GameState gameState, String message) {
        if (gameState.getCurrentTurn() == null || gameState.getCurrentTurn().getWord() == null) {
            return false;
        }

        String correctAnswer = gameState.getCurrentTurn().getWord();
        String userAnswer = message;

        // 대소문자 무시, 공백 제거
        correctAnswer = correctAnswer.toLowerCase().replaceAll("\\s+", "");
        userAnswer = userAnswer.toLowerCase().replaceAll("\\s+", "");

        return correctAnswer.equals(userAnswer);
    }

    private void handleCorrectAnswer(String roomId, GameState gameState, Player player, String playerName) {
        if (player == null) {
            log.error("Player not found in game state");
            return;
        }

        // 플레이어 정답 여부 업데이트
        player.setIsCorrect(true);

        // 점수 계산 (정답 맞춘 순서)
        long correctCount = gameState.getPlayers().stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsCorrect()))
                .count();

        int earnedScore = 0;
        if (correctCount == 1) {
            earnedScore = 300; // 1등
        } else if (correctCount == 2) {
            earnedScore = 200; // 2등
        } else if (correctCount == 3) {
            earnedScore = 100; // 3등
        }

        player.setScore((player.getScore() != null ? player.getScore() : 0) + earnedScore);

        // 출제자 점수 (정답 맞춘 사람당 50점)
        String drawerId = gameState.getCurrentTurn().getDrawerId();
        Player drawer = gameState.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(drawerId))
                .findFirst()
                .orElse(null);
        if (drawer != null) {
            drawer.setScore((drawer.getScore() != null ? drawer.getScore() : 0) + 50);
        }

        // 게임 상태 업데이트
        gameService.updateGameState(roomId, gameState);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/game/state", gameState);

        log.info("Player {} guessed correctly. Score: {}", playerName, player.getScore());
    }
}