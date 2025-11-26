package com.unduck.paletteduck.domain.room.service;

import com.unduck.paletteduck.domain.room.dto.PlayerRole;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.domain.room.dto.RoomPlayer;
import com.unduck.paletteduck.domain.room.repository.RoomRepository;
import com.unduck.paletteduck.domain.room.validator.RoomValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomPlayerService {

    private final RoomRepository roomRepository;
    private final RoomValidator roomValidator;

    /**
     * 방 입장
     */
    public void joinRoom(String roomId, String playerId, String nickname) {
        RoomInfo roomInfo = getRoomInfoOrThrow(roomId);

        // 이미 입장한 경우 스킵
        if (isPlayerInRoom(roomInfo, playerId)) {
            log.debug("Player already in room - playerId: {}", playerId);
            return;
        }

        // 입장 가능 여부 검증
        roomValidator.validateJoinRoom(roomInfo);

        // 역할 결정 (참가자 우선)
        PlayerRole role = determineRole(roomInfo);

        RoomPlayer newPlayer = new RoomPlayer(playerId, nickname, false, false, role, 0, 0, 0);
        roomInfo.getPlayers().add(newPlayer);

        roomRepository.save(roomId, roomInfo);
        log.info("Player joined - roomId: {}, nickname: {}, role: {}", roomId, nickname, role);
    }

    /**
     * 방 나가기
     */
    public RoomInfo leaveRoom(String roomId, String playerId) {
        RoomInfo roomInfo = roomRepository.findById(roomId);
        if (roomInfo == null) return null;

        RoomPlayer leavingPlayer = findPlayer(roomInfo, playerId);
        if (leavingPlayer == null) return null;

        roomInfo.getPlayers().remove(leavingPlayer);

        // 방이 비면 삭제
        if (roomInfo.getPlayers().isEmpty()) {
            roomRepository.delete(roomId);
            log.info("Room deleted (empty) - roomId: {}", roomId);
            return null;
        }

        // 방장 위임
        if (leavingPlayer.isHost()) {
            transferHost(roomInfo, leavingPlayer);
        }

        roomRepository.save(roomId, roomInfo);
        log.info("Player left - roomId: {}, nickname: {}", roomId, leavingPlayer.getNickname());

        return roomInfo;
    }

    /**
     * 역할 변경
     */
    public void changeRole(String roomId, String playerId, PlayerRole newRole) {
        RoomInfo roomInfo = getRoomInfoOrThrow(roomId);
        RoomPlayer player = findPlayerOrThrow(roomInfo, playerId);

        // 역할 변경 가능 여부 검증
        roomValidator.validateRoleChange(roomInfo, newRole);

        player.setRole(newRole);
        player.setReady(false); // 역할 변경 시 준비 해제

        roomRepository.save(roomId, roomInfo);
        log.info("Role changed - roomId: {}, nickname: {}, newRole: {}",
                roomId, player.getNickname(), newRole);
    }

    /**
     * 준비 완료 토글
     */
    public void toggleReady(String roomId, String playerId) {
        RoomInfo roomInfo = getRoomInfoOrThrow(roomId);
        RoomPlayer player = findPlayerOrThrow(roomInfo, playerId);

        if (player.isHost()) {
            log.warn("Host cannot toggle ready");
            return;
        }

        player.setReady(!player.isReady());

        roomRepository.save(roomId, roomInfo);
        log.info("Ready toggled - roomId: {}, nickname: {}, ready: {}",
                roomId, player.getNickname(), player.isReady());
    }

    // Private 헬퍼 메서드

    private RoomInfo getRoomInfoOrThrow(String roomId) {
        RoomInfo roomInfo = roomRepository.findById(roomId);
        if (roomInfo == null) {
            throw new IllegalArgumentException("Room not found: " + roomId);
        }
        return roomInfo;
    }

    private RoomPlayer findPlayer(RoomInfo roomInfo, String playerId) {
        return roomInfo.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    private RoomPlayer findPlayerOrThrow(RoomInfo roomInfo, String playerId) {
        RoomPlayer player = findPlayer(roomInfo, playerId);
        if (player == null) {
            throw new IllegalArgumentException("Player not found: " + playerId);
        }
        return player;
    }

    private boolean isPlayerInRoom(RoomInfo roomInfo, String playerId) {
        return roomInfo.getPlayers().stream()
                .anyMatch(p -> p.getPlayerId().equals(playerId));
    }

    private PlayerRole determineRole(RoomInfo roomInfo) {
        // 게임 중이면 무조건 관전자로 입장
        if (roomInfo.getStatus() == com.unduck.paletteduck.domain.room.dto.RoomStatus.PLAYING) {
            return PlayerRole.SPECTATOR;
        }

        // 대기 중일 때는 참가자 수에 따라 결정
        int playerCount = (int) roomInfo.getPlayers().stream()
                .filter(p -> p.getRole() == PlayerRole.PLAYER).count();

        return playerCount < roomInfo.getSettings().getMaxPlayers()
                ? PlayerRole.PLAYER
                : PlayerRole.SPECTATOR;
    }

    private void transferHost(RoomInfo roomInfo, RoomPlayer oldHost) {
        RoomPlayer newHost = roomInfo.getPlayers().get(0);
        newHost.setHost(true);
        newHost.setReady(false);

        log.info("Host transferred - from: {}, to: {}",
                oldHost.getNickname(), newHost.getNickname());
    }
}
