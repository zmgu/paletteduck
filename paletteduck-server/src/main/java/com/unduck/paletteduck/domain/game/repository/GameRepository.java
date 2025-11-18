package com.unduck.paletteduck.domain.game.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unduck.paletteduck.domain.game.dto.GameState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GameRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String GAME_KEY_PREFIX = "game:";
    private static final Duration GAME_TTL = Duration.ofHours(24);

    public void save(String roomId, GameState gameState) {
        try {
            String key = GAME_KEY_PREFIX + roomId;
            String json = objectMapper.writeValueAsString(gameState);
            redisTemplate.opsForValue().set(key, json, GAME_TTL);
            log.debug("GameState saved: {}", roomId);
        } catch (Exception e) {
            log.error("Failed to save GameState", e);
            throw new RuntimeException("Failed to save game state", e);
        }
    }

    public GameState findById(String roomId) {
        try {
            String key = GAME_KEY_PREFIX + roomId;
            String json = (String) redisTemplate.opsForValue().get(key);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, GameState.class);
        } catch (Exception e) {
            log.error("Failed to load GameState", e);
            throw new RuntimeException("Failed to load game state", e);
        }
    }

    public void delete(String roomId) {
        String key = GAME_KEY_PREFIX + roomId;
        redisTemplate.delete(key);
        log.debug("GameState deleted: {}", roomId);
    }
}