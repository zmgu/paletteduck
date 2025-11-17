package com.unduck.paletteduck.domain.room.validator;

import com.unduck.paletteduck.domain.room.constant.RoomConstants;
import com.unduck.paletteduck.domain.room.dto.PlayerRole;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.domain.room.dto.RoomPlayer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RoomValidator {

    /**
     * 게임 시작 가능 여부 검증
     */
    public void validateGameStart(RoomInfo roomInfo, String playerId) {
        // 방장 권한 확인
        RoomPlayer player = findPlayerOrThrow(roomInfo, playerId);
        if (!player.isHost()) {
            log.warn("Not host - playerId: {}", playerId);
            throw new IllegalStateException("Only host can start game");
        }

        // 참가자 수 확인
        long playerCount = roomInfo.getPlayers().stream()
                .filter(p -> p.getRole() == PlayerRole.PLAYER)
                .count();

        if (playerCount < RoomConstants.MIN_PLAYERS) {
            log.warn("Not enough players - count: {}", playerCount);
            throw new IllegalStateException("Need at least " + RoomConstants.MIN_PLAYERS + " players");
        }

        // 준비 완료 확인
        boolean allReady = roomInfo.getPlayers().stream()
                .filter(p -> p.getRole() == PlayerRole.PLAYER && !p.isHost())
                .allMatch(RoomPlayer::isReady);

        if (!allReady) {
            log.warn("Not all players ready - roomId: {}", roomInfo.getRoomId());
            throw new IllegalStateException("All players must be ready");
        }
    }

    /**
     * 역할 변경 가능 여부 검증
     */
    public void validateRoleChange(RoomInfo roomInfo, PlayerRole newRole) {
        long targetRoleCount = roomInfo.getPlayers().stream()
                .filter(p -> p.getRole() == newRole)
                .count();

        int maxCount = newRole == PlayerRole.PLAYER
                ? roomInfo.getSettings().getMaxPlayers()
                : roomInfo.getSettings().getMaxSpectators();

        if (targetRoleCount >= maxCount) {
            log.warn("Role is full - role: {}, count: {}/{}", newRole, targetRoleCount, maxCount);
            throw new IllegalStateException("Role is full");
        }
    }

    /**
     * 방 입장 가능 여부 검증
     */
    public void validateJoinRoom(RoomInfo roomInfo) {
        int playerCount = (int) roomInfo.getPlayers().stream()
                .filter(p -> p.getRole() == PlayerRole.PLAYER).count();
        int spectatorCount = (int) roomInfo.getPlayers().stream()
                .filter(p -> p.getRole() == PlayerRole.SPECTATOR).count();

        int maxPlayers = roomInfo.getSettings().getMaxPlayers();
        int maxSpectators = roomInfo.getSettings().getMaxSpectators();

        if (playerCount >= maxPlayers && spectatorCount >= maxSpectators) {
            log.warn("Room is full - players: {}/{}, spectators: {}/{}",
                    playerCount, maxPlayers, spectatorCount, maxSpectators);
            throw new IllegalStateException("Room is full");
        }
    }

    /**
     * 플레이어 찾기 (없으면 예외)
     */
    private RoomPlayer findPlayerOrThrow(RoomInfo roomInfo, String playerId) {
        return roomInfo.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
    }
}
