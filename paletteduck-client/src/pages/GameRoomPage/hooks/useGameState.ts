import { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { type GameState } from '../../../types/game.types';

export const useGameState = () => {
  const location = useLocation();
  const [gameState, setGameState] = useState<GameState | null>(
    location.state?.gameState || null
  );
  const [timeLeft, setTimeLeft] = useState<number>(0);

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

    const interval = setInterval(() => {
      const remaining = calculateTimeLeft();
      if (remaining === 0) {
        clearInterval(interval);
      }
    }, 1000);

    return () => clearInterval(interval);
  }, [gameState]);

  return {
    gameState,
    setGameState,
    timeLeft,
  };
};