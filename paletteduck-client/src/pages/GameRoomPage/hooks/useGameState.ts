import { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { wsClient } from '../../../utils/wsClient';
import { WS_TOPICS } from '../../../constants/wsDestinations';
import type { GameState } from '../../../types/game.types';
import { getPlayerInfo } from '../../../utils/apiClient';

export const useGameState = (roomId: string) => {
  const location = useLocation();
  const [gameState, setGameState] = useState<GameState | null>(
    location.state?.gameState || null  // 초기 상태 복원
  );
  const [timeLeft, setTimeLeft] = useState(0);
  const playerInfo = getPlayerInfo();

  useEffect(() => {
    
    if (!playerInfo || !roomId) {
      console.error('[useGameState] Missing playerInfo or roomId!');
      return;
    }

    const timer = setTimeout(() => {
      wsClient.subscribe(WS_TOPICS.GAME_STATE(roomId), (data: GameState) => {
        setGameState(data);
      });
    }, 100);

    return () => {
      clearTimeout(timer);
    };
  }, [roomId, playerInfo]);

  // 타이머 카운트다운
  useEffect(() => {
    if (!gameState) return;

    const calculateTimeLeft = () => {
      const now = Date.now();
      const elapsed = Math.floor((now - gameState.phaseStartTime) / 1000);
      
      let maxTime = 0;
      if (gameState.phase === 'COUNTDOWN') {
        maxTime = 3;
      } else if (gameState.phase === 'WORD_SELECT') {
        maxTime = 15;
      } else if (gameState.phase === 'DRAWING') {
        maxTime = gameState.drawTime;
      }
      
      const remaining = Math.max(0, maxTime - elapsed);
      setTimeLeft(remaining);
      return remaining;
    };

    calculateTimeLeft();

    const interval: number = setInterval(() => {
      const remaining = calculateTimeLeft();
      if (remaining === 0) {
        clearInterval(interval);
      }
    }, 1000) as unknown as number;

    return () => {
      clearInterval(interval);
    };
  }, [gameState]);

  return {
    gameState,
    setGameState,
    timeLeft,
  };
};