package com.unduck.paletteduck.domain.room.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unduck.paletteduck.domain.game.dto.GameSettings;
import com.unduck.paletteduck.domain.room.dto.*;
import com.unduck.paletteduck.domain.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    /**
     * 방 생성
     */
    public RoomCreateResponse createRoom(String playerId, String nickname, boolean isPublic) {
        String roomId = generateRoomId();

        RoomPlayer host = createHost(playerId, nickname);
        List<RoomPlayer> players = new ArrayList<>();
        players.add(host);

        GameSettings settings = new GameSettings();
        RoomInfo roomInfo = new RoomInfo(roomId, roomId, players, settings, RoomStatus.WAITING, isPublic);

        log.info("Creating room - roomId: {}, host: {}, isPublic: {}", roomId, nickname, isPublic);
        log.debug("RoomInfo before save - isPublic: {}", roomInfo.isPublic());

        roomRepository.save(roomId, roomInfo);

        // 저장 후 검증
        RoomInfo savedRoom = roomRepository.findById(roomId);
        log.info("Room created and verified - roomId: {}, host: {}, isPublic: {}",
                roomId, nickname, savedRoom != null ? savedRoom.isPublic() : "null");

        return new RoomCreateResponse(roomId, roomId);
    }

    /**
     * 방 정보 조회
     */
    public RoomInfo getRoomInfo(String roomId) {
        return roomRepository.findById(roomId);
    }

    /**
     * 방 정보 저장
     */
    public void saveRoomInfo(String roomId, RoomInfo roomInfo) {
        roomRepository.save(roomId, roomInfo);
    }

    /**
     * 방 삭제
     */
    public void deleteRoom(String roomId) {
        roomRepository.delete(roomId);
    }

    /**
     * 랜덤 공개방 찾기
     * - 공개방(isPublic=true)
     * - 대기 중(WAITING)
     * - 참가 가능한 자리가 있는 방
     */
    public RoomInfo findRandomPublicRoom() {
        List<RoomInfo> availableRooms = findAvailablePublicRooms();

        if (availableRooms.isEmpty()) {
            log.info("No available public rooms found");
            return null;
        }

        // 랜덤으로 방 선택
        int randomIndex = (int) (Math.random() * availableRooms.size());
        RoomInfo selectedRoom = availableRooms.get(randomIndex);

        log.info("Random public room selected - roomId: {}, isPublic: {}, status: {}, players: {}/{}",
                selectedRoom.getRoomId(),
                selectedRoom.isPublic(),
                selectedRoom.getStatus(),
                countParticipants(selectedRoom),
                selectedRoom.getSettings().getMaxPlayers());

        return selectedRoom;
    }

    /**
     * 입장 가능한 공개방 목록 조회
     */
    private List<RoomInfo> findAvailablePublicRooms() {
        List<RoomInfo> allRooms = roomRepository.findAll();
        List<RoomInfo> availableRooms = new ArrayList<>();

        log.info("Checking {} total rooms for availability", allRooms.size());

        for (RoomInfo room : allRooms) {
            log.debug("Room check - roomId: {}, isPublic: {}, status: {}, participants: {}/{}",
                    room.getRoomId(),
                    room.isPublic(),
                    room.getStatus(),
                    countParticipants(room),
                    room.getSettings().getMaxPlayers());

            if (isRoomAvailable(room)) {
                availableRooms.add(room);
                log.info("Room available for random join - roomId: {}", room.getRoomId());
            }
        }

        log.info("Found {} available public rooms out of {} total rooms",
                availableRooms.size(), allRooms.size());

        return availableRooms;
    }

    /**
     * 방 입장 가능 여부 확인
     */
    private boolean isRoomAvailable(RoomInfo room) {
        if (room == null) {
            log.debug("Room is null");
            return false;
        }

        // 공개방이어야 함
        if (!room.isPublic()) {
            log.debug("Room {} is private, skipping", room.getRoomId());
            return false;
        }

        // 대기 중이어야 함
        if (room.getStatus() != RoomStatus.WAITING) {
            log.debug("Room {} is not in WAITING status: {}", room.getRoomId(), room.getStatus());
            return false;
        }

        // 참가자 수가 최대 참가자 수보다 적어야 함
        int currentParticipants = countParticipants(room);
        int maxPlayers = room.getSettings().getMaxPlayers();

        if (currentParticipants >= maxPlayers) {
            log.debug("Room {} is full: {}/{}", room.getRoomId(), currentParticipants, maxPlayers);
            return false;
        }

        return true;
    }

    /**
     * 참가자 수 계산 (관전자 제외)
     */
    private int countParticipants(RoomInfo room) {
        if (room == null || room.getPlayers() == null) {
            return 0;
        }

        return (int) room.getPlayers().stream()
                .filter(player -> player.getRole() == PlayerRole.PLAYER)
                .count();
    }

    // Private 헬퍼 메서드

    private String generateRoomId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private RoomPlayer createHost(String playerId, String nickname) {
        return new RoomPlayer(playerId, nickname, true, false, PlayerRole.PLAYER, 0, 0, 0, System.currentTimeMillis());
    }
}