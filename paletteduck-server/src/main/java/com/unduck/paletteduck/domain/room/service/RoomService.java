package com.unduck.paletteduck.domain.room.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unduck.paletteduck.domain.game.dto.GameSettings;
import com.unduck.paletteduck.domain.room.dto.*;
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

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RoomCreateResponse createRoom(String playerId, String nickname) {
        String roomId = UUID.randomUUID().toString().substring(0, 8);

        RoomPlayer host = new RoomPlayer(playerId, nickname, true, false, PlayerRole.PLAYER, 0, 0, 0);
        List<RoomPlayer> players = new ArrayList<>();
        players.add(host);

        GameSettings settings = new GameSettings();
        RoomInfo roomInfo = new RoomInfo(roomId, roomId, players, settings, RoomStatus.WAITING);

        saveRoomInfo(roomId, roomInfo);
        log.info("Room created - roomId: {}, host: {}", roomId, nickname);

        return new RoomCreateResponse(roomId, roomId);
    }

    public RoomInfo getRoomInfo(String roomId) {
        String roomJson = (String) redisTemplate.opsForValue().get("room:" + roomId);

        if (roomJson == null) {
            return null;
        }

        try {
            return objectMapper.readValue(roomJson, RoomInfo.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse room info - roomId: {}", roomId, e);
            throw new RuntimeException("Failed to get room info", e);
        }
    }

    public void joinRoom(String roomId, String playerId, String nickname) {
        RoomInfo roomInfo = getRoomInfo(roomId);

        if (roomInfo == null) {
            throw new RuntimeException("Room not found");
        }

        boolean alreadyJoined = roomInfo.getPlayers().stream()
                .anyMatch(p -> p.getPlayerId().equals(playerId));

        if (!alreadyJoined) {
            int playerCount = (int) roomInfo.getPlayers().stream()
                    .filter(p -> p.getRole() == PlayerRole.PLAYER).count();
            int spectatorCount = (int) roomInfo.getPlayers().stream()
                    .filter(p -> p.getRole() == PlayerRole.SPECTATOR).count();

            PlayerRole role;
            if (playerCount < roomInfo.getSettings().getMaxPlayers()) {
                role = PlayerRole.PLAYER;
            } else if (spectatorCount < roomInfo.getSettings().getMaxSpectators()) {
                role = PlayerRole.SPECTATOR;
            } else {
                throw new RuntimeException("Room is full");
            }

            RoomPlayer newPlayer = new RoomPlayer(playerId, nickname, false, false, role, 0, 0, 0);
            roomInfo.getPlayers().add(newPlayer);
            saveRoomInfo(roomId, roomInfo);
            log.info("Player joined - roomId: {}, nickname: {}, role: {}", roomId, nickname, role);
        }
    }

    public void changeRole(String roomId, String playerId, PlayerRole newRole) {
        RoomInfo roomInfo = getRoomInfo(roomId);

        if (roomInfo == null) {
            throw new RuntimeException("Room not found");
        }

        RoomPlayer player = roomInfo.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Player not found"));

        int targetRoleCount = (int) roomInfo.getPlayers().stream()
                .filter(p -> p.getRole() == newRole).count();

        int maxCount = newRole == PlayerRole.PLAYER ?
                roomInfo.getSettings().getMaxPlayers() :
                roomInfo.getSettings().getMaxSpectators();

        if (targetRoleCount >= maxCount) {
            throw new RuntimeException("Role is full");
        }

        player.setRole(newRole);
        player.setReady(false);

        saveRoomInfo(roomId, roomInfo);
        log.info("Role changed - roomId: {}, nickname: {}, newRole: {}", roomId, player.getNickname(), newRole);
    }

    public void toggleReady(String roomId, String playerId) {
        RoomInfo roomInfo = getRoomInfo(roomId);

        if (roomInfo == null) {
            log.error("Room not found - roomId: {}", roomId);
            throw new RuntimeException("Room not found");
        }

        RoomPlayer player = roomInfo.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);

        if (player == null) {
            log.error("Player not found - roomId: {}, playerId: {}", roomId, playerId);
            return;
        }

        if (player.isHost()) {
            log.warn("Host cannot toggle ready - roomId: {}", roomId);
            return;
        }

        player.setReady(!player.isReady());
        saveRoomInfo(roomId, roomInfo);
        log.info("Ready toggled - roomId: {}, nickname: {}, ready: {}", roomId, player.getNickname(), player.isReady());
    }

    public void updateSettings(String roomId, String playerId, GameSettings settings) {
        RoomInfo roomInfo = getRoomInfo(roomId);

        if (roomInfo == null) {
            log.error("Room not found - roomId: {}", roomId);
            throw new RuntimeException("Room not found");
        }

        RoomPlayer player = roomInfo.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);

        if (player == null || !player.isHost()) {
            log.warn("Not authorized to update settings - roomId: {}, playerId: {}", roomId, playerId);
            return;
        }

        roomInfo.setSettings(settings);
        saveRoomInfo(roomId, roomInfo);
        log.info("Settings updated - roomId: {}, by: {}", roomId, player.getNickname());
    }

    public void startGame(String roomId, String playerId) {
        RoomInfo roomInfo = getRoomInfo(roomId);

        if (roomInfo == null) {
            log.error("Room not found - roomId: {}", roomId);
            throw new RuntimeException("Room not found");
        }

        RoomPlayer player = roomInfo.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);

        if (player == null || !player.isHost()) {
            log.warn("Not authorized to start game - roomId: {}, playerId: {}", roomId, playerId);
            throw new RuntimeException("Only host can start game");
        }

        long playerCount = roomInfo.getPlayers().stream()
                .filter(p -> p.getRole() == PlayerRole.PLAYER)
                .count();

        if (playerCount < 2) {
            log.warn("Not enough players - roomId: {}, count: {}", roomId, playerCount);
            throw new RuntimeException("Need at least 2 players");
        }

        boolean allReady = roomInfo.getPlayers().stream()
                .filter(p -> p.getRole() == PlayerRole.PLAYER && !p.isHost())
                .allMatch(RoomPlayer::isReady);

        if (!allReady) {
            log.warn("Not all players ready - roomId: {}", roomId);
            throw new RuntimeException("All players must be ready");
        }

        roomInfo.setStatus(RoomStatus.PLAYING);
        saveRoomInfo(roomId, roomInfo);
        log.info("Game started - roomId: {}", roomId);
    }

    public RoomInfo leaveRoom(String roomId, String playerId) {
        RoomInfo roomInfo = getRoomInfo(roomId);

        if (roomInfo == null) {
            return null;
        }

        RoomPlayer leavingPlayer = roomInfo.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);

        if (leavingPlayer == null) {
            return null;
        }

        roomInfo.getPlayers().remove(leavingPlayer);

        if (roomInfo.getPlayers().isEmpty()) {
            redisTemplate.delete("room:" + roomId);
            log.info("Room deleted (empty) - roomId: {}", roomId);
            return null;
        }

        if (leavingPlayer.isHost()) {
            RoomPlayer newHost = roomInfo.getPlayers().get(0);
            newHost.setHost(true);
            newHost.setReady(false);
            log.info("Host transferred - roomId: {}, from: {}, to: {}", roomId, leavingPlayer.getNickname(), newHost.getNickname());
        }

        saveRoomInfo(roomId, roomInfo);
        log.info("Player left - roomId: {}, nickname: {}", roomId, leavingPlayer.getNickname());

        return roomInfo; // 변경된 roomInfo 반환
    }

    private void saveRoomInfo(String roomId, RoomInfo roomInfo) {
        try {
            String roomJson = objectMapper.writeValueAsString(roomInfo);
            redisTemplate.opsForValue().set("room:" + roomId, roomJson, Duration.ofHours(24));
        } catch (JsonProcessingException e) {
            log.error("Failed to save room info - roomId: {}", roomId, e);
            throw new RuntimeException("Failed to save room info", e);
        }
    }
}