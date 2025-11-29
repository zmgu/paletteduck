package com.unduck.paletteduck.domain.game.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameState {
    private String roomId;              // 방 ID
    private String gameSessionId;       // 게임 세션 ID (게임 재시작 시 변경됨)
    private int currentRound;           // 현재 라운드 (1부터 시작)
    private int totalRounds;            // 전체 라운드 수
    private GamePhase phase;            // 현재 진행 단계
    private TurnInfo currentTurn;       // 현재 턴 정보
    private List<String> turnOrder;     // 출제자 순서 (playerId 목록)
    private long phaseStartTime;        // 현재 phase 시작 시각 (밀리초)
    private int drawTime;               // 그리기 제한 시간 (초)
    private List<Player> players;       // 플레이어 정보 추가

    public GameState(String roomId, int totalRounds, int drawTime, List<String> turnOrder) {
        this.roomId = roomId;
        this.gameSessionId = java.util.UUID.randomUUID().toString(); // 새 게임 시작 시 고유 ID 생성
        this.currentRound = 1;
        this.totalRounds = totalRounds;
        this.phase = GamePhase.COUNTDOWN;
        this.turnOrder = new ArrayList<>(turnOrder);
        this.phaseStartTime = System.currentTimeMillis();
        this.drawTime = drawTime;
        this.players = new ArrayList<>();
    }
}