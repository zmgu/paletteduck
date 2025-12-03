package com.unduck.paletteduck.domain.game.service;

import com.unduck.paletteduck.config.constants.WebSocketTopics;
import com.unduck.paletteduck.domain.game.dto.GamePhase;
import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.dto.TurnEndReason;
import com.unduck.paletteduck.domain.game.event.DrawingTimeoutEvent;
import com.unduck.paletteduck.domain.game.event.TurnEndEvent;
import com.unduck.paletteduck.domain.game.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * 턴 관리 서비스
 * 턴 종료 처리를 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TurnManager {

    private final GameRepository gameRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AsyncGameTimerScheduler timerScheduler;

    /**
     * 턴 종료
     */
    public void endTurn(String roomId, GameState gameState, TurnEndReason reason) {
        // 턴 종료 사유 설정
        if (gameState.getCurrentTurn() != null) {
            gameState.getCurrentTurn().setTurnEndReason(reason);
        }

        // 턴 결과 단계로 전환
        gameState.setPhase(GamePhase.TURN_RESULT);
        gameState.setPhaseStartTime(System.currentTimeMillis());

        gameRepository.save(roomId, gameState);

        // 턴 결과 브로드캐스트
        messagingTemplate.convertAndSend(WebSocketTopics.gameState(roomId), gameState);

        log.info("Turn ended, showing results - room: {}, turn: {}, reason: {}",
                roomId, gameState.getCurrentTurn().getTurnNumber(), reason);

        // 턴 결과 표시 후 다음 턴 준비
        timerScheduler.scheduleTurnResultEnd(roomId);
    }

    // ========== 이벤트 리스너 ==========

    /**
     * 그리기 시간 초과 이벤트 처리
     */
    @EventListener
    public void onDrawingTimeout(DrawingTimeoutEvent event) {
        endTurn(event.getRoomId(), event.getGameState(), TurnEndReason.TIME_OUT);
    }

    /**
     * 턴 종료 이벤트 처리
     */
    @EventListener
    public void onTurnEnd(TurnEndEvent event) {
        endTurn(event.getRoomId(), event.getGameState(), event.getReason());
    }
}
