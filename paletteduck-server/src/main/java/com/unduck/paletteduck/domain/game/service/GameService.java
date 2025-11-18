package com.unduck.paletteduck.domain.game.service;

import com.unduck.paletteduck.domain.game.dto.GameState;
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

        gameRepository.save(roomInfo.getRoomId(), gameState);
        log.info("Game initialized for room: {}, players: {}", roomInfo.getRoomId(), playerIds.size());
        return gameState;
    }

    public GameState getGameState(String roomId) {
        return gameRepository.findById(roomId);
    }

    public void deleteGame(String roomId) {
        gameRepository.delete(roomId);
    }
}