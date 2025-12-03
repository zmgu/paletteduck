package com.unduck.paletteduck.domain.game.service;

import com.unduck.paletteduck.domain.game.dto.GameState;
import com.unduck.paletteduck.domain.game.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 게임 타이머 서비스
 * 단어 선택 등 게임 진행 중 사용자 액션 처리를 담당
 *
 * 비동기 타이머 관련 기능은 AsyncGameTimerScheduler로 분리됨
 * 페이즈 전환 로직은 GamePhaseManager로 분리됨
 * 턴 종료 로직은 TurnManager로 분리됨
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameTimerService {

    private final GameRepository gameRepository;
    private final GamePhaseManager phaseManager;

    /**
     * 단어 선택
     */
    public void selectWord(String roomId, String playerId, String word) {
        GameState gameState = gameRepository.findById(roomId);
        if (gameState == null || gameState.getCurrentTurn() == null) {
            return;
        }

        // 출제자 본인인지 확인
        if (!gameState.getCurrentTurn().getDrawerId().equals(playerId)) {
            log.warn("Unauthorized word selection - playerId: {}, drawer: {}",
                    playerId, gameState.getCurrentTurn().getDrawerId());
            return;
        }

        // 선택지에 있는 단어인지 확인, 또는 직접 입력한 유효한 단어인지 확인
        boolean isInWordChoices = gameState.getCurrentTurn().getWordChoices().contains(word);
        boolean isValidCustomWord = word != null && word.matches("^[가-힣ㄱ-ㅎㅏ-ㅣ]{2,10}$");

        if (!isInWordChoices && !isValidCustomWord) {
            log.warn("Invalid word selection - word: {}, isInChoices: {}, isValidCustom: {}",
                    word, isInWordChoices, isValidCustomWord);
            return;
        }

        gameState.getCurrentTurn().setWord(word);
        gameRepository.save(roomId, gameState);

        if (isInWordChoices) {
            log.info("Word selected from choices - room: {}, word: {}", roomId, word);
        } else {
            log.info("Custom word selected - room: {}, word: {}", roomId, word);
        }

        // 즉시 그리기 단계로 전환
        phaseManager.startDrawingPhase(roomId, gameState);
    }
}
