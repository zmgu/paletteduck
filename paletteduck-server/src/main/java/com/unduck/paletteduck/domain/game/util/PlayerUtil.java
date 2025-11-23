package com.unduck.paletteduck.domain.game.util;

import com.unduck.paletteduck.domain.game.dto.Player;
import com.unduck.paletteduck.domain.room.dto.RoomPlayer;

import java.util.List;
import java.util.Optional;

public final class PlayerUtil {

    private PlayerUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static Optional<Player> findPlayerById(List<Player> players, String playerId) {
        if (players == null || playerId == null) {
            return Optional.empty();
        }

        return players.stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst();
    }

    public static Optional<RoomPlayer> findRoomPlayerById(List<RoomPlayer> players, String playerId) {
        if (players == null || playerId == null) {
            return Optional.empty();
        }

        return players.stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst();
    }

    public static Player findPlayerByIdOrThrow(List<Player> players, String playerId) {
        return findPlayerById(players, playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
    }

    public static RoomPlayer findRoomPlayerByIdOrThrow(List<RoomPlayer> players, String playerId) {
        return findRoomPlayerById(players, playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
    }
}
