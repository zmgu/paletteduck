package com.unduck.paletteduck.domain.room.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SessionMappingService {

    private final Map<String, String> sessionToPlayer = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> playerToSessions = new ConcurrentHashMap<>();

    public void addSession(String sessionId, String playerId, String roomId) {
        log.info("Adding session mapping - sessionId: {}, playerId: {}, roomId: {}", sessionId, playerId, roomId);
        sessionToPlayer.put(sessionId, playerId + ":" + roomId);

        playerToSessions.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    public String removeSession(String sessionId) {
        log.info("Removing session - sessionId: {}", sessionId);
        String value = sessionToPlayer.remove(sessionId);

        if (value != null) {
            String[] parts = value.split(":");
            String playerId = parts[0];
            String roomId = parts.length > 1 ? parts[1] : null;

            Set<String> sessions = playerToSessions.get(playerId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    playerToSessions.remove(playerId);
                    log.info("Player has no more sessions - playerId: {}, roomId: {}", playerId, roomId);
                    return roomId;
                }
            }
        }

        return null;
    }

    public String getPlayerAndRoom(String sessionId) {
        return sessionToPlayer.get(sessionId);
    }
}