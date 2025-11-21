package com.unduck.paletteduck.domain.game.service;

import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.dto.Player;
import com.unduck.paletteduck.domain.game.repository.GameRepository;
import com.unduck.paletteduck.domain.room.dto.RoomInfo;
import com.unduck.paletteduck.domain.room.dto.RoomPlayer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;

    public GameState initializeGame(String roomId, RoomInfo roomInfo) {
        log.info("Initializing game - roomId: {}", roomId);

        // RoomInfo 플레이어 확인
        log.info("RoomInfo total players: {}", roomInfo.getPlayers().size());

        // ✅ 방장 찾기
        String hostId = roomInfo.getPlayers().stream()
                .filter(RoomPlayer::isHost)
                .map(RoomPlayer::getPlayerId)
                .findFirst()
                .orElse(null);

        log.info("Host player ID: {}", hostId);

        // 플레이어 목록 생성
        List<Player> players = new ArrayList<>();

        for (var p : roomInfo.getPlayers()) {
            log.info("Processing player: {}, role: {}", p.getNickname(), p.getRole());

            Player player = Player.builder()
                    .playerId(p.getPlayerId())
                    .playerName(p.getNickname())
                    .score(0)
                    .isCorrect(false)
                    .build();
            players.add(player);
            log.info("  Added player: {}", player.getPlayerName());
        }

        log.info("Players created: {}", players.size());

        // 턴 순서 생성
        List<String> turnOrder = new ArrayList<>();
        for (Player p : players) {
            turnOrder.add(p.getPlayerId());
        }

        log.info("Turn order created: {}", turnOrder.size());

        // GameState 생성
        GameState gameState = new GameState(
                roomId,
                roomInfo.getSettings().getRounds(),
                roomInfo.getSettings().getDrawTime(),
                turnOrder
        );

        // ✅ hostId 설정
        gameState.setHostId(hostId);

        // 플레이어 설정
        gameState.setPlayers(players);

        // 저장
        gameRepository.save(roomId, gameState);

        log.info("Game initialized - roomId: {}, players: {}, rounds: {}, turnOrder: {}, hostId: {}",
                roomId, players.size(), gameState.getTotalRounds(), gameState.getTurnOrder().size(), hostId);

        return gameState;
    }

    public void updateGameState(String roomId, GameState gameState) {
        gameRepository.save(roomId, gameState);
        log.debug("Game state updated - roomId: {}", roomId);
    }

    public GameState getGameState(String roomId) {
        return gameRepository.findById(roomId);
    }

    public void deleteGame(String roomId) {
        gameRepository.delete(roomId);
    }
}