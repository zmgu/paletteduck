import { useEffect, useState } from 'react';
import { wsClient } from '../../../utils/wsClient';
import { WS_TOPICS, WS_DESTINATIONS } from '../../../constants/wsDestinations';
import type { GameState, DrawData } from '../../../types/game.types';
import { getPlayerInfo } from '../../../utils/apiClient';

interface UseGameWebSocketProps {
  roomId: string;
  onGameStateUpdate: (state: GameState) => void;
}

export const useGameWebSocket = ({ roomId, onGameStateUpdate }: UseGameWebSocketProps) => {
  const [drawData, setDrawData] = useState<DrawData | null>(null);
  const [clearSignal, setClearSignal] = useState(0);
  const playerInfo = getPlayerInfo();

  useEffect(() => {
    if (!playerInfo || !roomId) return;

    wsClient.subscribe(WS_TOPICS.GAME_STATE(roomId), (data: GameState) => {
      console.log('Game state updated:', data);
      onGameStateUpdate(data);
    });

    wsClient.subscribe(WS_TOPICS.GAME_DRAW(roomId), (data: DrawData) => {
      setDrawData(data);
    });

    wsClient.subscribe(WS_TOPICS.GAME_CLEAR(roomId), () => {
      setClearSignal(prev => prev + 1);
    });
  }, [roomId, playerInfo, onGameStateUpdate]);

  const selectWord = (word: string) => {
    if (!playerInfo) return;
    wsClient.send(WS_DESTINATIONS.GAME_WORD_SELECT(roomId), {
      playerId: playerInfo.playerId,
      word,
    });
  };

  const handleDrawComplete = (data: DrawData) => {
    if (!playerInfo) return;
    wsClient.send(WS_DESTINATIONS.GAME_DRAW(roomId), {
      ...data,
      playerId: playerInfo.playerId,
    });
  };

  const handleClearCanvas = () => {
    if (!playerInfo) return;
    setClearSignal(prev => prev + 1);
    wsClient.send(WS_DESTINATIONS.GAME_CLEAR(roomId), playerInfo.playerId);
  };

  return {
    drawData,
    clearSignal,
    selectWord,
    handleDrawComplete,
    handleClearCanvas,
  };
};