package com.unduck.paletteduck.domain.game.service;

import com.unduck.paletteduck.domain.game.dto.GamePhase;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.dto.TurnInfo;
import com.unduck.paletteduck.domain.game.repository.GameRepository;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.domain.room.service.RoomService;
import com.unduck.paletteduck.domain.word.service.WordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameTimerService {

    private final GameRepository gameRepository;
    private final RoomService roomService;
    private final WordService wordService;
    private final SimpMessagingTemplate messagingTemplate;

    @Async
    public void startCountdown(String roomId) {
        try {
            TimeUnit.SECONDS.sleep(3);

            GameState gameState = gameRepository.findById(roomId);
            if (gameState == null) {
                log.warn("GameState not found after countdown - roomId: {}", roomId);
                return;
            }

            startFirstTurn(roomId, gameState);
        } catch (InterruptedException e) {
            log.error("Countdown interrupted - roomId: {}", roomId, e);
            Thread.currentThread().interrupt();
        }
    }

    private void startFirstTurn(String roomId, GameState gameState) {
        RoomInfo roomInfo = roomService.getRoomInfo(roomId);
        if (roomInfo == null) {
            log.warn("RoomInfo not found - roomId: {}", roomId);
            return;
        }

        // 첫 번째 출제자 지정
        String drawerId = gameState.getTurnOrder().get(0);
        String drawerNickname = roomInfo.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(drawerId))
                .map(p -> p.getNickname())
                .findFirst()
                .orElse("Unknown");

        // 단어 선택지 생성
        int wordChoiceCount = roomInfo.getSettings().getWordChoices();
        List<String> wordChoices = wordService.getMixedWords(wordChoiceCount);

        // 턴 정보 생성
        TurnInfo turnInfo = new TurnInfo(1, drawerId, drawerNickname);
        turnInfo.setWordChoices(wordChoices);
        turnInfo.setTimeLeft(15); // 단어 선택 시간

        // GameState 업데이트
        gameState.setPhase(GamePhase.WORD_SELECT);
        gameState.setCurrentTurn(turnInfo);
        gameState.setPhaseStartTime(System.currentTimeMillis());

        gameRepository.save(roomId, gameState);

        // 브로드캐스트
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/game/state", gameState);

        log.info("First turn started - room: {}, drawer: {}, words: {}",
                roomId, drawerNickname, wordChoices);

        // 단어 선택 타이머 시작
        startWordSelectTimer(roomId);
    }

    @Async
    private void startWordSelectTimer(String roomId) {
        try {
            TimeUnit.SECONDS.sleep(15);

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

        } catch (InterruptedException e) {
            log.error("Word select timer interrupted - roomId: {}", roomId, e);
            Thread.currentThread().interrupt();
        }
    }

    private void startDrawingPhase(String roomId, GameState gameState) {
        gameState.setPhase(GamePhase.DRAWING);
        gameState.setPhaseStartTime(System.currentTimeMillis());
        gameState.getCurrentTurn().setTimeLeft(gameState.getDrawTime());
        gameState.getCurrentTurn().setWordChoices(List.of()); // 선택지 제거

        gameRepository.save(roomId, gameState);

        // 브로드캐스트
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/game/state", gameState);

        log.info("Drawing phase started - room: {}, word: {}, time: {}s",
                roomId, gameState.getCurrentTurn().getWord(), gameState.getDrawTime());
    }

    public void selectWord(String roomId, String playerId, String word) {
        GameState gameState = gameRepository.findById(roomId);
        if (gameState == null || gameState.getCurrentTurn() == null) {
            return;
        }

        // 출제자 본인인지 확인
        if (!gameState.getCurrentTurn().getDrawerId().equals(playerId)) {
            log.warn("Unauthorized word selection - playerId: {}, drawer: {}",
                    playerId, gameState.getCurrentTurn().getDrawerId());
            return;
        }

        // 선택지에 있는 단어인지 확인
        if (!gameState.getCurrentTurn().getWordChoices().contains(word)) {
            log.warn("Invalid word selection - word: {}", word);
            return;
        }

        gameState.getCurrentTurn().setWord(word);
        gameRepository.save(roomId, gameState);

        log.info("Word selected - room: {}, word: {}", roomId, word);

        // 즉시 그리기 단계로 전환
        startDrawingPhase(roomId, gameState);
    }
}