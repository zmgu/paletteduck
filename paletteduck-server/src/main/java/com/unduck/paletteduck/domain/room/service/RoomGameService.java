package com.unduck.paletteduck.domain.room.service;

import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.service.GameService;
import com.unduck.paletteduck.domain.game.service.GameTimerService;
import com.unduck.paletteduck.domain.game.dto.GameSettings;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.domain.room.dto.RoomPlayer;
import com.unduck.paletteduck.domain.room.dto.RoomStatus;
import com.unduck.paletteduck.domain.room.repository.RoomRepository;
import com.unduck.paletteduck.domain.room.validator.RoomValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomGameService {

    private final RoomRepository roomRepository;
    private final RoomValidator roomValidator;
    private final GameService gameService;
    private final GameTimerService gameTimerService;

    public GameState startGame(String roomId, String playerId) {
        RoomInfo roomInfo = roomRepository.findById(roomId);
        if (roomInfo == null) {
            throw new IllegalStateException("Room not found");
        }

        roomValidator.validateGameStart(roomInfo, playerId);

        roomInfo.setStatus(RoomStatus.PLAYING);
        roomRepository.save(roomId, roomInfo);

        GameState gameState = gameService.initializeGame(roomInfo);

        // 카운트다운 타이머 시작
        gameTimerService.startCountdown(roomId);

        log.info("Game started for room: {}", roomId);

        return gameState;
    }

    public void updateSettings(String roomId, String playerId, GameSettings settings) {
        RoomInfo roomInfo = roomRepository.findById(roomId);
        if (roomInfo == null) {
            throw new IllegalStateException("Room not found");
        }

        RoomPlayer player = roomInfo.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);

        if (player == null || !player.isHost()) {
            return;
        }

        roomInfo.setSettings(settings);
        roomRepository.save(roomId, roomInfo);
    }
}