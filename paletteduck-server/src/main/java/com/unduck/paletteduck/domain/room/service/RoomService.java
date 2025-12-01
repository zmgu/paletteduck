package com.unduck.paletteduck.domain.room.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unduck.paletteduck.domain.game.dto.GameSettings;
import com.unduck.paletteduck.domain.room.dto.*;
import com.unduck.paletteduck.domain.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    /**
     * 방 생성
     */
    public RoomCreateResponse createRoom(String playerId, String nickname) {
        String roomId = generateRoomId();

        RoomPlayer host = createHost(playerId, nickname);
        List<RoomPlayer> players = new ArrayList<>();
        players.add(host);

        GameSettings settings = new GameSettings();
        RoomInfo roomInfo = new RoomInfo(roomId, roomId, players, settings, RoomStatus.WAITING);

        roomRepository.save(roomId, roomInfo);
        log.info("Room created - roomId: {}, host: {}", roomId, nickname);

        return new RoomCreateResponse(roomId, roomId);
    }

    /**
     * 방 정보 조회
     */
    public RoomInfo getRoomInfo(String roomId) {
        return roomRepository.findById(roomId);
    }

    /**
     * 방 정보 저장
     */
    public void saveRoomInfo(String roomId, RoomInfo roomInfo) {
        roomRepository.save(roomId, roomInfo);
    }

    /**
     * 방 삭제
     */
    public void deleteRoom(String roomId) {
        roomRepository.delete(roomId);
    }

    // Private 헬퍼 메서드

    private String generateRoomId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private RoomPlayer createHost(String playerId, String nickname) {
        return new RoomPlayer(playerId, nickname, true, false, PlayerRole.PLAYER, 0, 0, 0, System.currentTimeMillis());
    }
}