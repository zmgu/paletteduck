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

        // 복귀 플레이어 추가
        tracker.addReturnedPlayer(playerId);
        boolean isFirstReturn = tracker.getReturnedPlayerIds().size() == 1;

        log.info("Player returned to waiting - room: {}, player: {}, isFirst: {}",
                roomId, playerId, isFirstReturn);

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
            RoomPlayer currentHost = roomInfo.getPlayers().stream()
                    .filter(RoomPlayer::isHost)
                    .findFirst()
                    .orElse(null);

            RoomPlayer originalHost = roomInfo.getPlayers().stream()
                    .filter(p -> p.getPlayerId().equals(originalHostId))
                    .findFirst()
                    .orElse(null);

            if (currentHost != null && originalHost != null &&
                !currentHost.getPlayerId().equals(originalHostId)) {
                // 다른 사람이 임시 방장이었다면 원래 방장에게 권한 재위임
                currentHost.setHost(false);
                currentHost.setReady(false);
                originalHost.setHost(true);
                originalHost.setReady(false);

                log.info("Host restored to original - from: {} (temporary), to: {} (original)",
                        currentHost.getNickname(), originalHost.getNickname());

                roomRepository.save(roomId, roomInfo);
            }
        } else if (isFirstReturn && !playerId.equals(originalHostId)) {
            // 첫 번째 복귀자가 참가자인 경우 -> 임시 방장 위임
            RoomPlayer currentHost = roomInfo.getPlayers().stream()
                    .filter(RoomPlayer::isHost)
                    .findFirst()
                    .orElse(null);

            RoomPlayer newHost = roomInfo.getPlayers().stream()
                    .filter(p -> p.getPlayerId().equals(playerId))
                    .findFirst()
                    .orElse(null);

            if (currentHost != null && newHost != null && !currentHost.getPlayerId().equals(playerId)) {
                // 임시 방장 권한 이전 (원래 방장이 수동 복귀하면 다시 돌려줌)
                currentHost.setHost(false);
                newHost.setHost(true);
                newHost.setReady(false);

                log.info("Temporary host assigned on return - from: {} (original), to: {} (first returner)",
                        currentHost.getNickname(), newHost.getNickname());

                roomRepository.save(roomId, roomInfo);
            }
        }

        // 트래커 저장
        trackerRepository.save(roomId, tracker);

        // 업데이트된 방 정보 브로드캐스트
        messagingTemplate.convertAndSend(WebSocketTopics.room(roomId), roomInfo);

        return roomInfo;
    }
}