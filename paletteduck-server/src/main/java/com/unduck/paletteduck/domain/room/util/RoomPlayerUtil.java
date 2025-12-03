package com.unduck.paletteduck.domain.room.util;

import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.domain.room.dto.RoomPlayer;

import java.util.Optional;

/**
 * RoomPlayer 관련 유틸리티 메서드
 */
public final class RoomPlayerUtil {

    private RoomPlayerUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * playerId로 RoomPlayer를 찾습니다
     *
     * @param roomInfo 방 정보
     * @param playerId 플레이어 ID
     * @return Optional로 감싼 RoomPlayer
     */
    public static Optional<RoomPlayer> findPlayerById(RoomInfo roomInfo, String playerId) {
        if (roomInfo == null || playerId == null) {
            return Optional.empty();
        }

        return roomInfo.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst();
    }

    /**
     * playerId로 RoomPlayer를 찾고, 없으면 예외를 던집니다
     *
     * @param roomInfo 방 정보
     * @param playerId 플레이어 ID
     * @return RoomPlayer
     * @throws IllegalStateException 플레이어를 찾을 수 없는 경우
     */
    public static RoomPlayer findPlayerByIdOrThrow(RoomInfo roomInfo, String playerId) {
        return findPlayerById(roomInfo, playerId)
                .orElseThrow(() -> new IllegalStateException("Player not found in room: " + playerId));
    }

    /**
     * 방장을 찾습니다
     *
     * @param roomInfo 방 정보
     * @return Optional로 감싼 방장 RoomPlayer
     */
    public static Optional<RoomPlayer> findHost(RoomInfo roomInfo) {
        if (roomInfo == null) {
            return Optional.empty();
        }

        return roomInfo.getPlayers().stream()
                .filter(RoomPlayer::isHost)
                .findFirst();
    }
}
