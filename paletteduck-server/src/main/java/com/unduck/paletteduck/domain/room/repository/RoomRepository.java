package com.unduck.paletteduck.domain.room.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unduck.paletteduck.domain.room.constant.RoomConstants;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.exception.BusinessException;
import com.unduck.paletteduck.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RoomRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String ROOM_KEY_PREFIX = "room:";

    /**
     * 방 정보 저장
     */
    public void save(String roomId, RoomInfo roomInfo) {
        try {
            String roomJson = objectMapper.writeValueAsString(roomInfo);
            String key = ROOM_KEY_PREFIX + roomId;
            redisTemplate.opsForValue().set(key, roomJson, Duration.ofHours(RoomConstants.ROOM_TTL_HOURS));
            log.debug("Room saved - roomId: {}", roomId);
        } catch (JsonProcessingException e) {
            log.error("Failed to save room - roomId: {}", roomId, e);
            throw new BusinessException(ErrorCode.DATA_SERIALIZATION_ERROR, e);
        }
    }

    /**
     * 방 정보 조회
     */
    public RoomInfo findById(String roomId) {
        String key = ROOM_KEY_PREFIX + roomId;
        String roomJson = (String) redisTemplate.opsForValue().get(key);

        if (roomJson == null) {
            log.debug("Room not found - roomId: {}", roomId);
            return null;
        }

        try {
            return objectMapper.readValue(roomJson, RoomInfo.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse room - roomId: {}", roomId, e);
            throw new BusinessException(ErrorCode.DATA_SERIALIZATION_ERROR, e);
        }
    }

    /**
     * 방 삭제
     */
    public void delete(String roomId) {
        String key = ROOM_KEY_PREFIX + roomId;
        redisTemplate.delete(key);
        log.info("Room deleted - roomId: {}", roomId);
    }

    /**
     * 방 존재 여부 확인
     */
    public boolean exists(String roomId) {
        String key = ROOM_KEY_PREFIX + roomId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
