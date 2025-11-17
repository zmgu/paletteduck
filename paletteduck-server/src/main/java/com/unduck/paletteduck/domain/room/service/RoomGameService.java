package com.unduck.paletteduck.domain.room.service;

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

    /**
     * 게임 설정 변경
     */
    public void updateSettings(String roomId, String playerId, GameSettings settings) {
        RoomInfo roomInfo = getRoomInfoOrThrow(roomId);

        // 방장 권한 확인
        RoomPlayer player = findPlayer(roomInfo, playerId);
        if (player == null || !player.isHost()) {
            log.warn("Not authorized to update settings - playerId: {}", playerId);
            return;
        }

        roomInfo.setSettings(settings);
        roomRepository.save(roomId, roomInfo);

        log.info("Settings updated - roomId: {}, by: {}", roomId, player.getNickname());
    }

    /**
     * 게임 시작
     */
    public void startGame(String roomId, String playerId) {
        RoomInfo roomInfo = getRoomInfoOrThrow(roomId);

        // 게임 시작 가능 여부 검증
        roomValidator.validateGameStart(roomInfo, playerId);

        roomInfo.setStatus(RoomStatus.PLAYING);
        roomRepository.save(roomId, roomInfo);

        log.info("Game started - roomId: {}", roomId);
    }

    // Private 헬퍼 메서드

    private RoomInfo getRoomInfoOrThrow(String roomId) {
        RoomInfo roomInfo = roomRepository.findById(roomId);
        if (roomInfo == null) {
            throw new IllegalArgumentException("Room not found: " + roomId);
        }
        return roomInfo;
    }

    private RoomPlayer findPlayer(RoomInfo roomInfo, String playerId) {
        return roomInfo.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);
    }
}
