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
    private String roomId;
    private String hostId;
    private int currentRound;
    private int totalRounds;
    private GamePhase phase;
    private TurnInfo currentTurn;
    private List<String> turnOrder;
    private long phaseStartTime;
    private int drawTime;
    private List<Player> players;

    public GameState(String roomId, int totalRounds, int drawTime, List<String> turnOrder) {
        this.roomId = roomId;
        this.currentRound = 1;
        this.totalRounds = totalRounds;
        this.phase = GamePhase.COUNTDOWN;
        this.turnOrder = new ArrayList<>(turnOrder);
        this.phaseStartTime = System.currentTimeMillis();
        this.drawTime = drawTime;
        this.players = new ArrayList<>();
        this.hostId = null;
    }
}