package com.unduck.paletteduck.domain.room.service;

import com.unduck.paletteduck.config.constants.WebSocketTopics;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.service.GameService;
import com.unduck.paletteduck.domain.game.service.GameTimerService;
import com.unduck.paletteduck.domain.game.dto.GameSettings;
import com.unduck.paletteduck.domain.room.dto.ReturnToWaitingTracker;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.domain.room.dto.RoomPlayer;
import com.unduck.paletteduck.domain.room.dto.RoomStatus;
import com.unduck.paletteduck.domain.room.dto.PlayerRole;
import com.unduck.paletteduck.domain.room.repository.ReturnToWaitingTrackerRepository;
import com.unduck.paletteduck.domain.room.repository.RoomRepository;
import com.unduck.paletteduck.domain.room.validator.RoomValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomGameService {

    private final RoomRepository roomRepository;
    private final RoomValidator roomValidator;
    private final GameService gameService;
    private final GameTimerService gameTimerService;
    private final ReturnToWaitingTrackerRepository trackerRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public GameState startGame(String roomId, String playerId) {
        RoomInfo roomInfo = roomRepository.findById(roomId);
        if (roomInfo == null) {
            throw new IllegalStateException("Room not found");
        }

        roomValidator.validateGameStart(roomInfo, playerId);

        roomInfo.setStatus(RoomStatus.PLAYING);
        roomRepository.save(roomId, roomInfo);

        GameState gameState = gameService.initializeGame(roomInfo);

        // 카운트다운 타이머 시작
        gameTimerService.startCountdown(roomId);

        log.info("Game started for room: {}", roomId);

        return gameState;
    }

    public void updateSettings(String roomId, String playerId, GameSettings settings) {
        RoomInfo roomInfo = roomRepository.findById(roomId);
        if (roomInfo == null) {
            throw new IllegalStateException("Room not found");
        }

        RoomPlayer player = roomInfo.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);

        if (player == null || !player.isHost()) {
            return;
        }

        roomInfo.setSettings(settings);
        roomRepository.save(roomId, roomInfo);
    }

    /**
     * 게임 종료 후 대기방으로 복귀
     * - 이전 게임 설정 유지
     * - 플레이어 ready 상태 초기화
     * - 방 상태를 WAITING으로 변경
     */
    public RoomInfo returnToWaitingRoom(String roomId) {
        RoomInfo roomInfo = roomRepository.findById(roomId);
        if (roomInfo == null) {
            throw new IllegalStateException("Room not found");
        }

        // 게임 중이 아니면 무시
        if (roomInfo.getStatus() != RoomStatus.PLAYING) {
            log.warn("Cannot return to waiting room - room is not playing: {}", roomId);
            return roomInfo;
        }

        // 모든 플레이어 ready 상태 초기화
        roomInfo.getPlayers().forEach(player -> player.setReady(false));

        // 방 상태를 WAITING으로 변경
        roomInfo.setStatus(RoomStatus.WAITING);

        // 설정은 유지 (이전 판과 동일한 세팅)
        roomRepository.save(roomId, roomInfo);

        // GameState 삭제 (새 게임 시작 시 다시 생성)
        gameService.deleteGame(roomId);

        log.info("Returned to waiting room - room: {}, players: {}", roomId, roomInfo.getPlayers().size());

        return roomInfo;
    }

    /**
     * 플레이어가 수동으로 대기방 복귀할 때 호출
     * - 방장 위임 로직 처리
     * - 첫 번째 복귀자인 경우 방 상태를 WAITING으로 변경
     * - synchronized로 동시성 문제 방지
     */
    public synchronized RoomInfo handlePlayerReturnToWaiting(String roomId, String playerId) {
        RoomInfo roomInfo = roomRepository.findById(roomId);
        if (roomInfo == null) {
            throw new IllegalStateException("Room not found");
        }

        // 복귀 추적 정보 조회 (먼저 확인)
        ReturnToWaitingTracker tracker = trackerRepository.findById(roomId);

        // 트래커가 없으면 게임 종료 후 복귀 프로세스가 아님
        if (tracker == null) {
            // 이미 대기방 상태면 복귀할 필요 없음
            if (roomInfo.getStatus() != RoomStatus.PLAYING) {
                log.debug("Room already in waiting state and no return process - roomId: {}", roomId);
                return roomInfo;
            }

            log.warn("Return tracker not found - roomId: {}, creating new tracker", roomId);
            // 트래커 생성 (edge case 처리)
            String hostId = roomInfo.getPlayers().stream()
                    .filter(RoomPlayer::isHost)
                    .map(RoomPlayer::getPlayerId)
                    .findFirst()
                    .orElse(null);
            tracker = new ReturnToWaitingTracker(roomId, hostId);
        }

        // 이미 복귀한 플레이어인지 확인
        if (tracker.hasPlayerReturned(playerId)) {
            log.debug("Player already returned - playerId: {}", playerId);
            return roomInfo;
        }

        // 복귀하려는 플레이어 정보 조회
        RoomPlayer returningPlayer = roomInfo.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);

        if (returningPlayer == null) {
            log.warn("Player not found in room - playerId: {}", playerId);
            throw new IllegalStateException("Player not found in room");
        }

        // 복귀 플레이어 추가
        tracker.addReturnedPlayer(playerId);
        boolean isFirstReturn = tracker.getReturnedPlayerIds().size() == 1;

        // 관전자 복귀 제한: 참가자나 방장이 먼저 복귀하지 않으면 관전자는 복귀 불가
        // 첫 복귀자는 절대 관전자가 될 수 없음
        if (returningPlayer.getRole() == PlayerRole.SPECTATOR) {
            if (isFirstReturn || !tracker.isHasPlayerOrHostReturned()) {
                log.warn("Spectator cannot be first returner or return before players/host - playerId: {}", playerId);
                // 이미 추가된 플레이어 제거
                tracker.getReturnedPlayerIds().remove(playerId);
                tracker.setAnyoneReturned(tracker.getReturnedPlayerIds().size() > 0);
                throw new IllegalStateException("관전자는 참가자나 방장이 먼저 복귀한 후에 복귀할 수 있습니다.");
            }
        }

        log.info("Player returned to waiting - room: {}, player: {}, role: {}, isFirst: {}",
                roomId, playerId, returningPlayer.getRole(), isFirstReturn);

        // 첫 번째 복귀자인 경우 방 상태를 WAITING으로 변경
        if (isFirstReturn) {
            // 방 상태를 WAITING으로 변경
            returnToWaitingRoom(roomId);
            roomInfo = roomRepository.findById(roomId); // 업데이트된 정보 다시 조회
        }

        // 방장 위임 로직
        String originalHostId = tracker.getOriginalHostId();

        if (playerId.equals(originalHostId)) {
            // 원래 방장이 수동으로 복귀하는 경우
            tracker.setHasOriginalHostReturned(true);
            tracker.setHasPlayerOrHostReturned(true);

            // 방장 권한은 이미 원래 방장이 가지고 있으므로 유지
            RoomPlayer originalHost = roomInfo.getPlayers().stream()
                    .filter(p -> p.getPlayerId().equals(originalHostId))
                    .findFirst()
                    .orElse(null);

            if (originalHost != null && !originalHost.isHost()) {
                // 혹시 권한이 없다면 다시 부여
                RoomPlayer currentHost = roomInfo.getPlayers().stream()
                        .filter(RoomPlayer::isHost)
                        .findFirst()
                        .orElse(null);

                if (currentHost != null) {
                    currentHost.setHost(false);
                    currentHost.setReady(false);
                }

                originalHost.setHost(true);
                originalHost.setReady(false);

                log.info("Host restored to original - from: {} (temporary), to: {} (original)",
                        currentHost != null ? currentHost.getNickname() : "none", originalHost.getNickname());

                roomRepository.save(roomId, roomInfo);
            } else {
                log.info("Original host returned and already has host authority - playerId: {}", playerId);
            }
        } else if (returningPlayer.getRole() == PlayerRole.PLAYER) {
            // 참가자가 복귀하는 경우
            tracker.setHasPlayerOrHostReturned(true);

            // 첫 번째로 복귀한 참가자라면 기록 (방장 자동 복귀 시 위임 대상)
            if (tracker.getFirstReturnedPlayerId() == null) {
                tracker.setFirstReturnedPlayerId(playerId);
                log.info("First player returned (candidate for host delegation on auto-return) - playerId: {}", playerId);

                // 첫 번째 복귀자가 참가자인 경우, 원래 방장의 방장 권한 제거
                // (방장이 수동 복귀하거나 자동 복귀할 때까지 방장 권한 없음)
                RoomPlayer originalHost = roomInfo.getPlayers().stream()
                        .filter(RoomPlayer::isHost)
                        .findFirst()
                        .orElse(null);

                if (originalHost != null && !originalHost.getPlayerId().equals(playerId)) {
                    originalHost.setHost(false);
                    originalHost.setReady(false);

                    log.info("Host authority temporarily removed - original host: {} (not yet returned)",
                            originalHost.getNickname());

                    roomRepository.save(roomId, roomInfo);
                }
            }

            // 방장 권한은 부여하지 않음 (원래 방장이 수동 복귀하거나 자동 복귀할 때까지 대기)
            log.info("Player returned but host authority not transferred yet - playerId: {}", playerId);
        } else if (returningPlayer.getRole() == PlayerRole.SPECTATOR) {
            // 관전자가 복귀하는 경우 (이미 참가자/방장이 복귀한 상태)
            log.info("Spectator returned after players/host - playerId: {}", playerId);
        }

        // 트래커 저장
        trackerRepository.save(roomId, tracker);

        // 업데이트된 방 정보 브로드캐스트
        messagingTemplate.convertAndSend(WebSocketTopics.room(roomId), roomInfo);

        return roomInfo;
    }
}