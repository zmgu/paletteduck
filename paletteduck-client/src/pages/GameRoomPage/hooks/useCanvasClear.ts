import { useEffect, useState } from 'react';
import { wsClient } from '../../../utils/wsClient';
import { WS_TOPICS, WS_DESTINATIONS } from '../../../constants/wsDestinations';
import { getPlayerInfo } from '../../../utils/apiClient';

export const useCanvasClear = (roomId: string) => {
  const [clearSignal, setClearSignal] = useState(0);
  const playerInfo = getPlayerInfo();

  useEffect(() => {
    if (!playerInfo || !roomId) return;

    const timer = setTimeout(() => {
      wsClient.subscribe(WS_TOPICS.GAME_CLEAR(roomId), () => {
        setClearSignal(prev => prev + 1);
      });
    }, 100);

    return () => {
      clearTimeout(timer);
    };
  }, [roomId, playerInfo]);

  const clearCanvas = () => {
    if (!playerInfo) return;
    setClearSignal(prev => prev + 1);
    wsClient.send(WS_DESTINATIONS.GAME_CLEAR(roomId), playerInfo.playerId);
  };

  return {
    clearSignal,
    clearCanvas,
  };
};