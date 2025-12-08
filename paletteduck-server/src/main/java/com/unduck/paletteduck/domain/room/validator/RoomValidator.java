package com.unduck.paletteduck.domain.room.validator;

import com.unduck.paletteduck.domain.room.constants.RoomConstants;
import com.unduck.paletteduck.domain.room.dto.PlayerRole;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.domain.room.dto.RoomPlayer;
import com.unduck.paletteduck.domain.room.util.RoomPlayerUtil;
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
        RoomPlayer player = RoomPlayerUtil.findPlayerByIdOrThrow(roomInfo, playerId);
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

        // 게임 중이면 관전자 자리만 체크
        if (roomInfo.getStatus() == com.unduck.paletteduck.domain.room.dto.RoomStatus.PLAYING) {
            if (spectatorCount >= maxSpectators) {
                log.warn("No spectator slots available - spectators: {}/{}",
                        spectatorCount, maxSpectators);
                throw new IllegalStateException("No spectator slots available");
            }
            return;
        }

        // 대기 중일 때는 둘 다 꽉 찬 경우만 불가
        if (playerCount >= maxPlayers && spectatorCount >= maxSpectators) {
            log.warn("Room is full - players: {}/{}, spectators: {}/{}",
                    playerCount, maxPlayers, spectatorCount, maxSpectators);
            throw new IllegalStateException("Room is full");
        }
    }

    /**
     * 게임 설정 변경 가능 여부 검증
     */
    public void validateSettingsUpdate(RoomInfo roomInfo, com.unduck.paletteduck.domain.game.dto.GameSettings newSettings) {
        // 현재 참가자 수 확인
        long currentPlayerCount = roomInfo.getPlayers().stream()
                .filter(p -> p.getRole() == PlayerRole.PLAYER)
                .count();

        // 최대 참가자 수가 현재 참가자 수보다 적으면 안됨
        if (newSettings.getMaxPlayers() < currentPlayerCount) {
            log.warn("Cannot set maxPlayers below current player count - current: {}, requested: {}",
                    currentPlayerCount, newSettings.getMaxPlayers());
            throw new IllegalStateException(
                    "Cannot set maxPlayers (" + newSettings.getMaxPlayers() +
                    ") below current player count (" + currentPlayerCount + ")");
        }

        // 최대 관전자 수 확인
        long currentSpectatorCount = roomInfo.getPlayers().stream()
                .filter(p -> p.getRole() == PlayerRole.SPECTATOR)
                .count();

        if (newSettings.getMaxSpectators() < currentSpectatorCount) {
            log.warn("Cannot set maxSpectators below current spectator count - current: {}, requested: {}",
                    currentSpectatorCount, newSettings.getMaxSpectators());
            throw new IllegalStateException(
                    "Cannot set maxSpectators (" + newSettings.getMaxSpectators() +
                    ") below current spectator count (" + currentSpectatorCount + ")");
        }
    }
}
