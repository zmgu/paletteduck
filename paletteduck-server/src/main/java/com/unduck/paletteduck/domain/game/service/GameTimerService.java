package com.unduck.paletteduck.domain.game.service;

import com.unduck.paletteduck.domain.game.dto.GamePhase;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.dto.TurnInfo;
import com.unduck.paletteduck.domain.game.repository.GameRepository;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.domain.room.service.RoomService;
import com.unduck.paletteduck.domain.word.service.WordService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameTimerService {

    private final GameRepository gameRepository;
    private final RoomService roomService;
    private final WordService wordService;
    private final SimpMessagingTemplate messagingTemplate;

    // ✅ 타이머 관리용
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final Map<String, ScheduledFuture<?>> activeTimers = new ConcurrentHashMap<>();

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ✅ 타이머 취소
    private void cancelTimer(String roomId) {
        ScheduledFuture<?> timer = activeTimers.remove(roomId);
        if (timer != null && !timer.isDone()) {
            timer.cancel(false);
            log.info("Timer cancelled - room: {}", roomId);
        }
    }

    @Async
    public void startCountdown(String roomId) {
        try {
            TimeUnit.SECONDS.sleep(3);

            GameState gameState = gameRepository.findById(roomId);
            if (gameState == null) {
                log.warn("GameState not found after countdown - roomId: {}", roomId);
                return;
            }

            // ✅ 첫 턴도 startNextTurn 사용 (turnNumber = 0일 때)
            if (gameState.getCurrentTurn() == null) {
                gameState.setCurrentTurn(new TurnInfo(0, "", ""));  // 초기화
            }

            startNextTurn(roomId, gameState);
        } catch (InterruptedException e) {
            log.error("Countdown interrupted - roomId: {}", roomId, e);
            Thread.currentThread().interrupt();
        }
    }

    private void startWordSelectTimer(String roomId) {
        log.info("Word select timer starting - room: {}, duration: 15s", roomId);

        ScheduledFuture<?> timer = scheduler.schedule(() -> {
            try {
                log.info("Word select timer expired - room: {}", roomId);

                GameState gameState = gameRepository.findById(roomId);
                if (gameState == null || gameState.getCurrentTurn() == null) {
                    return;
                }

                // 단어 선택 안 했으면 랜덤 선택
                if (gameState.getCurrentTurn().getWord() == null) {
                    List<String> choices = gameState.getCurrentTurn().getWordChoices();
                    String randomWord = choices.get((int) (Math.random() * choices.size()));
                    gameState.getCurrentTurn().setWord(randomWord);

                    log.info("Word auto-selected - room: {}, word: {}", roomId, randomWord);
                }

                // 그리기 단계로 전환
                startDrawingPhase(roomId, gameState);

            } catch (Exception e) {
                log.error("Error in word select timer - room: {}", roomId, e);
            }
        }, 15, TimeUnit.SECONDS);

        activeTimers.put(roomId + "_WORD_SELECT", timer);
    }

    private void startDrawingPhase(String roomId, GameState gameState) {
        // ✅ 단어 선택 타이머 취소
        cancelTimer(roomId + "_WORD_SELECT");

        // ✅ RoomInfo에서 drawTime 가져오기
        RoomInfo roomInfo = roomService.getRoomInfo(roomId);
        if (roomInfo == null) {
            log.warn("RoomInfo not found - roomId: {}", roomId);
            return;
        }

        int drawTime = roomInfo.getSettings().getDrawTime();

        gameState.setPhase(GamePhase.DRAWING);
        gameState.setPhaseStartTime(System.currentTimeMillis());
        gameState.getCurrentTurn().setTimeLeft(drawTime);
        gameState.getCurrentTurn().setWordChoices(List.of());

        gameRepository.save(roomId, gameState);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/game/state", gameState);

        log.info("Drawing phase started - room: {}, word: {}, time: {}s",
                roomId, gameState.getCurrentTurn().getWord(), drawTime);

        // ✅ 그리기 타이머 시작
        startDrawingTimer(roomId, drawTime);
    }

    private void startDrawingTimer(String roomId, int drawTime) {
        log.info("Drawing timer starting - room: {}, duration: {}s", roomId, drawTime);

        // ✅ 힌트 타이머 시작
        startHintTimer(roomId, drawTime);

        // 턴 종료 타이머
        ScheduledFuture<?> timer = scheduler.schedule(() -> {
            try {
                log.info("Drawing timer expired - ending turn for room: {}", roomId);
                endTurn(roomId);
            } catch (Exception e) {
                log.error("Error in drawing timer - room: {}", roomId, e);
            }
        }, drawTime, TimeUnit.SECONDS);

        activeTimers.put(roomId + "_DRAWING", timer);
    }

    // ✅ 새 메서드: 힌트 타이머
    private void startHintTimer(String roomId, int drawTime) {
        GameState gameState = gameRepository.findById(roomId);
        if (gameState == null || gameState.getCurrentTurn() == null) {
            return;
        }

        String word = gameState.getCurrentTurn().getWord();
        if (word == null || word.isEmpty()) {
            return;
        }

        int wordLength = word.length();
        int hintInterval = drawTime / (wordLength + 1);  // 힌트 공개 간격

        log.info("Hint timer setup - word length: {}, interval: {}s", wordLength, hintInterval);

        // 각 힌트마다 타이머 설정
        for (int i = 1; i <= wordLength; i++) {
            final int hintIndex = i;
            int delay = hintInterval * i;

            ScheduledFuture<?> hintTimer = scheduler.schedule(() -> {
                try {
                    revealHint(roomId, hintIndex);
                } catch (Exception e) {
                    log.error("Error revealing hint - room: {}, index: {}", roomId, hintIndex, e);
                }
            }, delay, TimeUnit.SECONDS);

            activeTimers.put(roomId + "_HINT_" + i, hintTimer);
        }
    }

    // ✅ 새 메서드: 힌트 공개
    private void revealHint(String roomId, int hintIndex) {
        GameState gameState = gameRepository.findById(roomId);
        if (gameState == null || gameState.getCurrentTurn() == null) {
            return;
        }

        // 이미 턴이 종료되었으면 무시
        if (gameState.getPhase() != GamePhase.DRAWING) {
            return;
        }

        gameState.getCurrentTurn().setRevealedHints(hintIndex);
        gameRepository.save(roomId, gameState);

        log.info("Hint revealed - room: {}, hints: {}/{}",
                roomId, hintIndex, gameState.getCurrentTurn().getWord().length());

        // 브로드캐스트
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/game/state", gameState);
    }

    public void endTurnEarly(String roomId) {
        log.info("Ending turn early - room: {}", roomId);

        // ✅ 타이머 취소 (그리기 + 모든 힌트)
        cancelTimer(roomId + "_DRAWING");

        // ✅ 모든 힌트 타이머 취소
        GameState gameState = gameRepository.findById(roomId);
        if (gameState != null && gameState.getCurrentTurn() != null) {
            String word = gameState.getCurrentTurn().getWord();
            if (word != null) {
                for (int i = 1; i <= word.length(); i++) {
                    cancelTimer(roomId + "_HINT_" + i);
                }
            }
        }

        endTurn(roomId);
    }

    private void endTurn(String roomId) {
        GameState gameState = gameRepository.findById(roomId);
        if (gameState == null) {
            log.warn("GameState not found - roomId: {}", roomId);
            return;
        }

        // 이미 다른 phase로 넘어갔으면 무시
        if (gameState.getPhase() != GamePhase.DRAWING) {
            log.info("Turn already ended - current phase: {}", gameState.getPhase());
            return;
        }

        log.info("Turn ended - room: {}, word: {}", roomId, gameState.getCurrentTurn().getWord());

        // ✅ 그리기 타이머 취소 (중복 방지)
        cancelTimer(roomId + "_DRAWING");

        gameState.setPhase(GamePhase.ROUND_END);
        gameState.setPhaseStartTime(System.currentTimeMillis());

        gameRepository.save(roomId, gameState);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/game/state", gameState);

        log.info("Broadcasting turn end - room: {}", roomId);

        // ✅ 다음 턴 타이머 시작 (5초)
        startNextTurnTimer(roomId);
    }

    private void startNextTurnTimer(String roomId) {
        log.info("Next turn timer starting - room: {}, duration: 5s", roomId);

        ScheduledFuture<?> timer = scheduler.schedule(() -> {
            try {
                log.info("Next turn timer expired - starting next turn for room: {}", roomId);

                GameState gameState = gameRepository.findById(roomId);
                if (gameState == null) {
                    return;
                }

                // ✅ startNextTurn 호출 (플레이어 정답 상태 초기화는 startNextTurn 안에서 처리)
                startNextTurn(roomId, gameState);

            } catch (Exception e) {
                log.error("Error in next turn timer - room: {}", roomId, e);
            }
        }, 5, TimeUnit.SECONDS);

        activeTimers.put(roomId + "_NEXT_TURN", timer);
    }

    public void selectWord(String roomId, String playerId, String word) {
        GameState gameState = gameRepository.findById(roomId);
        if (gameState == null || gameState.getCurrentTurn() == null) {
            return;
        }

        if (!gameState.getCurrentTurn().getDrawerId().equals(playerId)) {
            log.warn("Unauthorized word selection - playerId: {}, drawer: {}",
                    playerId, gameState.getCurrentTurn().getDrawerId());
            return;
        }

        if (!gameState.getCurrentTurn().getWordChoices().contains(word)) {
            log.warn("Invalid word selection - word: {}", word);
            return;
        }

        gameState.getCurrentTurn().setWord(word);
        gameRepository.save(roomId, gameState);

        log.info("Word selected - room: {}, word: {}", roomId, word);

        // ✅ 즉시 그리기 단계로 전환 (타이머 자동 취소됨)
        startDrawingPhase(roomId, gameState);
    }

    private void startNextTurnTimerAsync(String roomId) {
        try {
            TimeUnit.SECONDS.sleep(5);

            GameState gameState = gameRepository.findById(roomId);
            if (gameState == null) {
                return;
            }

            // ✅ 다음 턴 시작 (순환)
            startNextTurn(roomId, gameState);

        } catch (InterruptedException e) {
            log.error("Next turn timer interrupted - roomId: {}", roomId, e);
            Thread.currentThread().interrupt();
        }
    }

    // ✅ 새 메서드: 다음 턴 시작
    private void startNextTurn(String roomId, GameState gameState) {
        RoomInfo roomInfo = roomService.getRoomInfo(roomId);
        if (roomInfo == null) {
            log.warn("RoomInfo not found - roomId: {}", roomId);
            return;
        }

        // 현재 턴 번호 가져오기
        int currentTurnNumber = gameState.getCurrentTurn() != null
                ? gameState.getCurrentTurn().getTurnNumber()
                : 0;

        int nextTurnNumber = currentTurnNumber + 1;

        // ✅ 다음 출제자 인덱스 계산
        List<String> turnOrder = gameState.getTurnOrder();
        int nextDrawerIndex = (nextTurnNumber - 1) % turnOrder.size();

        // ✅ 라운드 계산
        int nextRound = ((nextTurnNumber - 1) / turnOrder.size()) + 1;

        log.info("Next turn calculation - turnNumber: {}, drawerIndex: {}, round: {}/{}",
                nextTurnNumber, nextDrawerIndex, nextRound, gameState.getTotalRounds());

        // ✅ 게임 종료 체크 (모든 라운드 완료)
        if (nextRound > gameState.getTotalRounds()) {
            log.info("All rounds completed - ending game for room: {}", roomId);
            endGame(roomId, gameState);
            return;
        }

        // ✅ 현재 라운드 업데이트
        gameState.setCurrentRound(nextRound);

        String nextDrawerId = turnOrder.get(nextDrawerIndex);
        String nextDrawerNickname = roomInfo.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(nextDrawerId))
                .map(p -> p.getNickname())
                .findFirst()
                .orElse("Unknown");

        // 플레이어 정답 상태 초기화
        gameState.getPlayers().forEach(p -> p.setIsCorrect(false));

        // 단어 선택지 생성
        int wordChoiceCount = roomInfo.getSettings().getWordChoices();
        List<String> wordChoices = wordService.getMixedWords(wordChoiceCount);

        // 새 턴 정보 생성
        TurnInfo turnInfo = new TurnInfo(nextTurnNumber, nextDrawerId, nextDrawerNickname);
        turnInfo.setWordChoices(wordChoices);
        turnInfo.setTimeLeft(15);

        // GameState 업데이트
        gameState.setPhase(GamePhase.WORD_SELECT);
        gameState.setCurrentTurn(turnInfo);
        gameState.setPhaseStartTime(System.currentTimeMillis());

        gameRepository.save(roomId, gameState);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/game/state", gameState);

        log.info("Turn {} (Round {}/{}) started - drawer: {} (index {})",
                nextTurnNumber, nextRound, gameState.getTotalRounds(),
                nextDrawerNickname, nextDrawerIndex);

        // 단어 선택 타이머 시작
        startWordSelectTimer(roomId);
    }

    // ✅ 새 메서드: 게임 종료
    private void endGame(String roomId, GameState gameState) {
        log.info("Ending game - room: {}", roomId);

        // ✅ GAME_END phase로 전환
        gameState.setPhase(GamePhase.GAME_END);
        gameState.setPhaseStartTime(System.currentTimeMillis());

        // ✅ 최종 순위 정렬 (점수 높은 순)
        gameState.getPlayers().sort((a, b) ->
                Integer.compare(b.getScore() != null ? b.getScore() : 0,
                        a.getScore() != null ? a.getScore() : 0)
        );

        gameRepository.save(roomId, gameState);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/game/state", gameState);

        log.info("Game ended - room: {}, winner: {}",
                roomId,
                gameState.getPlayers().isEmpty() ? "none" : gameState.getPlayers().get(0).getPlayerName());
    }
}