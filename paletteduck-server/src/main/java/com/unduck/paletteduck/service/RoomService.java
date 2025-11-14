package com.unduck.paletteduck.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unduck.paletteduck.dto.RoomCreateResponse;
import com.unduck.paletteduck.dto.RoomInfo;
import com.unduck.paletteduck.dto.RoomPlayer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RoomCreateResponse createRoom(String playerId, String nickname) {
        String roomId = UUID.randomUUID().toString().substring(0, 8);
        String inviteCode = roomId;

        RoomPlayer host = new RoomPlayer(playerId, nickname, true, false);
        List<RoomPlayer> players = new ArrayList<>();
        players.add(host);

        try {
            String playersJson = objectMapper.writeValueAsString(players);
            redisTemplate.opsForValue().set("room:" + roomId, playersJson, Duration.ofHours(3));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to create room", e);
        }

        return new RoomCreateResponse(roomId, inviteCode);
    }

    public RoomInfo getRoomInfo(String roomId) {
        String playersJson = (String) redisTemplate.opsForValue().get("room:" + roomId);

        if (playersJson == null) {
            return null;
        }

        try {
            List<RoomPlayer> players = objectMapper.readValue(playersJson, new TypeReference<List<RoomPlayer>>() {});
            return new RoomInfo(roomId, roomId, players);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to get room info", e);
        }
    }

    public void joinRoom(String roomId, String playerId, String nickname) {
        String playersJson = (String) redisTemplate.opsForValue().get("room:" + roomId);

        if (playersJson == null) {
            throw new RuntimeException("Room not found");
        }

        try {
            List<RoomPlayer> players = objectMapper.readValue(playersJson, new TypeReference<List<RoomPlayer>>() {});

            boolean alreadyJoined = players.stream()
                    .anyMatch(p -> p.getPlayerId().equals(playerId));

            if (!alreadyJoined) {
                players.add(new RoomPlayer(playerId, nickname, false, false));
                String updatedJson = objectMapper.writeValueAsString(players);
                redisTemplate.opsForValue().set("room:" + roomId, updatedJson, Duration.ofHours(3));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to join room", e);
        }
    }
}