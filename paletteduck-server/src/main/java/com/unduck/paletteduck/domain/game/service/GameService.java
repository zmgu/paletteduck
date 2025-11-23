package com.unduck.paletteduck.domain.game.service;

import com.unduck.paletteduck.domain.game.dto.GamePhase;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.dto.Player;
import com.unduck.paletteduck.domain.game.dto.TurnInfo;
import com.unduck.paletteduck.domain.game.repository.GameRepository;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.domain.room.dto.RoomPlayer;
import com.unduck.paletteduck.domain.room.dto.PlayerRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final HintService hintService;

    public GameState initializeGame(RoomInfo roomInfo) {
        List<String> playerIds = roomInfo.getPlayers().stream()
                .filter(p -> p.getRole() == PlayerRole.PLAYER)
                .map(RoomPlayer::getPlayerId)
                .collect(Collectors.toList());

        GameState gameState = new GameState(
                roomInfo.getRoomId(),
                roomInfo.getSettings().getRounds(),
                roomInfo.getSettings().getDrawTime(),
                playerIds
        );

        // ✅ players 초기화 추가
        List<Player> players = roomInfo.getPlayers().stream()
                .filter(p -> p.getRole() == PlayerRole.PLAYER)
                .map(rp -> Player.builder()
                        .playerId(rp.getPlayerId())
                        .nickname(rp.getNickname())
                        .score(0)
                        .isCorrect(false)
                        .build())
                .collect(Collectors.toList());
        gameState.setPlayers(players);

        gameRepository.save(roomInfo.getRoomId(), gameState);
        log.info("Game initialized for room: {}, players: {}", roomInfo.getRoomId(), playerIds.size());
        return gameState;
    }

    public void updateGameState(String roomId, GameState gameState) {
        gameRepository.save(roomId, gameState);
        log.debug("Game state updated - roomId: {}", roomId);
    }

    public GameState getGameState(String roomId) {
        return gameRepository.findById(roomId);
    }

    public void deleteGame(String roomId) {
        gameRepository.delete(roomId);
    }

    /**
     * 출제자가 수동으로 초성 힌트를 제공합니다
     */
    public boolean provideChosungHint(String roomId, String playerId) {
        GameState gameState = gameRepository.findById(roomId);
        if (gameState == null || gameState.getCurrentTurn() == null) {
            log.warn("Game state not found - roomId: {}", roomId);
            return false;
        }

        // 출제자 확인
        if (!gameState.getCurrentTurn().getDrawerId().equals(playerId)) {
            log.warn("Unauthorized hint request - playerId: {}", playerId);
            return false;
        }

        // 그리기 단계 확인
        if (gameState.getPhase() != GamePhase.DRAWING) {
            log.warn("Cannot provide hint - wrong phase: {}", gameState.getPhase());
            return false;
        }

        // 레벨 2 이상이어야 초성 힌트 사용 가능
        if (gameState.getCurrentTurn().getHintLevel() < 2) {
            log.warn("Chosung hint not available yet - current level: {}", gameState.getCurrentTurn().getHintLevel());
            return false;
        }

        TurnInfo turnInfo = gameState.getCurrentTurn();
        String word = turnInfo.getWord();

        // 랜덤 초성 위치 공개
        Integer position = hintService.revealRandomChosung(word, turnInfo.getRevealedChosungPositions());
        if (position == null) {
            log.info("No more chosung positions to reveal - roomId: {}", roomId);
            return false;
        }

        turnInfo.getRevealedChosungPositions().add(position);

        // 힌트 배열 및 문자열 업데이트
        String[] hintArray = hintService.generateHintArray(word,
                turnInfo.getRevealedChosungPositions(),
                turnInfo.getRevealedLetterPositions());
        turnInfo.setHintArray(hintArray);

        String hint = hintService.generateHintDisplay(word,
                turnInfo.getRevealedChosungPositions(),
                turnInfo.getRevealedLetterPositions());
        turnInfo.setCurrentHint(hint);

        gameRepository.save(roomId, gameState);
        log.info("Manual chosung hint provided - room: {}, hint: {}", roomId, hint);
        return true;
    }

    /**
     * 출제자가 수동으로 글자 힌트를 제공합니다
     */
    public boolean provideLetterHint(String roomId, String playerId) {
        GameState gameState = gameRepository.findById(roomId);
        if (gameState == null || gameState.getCurrentTurn() == null) {
            log.warn("Game state not found - roomId: {}", roomId);
            return false;
        }

        // 출제자 확인
        if (!gameState.getCurrentTurn().getDrawerId().equals(playerId)) {
            log.warn("Unauthorized hint request - playerId: {}", playerId);
            return false;
        }

        // 그리기 단계 확인
        if (gameState.getPhase() != GamePhase.DRAWING) {
            log.warn("Cannot provide hint - wrong phase: {}", gameState.getPhase());
            return false;
        }

        // 레벨 2 이상이어야 글자 힌트 사용 가능
        if (gameState.getCurrentTurn().getHintLevel() < 2) {
            log.warn("Letter hint not available yet - current level: {}", gameState.getCurrentTurn().getHintLevel());
            return false;
        }

        TurnInfo turnInfo = gameState.getCurrentTurn();
        String word = turnInfo.getWord();

        // 랜덤 글자 위치 공개 (최대 글자수-1까지)
        Integer position = hintService.revealRandomLetter(word, turnInfo.getRevealedLetterPositions());
        if (position == null) {
            log.info("Cannot reveal more letters - roomId: {}", roomId);
            return false;
        }

        turnInfo.getRevealedLetterPositions().add(position);

        // 힌트 배열 및 문자열 업데이트
        String[] hintArray = hintService.generateHintArray(word,
                turnInfo.getRevealedChosungPositions(),
                turnInfo.getRevealedLetterPositions());
        turnInfo.setHintArray(hintArray);

        String hint = hintService.generateHintDisplay(word,
                turnInfo.getRevealedChosungPositions(),
                turnInfo.getRevealedLetterPositions());
        turnInfo.setCurrentHint(hint);

        gameRepository.save(roomId, gameState);
        log.info("Manual letter hint provided - room: {}, hint: {}", roomId, hint);
        return true;
    }
}