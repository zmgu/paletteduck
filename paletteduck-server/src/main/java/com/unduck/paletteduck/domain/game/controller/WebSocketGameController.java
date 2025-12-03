package com.unduck.paletteduck.domain.game.controller;

import com.unduck.paletteduck.config.constants.WebSocketTopics;
import com.unduck.paletteduck.domain.chat.constants.ChatConstants;
import com.unduck.paletteduck.domain.chat.dto.ChatMessage;
import com.unduck.paletteduck.domain.chat.dto.ChatType;
import com.unduck.paletteduck.domain.game.constants.GameConstants;
import com.unduck.paletteduck.domain.game.dto.DrawData;
import com.unduck.paletteduck.domain.game.dto.GamePhase;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.dto.Player;
import com.unduck.paletteduck.domain.game.dto.TurnInfo;
import com.unduck.paletteduck.domain.game.dto.VoteType;
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
    private final com.unduck.paletteduck.domain.game.service.AsyncGameTimerScheduler asyncGameTimerScheduler;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.unduck.paletteduck.domain.room.service.RoomService roomService;
    private final com.unduck.paletteduck.domain.game.service.GameScoringService gameScoringService;

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

        messagingTemplate.convertAndSend(WebSocketTopics.gameDraw(roomId), drawData);
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
        messagingTemplate.convertAndSend(WebSocketTopics.gameClear(roomId), clearMessage);
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

        // 그림 이벤트 저장 (도중 참가자를 위해)
        if (gameState.getCurrentTurn() != null && gameState.getCurrentTurn().getDrawingEvents() != null) {
            gameState.getCurrentTurn().getDrawingEvents().add(data);

            // ✅ Throttle 저장: 20개마다 한 번씩 Redis에 저장 (성능과 도중 참가 지원 균형)
            int eventsCount = gameState.getCurrentTurn().getDrawingEvents().size();
            if (eventsCount % 20 == 0) {
                gameService.updateGameState(roomId, gameState);
                log.debug("Drawing events saved to Redis - room: {}, events: {}", roomId, eventsCount);
            }
        }

        // 모든 클라이언트에게 실시간 브로드캐스트
        messagingTemplate.convertAndSend(WebSocketTopics.gameDrawing(roomId), data);
    }

    @MessageMapping("/room/{roomId}/game/chat")
    public void handleChat(@DestinationVariable String roomId, @Payload Map<String, Object> data) {

        String playerId = (String) data.get("playerId");
        String nickname = (String) data.get("nickname");
        String message = (String) data.get("message");

        GameState gameState = gameService.getGameState(roomId);
        if (gameState == null) {
            log.error("Game state not found for room: {}", roomId);
            return;
        }

        // 관전자는 채팅 불가
        com.unduck.paletteduck.domain.room.dto.RoomInfo roomInfo = roomService.getRoomInfo(roomId);
        if (roomInfo != null) {
            com.unduck.paletteduck.domain.room.dto.RoomPlayer roomPlayer = roomInfo.getPlayers().stream()
                    .filter(p -> p.getPlayerId().equals(playerId))
                    .findFirst()
                    .orElse(null);
            if (roomPlayer != null && roomPlayer.getRole() == com.unduck.paletteduck.domain.room.dto.PlayerRole.SPECTATOR) {
                log.warn("Spectator cannot send chat messages - playerId: {}", playerId);
                return;
            }
        }

        // 출제자는 채팅 불가
        if (gameState.getCurrentTurn() != null &&
                gameState.getCurrentTurn().getDrawerId().equals(playerId)) {
            log.warn("Drawer cannot send chat messages");
            return;
        }

        // 플레이어 정보 조회
        Player player = gameState.getPlayers() != null ?
                gameState.getPlayers().stream()
                        .filter(p -> p.getPlayerId().equals(playerId))
                        .findFirst()
                        .orElse(null) : null;

        // 발신자가 이미 정답을 맞춘 상태인지 확인
        boolean senderIsCorrect = player != null && Boolean.TRUE.equals(player.getIsCorrect());

        // 정답을 맞춘 사람이 보낸 메시지 처리 (정답자끼리의 대화)
        if (senderIsCorrect) {
            ChatMessage chatMsg = ChatMessage.builder()
                    .messageId(java.util.UUID.randomUUID().toString())
                    .playerId(playerId)
                    .nickname(nickname)
                    .message(message)
                    .timestamp(System.currentTimeMillis())
                    .type(ChatType.NORMAL)
                    .isCorrect(false)
                    .senderIsCorrect(true)  // 발신자가 정답 맞춘 상태
                    .build();
            messagingTemplate.convertAndSend(WebSocketTopics.roomChat(roomId), chatMsg);
            return;
        }

        // 정답 체크 (아직 정답을 맞추지 않은 사람만)
        boolean isCorrect = checkAnswer(gameState, message);

        if (isCorrect) {

            handleCorrectAnswer(roomId, gameState, player, nickname);

            // 정답 메시지 (본인에게)
            ChatMessage correctMsg = ChatMessage.builder()
                    .messageId(java.util.UUID.randomUUID().toString())
                    .playerId(playerId)
                    .nickname(nickname)
                    .message(ChatConstants.Message.CORRECT_ANSWER)
                    .timestamp(System.currentTimeMillis())
                    .type(ChatType.CORRECT)
                    .isCorrect(true)
                    .build();

            log.info("=== Sending correct message ===");
            messagingTemplate.convertAndSend(WebSocketTopics.roomChat(roomId), correctMsg);

            // 정답 공지 (모두에게)
            ChatMessage announceMsg = ChatMessage.builder()
                    .messageId(java.util.UUID.randomUUID().toString())
                    .playerId(GameConstants.SystemPlayer.ID)
                    .nickname(GameConstants.SystemPlayer.NAME)
                    .message(String.format(ChatConstants.Message.PLAYER_GUESSED_FORMAT, nickname))
                    .timestamp(System.currentTimeMillis())
                    .type(ChatType.SYSTEM)
                    .isCorrect(false)
                    .build();

            messagingTemplate.convertAndSend(WebSocketTopics.roomChat(roomId), announceMsg);

            // GameState 브로드캐스트
            messagingTemplate.convertAndSend(WebSocketTopics.gameState(roomId), gameState);

            // 모든 플레이어가 정답을 맞췄는지 확인
            checkAllPlayersCorrect(roomId, gameState);

        } else {
            // 일반 채팅 메시지 (정답 못 맞춘 사람)
            ChatMessage chatMsg = ChatMessage.builder()
                    .messageId(java.util.UUID.randomUUID().toString())
                    .playerId(playerId)
                    .nickname(nickname)
                    .message(message)
                    .timestamp(System.currentTimeMillis())
                    .type(ChatType.NORMAL)
                    .isCorrect(false)
                    .senderIsCorrect(false)  // 발신자가 정답 못 맞춘 상태
                    .build();
            messagingTemplate.convertAndSend(WebSocketTopics.roomChat(roomId), chatMsg);
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

    private void handleCorrectAnswer(String roomId, GameState gameState, Player player, String nickname) {
        // 정답자 이전 점수 저장
        int previousAnswererScore = player.getScore() != null ? player.getScore() : 0;

        // 출제자 이전 점수 저장
        String drawerId = gameState.getCurrentTurn().getDrawerId();
        Player drawer = gameState.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(drawerId))
                .findFirst()
                .orElse(null);
        int previousDrawerScore = (drawer != null && drawer.getScore() != null) ? drawer.getScore() : 0;

        // GameScoringService를 통한 점수 계산 (정답자 + 출제자 모두 처리)
        gameScoringService.handleCorrectAnswer(gameState, player);

        // 이번 턴 획득 점수 기록
        TurnInfo currentTurn = gameState.getCurrentTurn();
        int earnedScore = (player.getScore() != null ? player.getScore() : 0) - previousAnswererScore;
        currentTurn.getTurnScores().put(player.getPlayerId(), earnedScore);

        // 출제자 획득 점수 기록 (누적)
        if (drawer != null) {
            int drawerEarnedScore = (drawer.getScore() != null ? drawer.getScore() : 0) - previousDrawerScore;
            currentTurn.getTurnScores().put(drawerId,
                    (currentTurn.getTurnScores().getOrDefault(drawerId, 0)) + drawerEarnedScore);
        }

        // 게임 상태 저장만 (브로드캐스트는 나중에)
        gameService.updateGameState(roomId, gameState);

        log.info("Player {} guessed correctly. Score: {}", nickname, player.getScore());
    }

    private void checkAllPlayersCorrect(String roomId, GameState gameState) {
        if (gameState.getPlayers() == null || gameState.getCurrentTurn() == null) {
            return;
        }

        String drawerId = gameState.getCurrentTurn().getDrawerId();

        // 출제자를 제외한 모든 플레이어가 정답을 맞췄는지 확인
        long totalPlayers = gameState.getPlayers().stream()
                .filter(p -> !p.getPlayerId().equals(drawerId))
                .count();

        long correctPlayers = gameState.getPlayers().stream()
                .filter(p -> !p.getPlayerId().equals(drawerId))
                .filter(p -> Boolean.TRUE.equals(p.getIsCorrect()))
                .count();

        if (totalPlayers > 0 && totalPlayers == correctPlayers) {
            log.info("All players guessed correctly - ending turn early. Room: {}", roomId);

            // 비동기로 턴 종료 (브로드캐스트가 완료된 후)
            asyncGameTimerScheduler.endTurnWithDelay(roomId, gameState,
                    com.unduck.paletteduck.domain.game.dto.TurnEndReason.ALL_CORRECT, 100);
        }
    }

    @MessageMapping("/room/{roomId}/game/hint/chosung")
    public void provideChosungHint(@DestinationVariable String roomId, @Payload String playerId) {
        boolean success = gameService.provideChosungHint(roomId, playerId);

        if (success) {
            GameState gameState = gameService.getGameState(roomId);
            messagingTemplate.convertAndSend(WebSocketTopics.gameState(roomId), gameState);
            log.info("Chosung hint provided - room: {}, playerId: {}", roomId, playerId);
        }
    }

    @MessageMapping("/room/{roomId}/game/hint/letter")
    public void provideLetterHint(@DestinationVariable String roomId, @Payload String playerId) {
        boolean success = gameService.provideLetterHint(roomId, playerId);

        if (success) {
            GameState gameState = gameService.getGameState(roomId);
            messagingTemplate.convertAndSend(WebSocketTopics.gameState(roomId), gameState);
            log.info("Letter hint provided - room: {}, playerId: {}", roomId, playerId);
        }
    }

    @MessageMapping("/room/{roomId}/game/vote")
    public void handleVote(@DestinationVariable String roomId, @Payload Map<String, String> data) {
        String voterId = data.get("voterId");
        String voteTypeStr = data.get("voteType");

        GameState gameState = gameService.getGameState(roomId);
        if (gameState == null || gameState.getCurrentTurn() == null) {
            log.error("Game state or current turn not found for room: {}", roomId);
            return;
        }

        // 관전자는 투표 불가
        com.unduck.paletteduck.domain.room.dto.RoomInfo roomInfo = roomService.getRoomInfo(roomId);
        if (roomInfo != null) {
            com.unduck.paletteduck.domain.room.dto.RoomPlayer roomPlayer = roomInfo.getPlayers().stream()
                    .filter(p -> p.getPlayerId().equals(voterId))
                    .findFirst()
                    .orElse(null);
            if (roomPlayer != null && roomPlayer.getRole() == com.unduck.paletteduck.domain.room.dto.PlayerRole.SPECTATOR) {
                log.warn("Spectator cannot vote - playerId: {}", voterId);
                return;
            }
        }

        // DRAWING 단계에서만 투표 가능
        if (gameState.getPhase() != GamePhase.DRAWING) {
            log.warn("Voting not allowed in phase: {}", gameState.getPhase());
            return;
        }

        // 본인 그림에는 투표 불가
        if (gameState.getCurrentTurn().getDrawerId().equals(voterId)) {
            log.warn("Drawer cannot vote on their own drawing");
            return;
        }

        VoteType voteType;
        try {
            voteType = VoteType.valueOf(voteTypeStr);
        } catch (IllegalArgumentException e) {
            log.error("Invalid vote type: {}", voteTypeStr);
            return;
        }

        TurnInfo currentTurn = gameState.getCurrentTurn();
        Player drawer = gameState.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(currentTurn.getDrawerId()))
                .findFirst()
                .orElse(null);

        if (drawer == null) {
            log.error("Drawer not found: {}", currentTurn.getDrawerId());
            return;
        }

        // 이전 투표 확인
        VoteType previousVote = currentTurn.getVotes().get(voterId);

        // 이전 투표 제거
        if (previousVote == VoteType.LIKE) {
            drawer.setTotalLikes((drawer.getTotalLikes() != null ? drawer.getTotalLikes() : 0) - 1);
        } else if (previousVote == VoteType.DISLIKE) {
            drawer.setTotalDislikes((drawer.getTotalDislikes() != null ? drawer.getTotalDislikes() : 0) - 1);
        }

        // 새 투표 적용
        if (voteType == VoteType.LIKE) {
            drawer.setTotalLikes((drawer.getTotalLikes() != null ? drawer.getTotalLikes() : 0) + 1);
            currentTurn.getVotes().put(voterId, VoteType.LIKE);
        } else if (voteType == VoteType.DISLIKE) {
            drawer.setTotalDislikes((drawer.getTotalDislikes() != null ? drawer.getTotalDislikes() : 0) + 1);
            currentTurn.getVotes().put(voterId, VoteType.DISLIKE);
        } else {
            // NONE: 투표 취소
            currentTurn.getVotes().put(voterId, VoteType.NONE);
        }

        // GameState 저장 및 브로드캐스트
        gameService.updateGameState(roomId, gameState);
        messagingTemplate.convertAndSend(WebSocketTopics.gameState(roomId), gameState);

        log.info("Vote processed - room: {}, voterId: {}, voteType: {}, drawer: {}",
                roomId, voterId, voteType, drawer.getNickname());
    }
}