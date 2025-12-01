package com.unduck.paletteduck.domain.room.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unduck.paletteduck.domain.room.dto.ReturnToWaitingTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReturnToWaitingTrackerRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String TRACKER_KEY_PREFIX = "return-tracker:";
    private static final long TRACKER_TTL_SECONDS = 120; // 90초 + 여유 30초

    /**
     * 복귀 추적 정보 저장
     */
    public void save(String roomId, ReturnToWaitingTracker tracker) {
        try {
            String trackerJson = objectMapper.writeValueAsString(tracker);
            String key = TRACKER_KEY_PREFIX + roomId;
            redisTemplate.opsForValue().set(key, trackerJson, Duration.ofSeconds(TRACKER_TTL_SECONDS));
            log.debug("ReturnToWaitingTracker saved - roomId: {}", roomId);
        } catch (JsonProcessingException e) {
            log.error("Failed to save return tracker - roomId: {}", roomId, e);
            throw new RuntimeException("Failed to save return tracker", e);
        }
    }

    /**
     * 복귀 추적 정보 조회
     */
    public ReturnToWaitingTracker findById(String roomId) {
        String key = TRACKER_KEY_PREFIX + roomId;
        String trackerJson = (String) redisTemplate.opsForValue().get(key);

        if (trackerJson == null) {
            log.debug("ReturnToWaitingTracker not found - roomId: {}", roomId);
            return null;
        }

        try {
            return objectMapper.readValue(trackerJson, ReturnToWaitingTracker.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse return tracker - roomId: {}", roomId, e);
            throw new RuntimeException("Failed to get return tracker", e);
        }
    }

    /**
     * 복귀 추적 정보 삭제
     */
    public void delete(String roomId) {
        String key = TRACKER_KEY_PREFIX + roomId;
        redisTemplate.delete(key);
        log.debug("ReturnToWaitingTracker deleted - roomId: {}", roomId);
    }

    /**
     * 복귀 추적 정보 존재 여부 확인
     */
    public boolean exists(String roomId) {
        String key = TRACKER_KEY_PREFIX + roomId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
