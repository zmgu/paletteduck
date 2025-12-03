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
import com.unduck.paletteduck.domain.room.util.RoomPlayerUtil;
import com.unduck.paletteduck.domain.room.validator.RoomValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomGameService {

    private final RoomRepository roomRepository;
    private final RoomValidator roomValidator;
    private final GameService gameService;
    private final com.unduck.paletteduck.domain.game.service.AsyncGameTimerScheduler asyncGameTimerScheduler;
    private final ReturnToWaitingTrackerRepository trackerRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 방 단위 락 관리
    private final ConcurrentHashMap<String, Lock> roomLocks = new ConcurrentHashMap<>();

    /**
     * 방 ID에 대한 락을 획득
     */
    private Lock getRoomLock(String roomId) {
        return roomLocks.computeIfAbsent(roomId, k -> new ReentrantLock());
    }

    /**
     * 방 ID에 대한 락을 정리 (방이 삭제될 때 호출)
     */
    private void cleanupRoomLock(String roomId) {
        roomLocks.remove(roomId);
    }

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
        asyncGameTimerScheduler.startCountdown(roomId);

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
     * - 방 단위 락으로 동시성 문제 방지
     */
    public RoomInfo handlePlayerReturnToWaiting(String roomId, String playerId) {
        Lock lock = getRoomLock(roomId);
        lock.lock();
        try {
            return doHandlePlayerReturnToWaiting(roomId, playerId);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 대기방 복귀 처리 실제 로직
     */
    private RoomInfo doHandlePlayerReturnToWaiting(String roomId, String playerId) {
        RoomInfo roomInfo = getRoomInfoOrThrow(roomId);
        ReturnToWaitingTracker tracker = getOrCreateTracker(roomId, roomInfo);

        // 검증
        validateReturnRequest(roomInfo, tracker, playerId);

        RoomPlayer returningPlayer = findPlayerOrThrow(roomInfo, playerId);

        // 복귀 플레이어 등록
        tracker.addReturnedPlayer(playerId);
        boolean isFirstReturn = tracker.getReturnedPlayerIds().size() == 1;

        log.info("Player returned to waiting - room: {}, player: {}, role: {}, isFirst: {}",
                roomId, playerId, returningPlayer.getRole(), isFirstReturn);

        // 첫 번째 복귀자 처리
        if (isFirstReturn) {
            roomInfo = handleFirstReturn(roomId);
        }

        // 역할별 처리
        if (playerId.equals(tracker.getOriginalHostId())) {
            handleOriginalHostReturn(roomId, roomInfo, tracker, playerId);
        } else if (returningPlayer.getRole() == PlayerRole.PLAYER) {
            handlePlayerReturn(roomId, roomInfo, tracker, playerId);
        } else if (returningPlayer.getRole() == PlayerRole.SPECTATOR) {
            log.info("Spectator returned after players/host - playerId: {}", playerId);
        }

        // 트래커 저장 및 브로드캐스트
        trackerRepository.save(roomId, tracker);
        messagingTemplate.convertAndSend(WebSocketTopics.room(roomId), roomInfo);

        return roomInfo;
    }

    /**
     * 방 정보 조회 (없으면 예외)
     */
    private RoomInfo getRoomInfoOrThrow(String roomId) {
        RoomInfo roomInfo = roomRepository.findById(roomId);
        if (roomInfo == null) {
            throw new IllegalStateException("Room not found: " + roomId);
        }
        return roomInfo;
    }

    /**
     * 트래커 조회 또는 생성
     */
    private ReturnToWaitingTracker getOrCreateTracker(String roomId, RoomInfo roomInfo) {
        ReturnToWaitingTracker tracker = trackerRepository.findById(roomId);

        if (tracker == null) {
            if (roomInfo.getStatus() != RoomStatus.PLAYING) {
                log.debug("Room already in waiting state and no return process - roomId: {}", roomId);
                return null;
            }

            log.warn("Return tracker not found - roomId: {}, creating new tracker", roomId);
            String hostId = roomInfo.getPlayers().stream()
                    .filter(RoomPlayer::isHost)
                    .map(RoomPlayer::getPlayerId)
                    .findFirst()
                    .orElse(null);
            tracker = new ReturnToWaitingTracker(roomId, hostId);
        }

        return tracker;
    }

    /**
     * 복귀 요청 검증
     */
    private void validateReturnRequest(RoomInfo roomInfo, ReturnToWaitingTracker tracker, String playerId) {
        if (tracker == null) {
            return; // 이미 대기방 상태
        }

        if (tracker.hasPlayerReturned(playerId)) {
            log.debug("Player already returned - playerId: {}", playerId);
            throw new IllegalStateException("이미 복귀한 플레이어입니다.");
        }

        RoomPlayer returningPlayer = findPlayerOrThrow(roomInfo, playerId);
        boolean wouldBeFirstReturn = tracker.getReturnedPlayerIds().isEmpty();

        // 관전자는 첫 복귀자가 될 수 없음
        if (returningPlayer.getRole() == PlayerRole.SPECTATOR) {
            if (wouldBeFirstReturn || !tracker.isHasPlayerOrHostReturned()) {
                log.warn("Spectator cannot be first returner - playerId: {}", playerId);
                throw new IllegalStateException("관전자는 참가자나 방장이 먼저 복귀한 후에 복귀할 수 있습니다.");
            }
        }
    }

    /**
     * 플레이어 찾기 (없으면 예외)
     */
    private RoomPlayer findPlayerOrThrow(RoomInfo roomInfo, String playerId) {
        return RoomPlayerUtil.findPlayerByIdOrThrow(roomInfo, playerId);
    }

    /**
     * 첫 번째 복귀자 처리
     */
    private RoomInfo handleFirstReturn(String roomId) {
        returnToWaitingRoom(roomId);
        return roomRepository.findById(roomId);
    }

    /**
     * 원래 방장 복귀 처리
     */
    private void handleOriginalHostReturn(String roomId, RoomInfo roomInfo, ReturnToWaitingTracker tracker, String playerId) {
        tracker.setHasOriginalHostReturned(true);
        tracker.setHasPlayerOrHostReturned(true);

        RoomPlayer originalHost = findPlayerOrThrow(roomInfo, playerId);

        if (!originalHost.isHost()) {
            restoreHostAuthority(roomInfo, originalHost);
            roomRepository.save(roomId, roomInfo);
            log.info("Host authority restored to original - playerId: {}", playerId);
        } else {
            log.info("Original host returned and already has host authority - playerId: {}", playerId);
        }
    }

    /**
     * 방장 권한 복원
     */
    private void restoreHostAuthority(RoomInfo roomInfo, RoomPlayer newHost) {
        RoomPlayer currentHost = roomInfo.getPlayers().stream()
                .filter(RoomPlayer::isHost)
                .findFirst()
                .orElse(null);

        if (currentHost != null) {
            currentHost.setHost(false);
            currentHost.setReady(false);
        }

        newHost.setHost(true);
        newHost.setReady(false);
    }

    /**
     * 참가자 복귀 처리
     */
    private void handlePlayerReturn(String roomId, RoomInfo roomInfo, ReturnToWaitingTracker tracker, String playerId) {
        tracker.setHasPlayerOrHostReturned(true);

        // 첫 번째로 복귀한 참가자 기록
        if (tracker.getFirstReturnedPlayerId() == null) {
            tracker.setFirstReturnedPlayerId(playerId);
            log.info("First player returned (candidate for host delegation) - playerId: {}", playerId);

            // 원래 방장이 아직 복귀하지 않았을 때만 권한 일시 제거
            if (!tracker.isHasOriginalHostReturned()) {
                removeOriginalHostAuthority(roomId, roomInfo, playerId);
            } else {
                log.info("Original host already returned, keeping host authority - playerId: {}", playerId);
            }
        }

        log.info("Player returned but host authority not transferred yet - playerId: {}", playerId);
    }

    /**
     * 원래 방장의 권한 일시 제거
     */
    private void removeOriginalHostAuthority(String roomId, RoomInfo roomInfo, String returningPlayerId) {
        RoomPlayer originalHost = roomInfo.getPlayers().stream()
                .filter(RoomPlayer::isHost)
                .findFirst()
                .orElse(null);

        if (originalHost != null && !originalHost.getPlayerId().equals(returningPlayerId)) {
            originalHost.setHost(false);
            originalHost.setReady(false);

            log.info("Host authority temporarily removed - original host: {}", originalHost.getNickname());
            roomRepository.save(roomId, roomInfo);
        }
    }
}