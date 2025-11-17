package com.unduck.paletteduck.domain.player.service;

import com.unduck.paletteduck.domain.player.dto.PlayerJoinRequest;
import com.unduck.paletteduck.domain.player.dto.PlayerJoinResponse;
import com.unduck.paletteduck.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtil jwtUtil;

    public PlayerJoinResponse joinGame(PlayerJoinRequest request) {
        String playerId = UUID.randomUUID().toString();
        String token = jwtUtil.generateToken(playerId, request.getNickname());

        redisTemplate.opsForValue().set(
                "player:" + playerId,
                request.getNickname(),
                Duration.ofHours(2)
        );

        return new PlayerJoinResponse(token, playerId, request.getNickname());
    }
}