import { useEffect, useState, useMemo } from 'react';
import { wsClient } from '../../../utils/wsClient';
import { WS_TOPICS, WS_DESTINATIONS } from '../../../constants/wsDestinations';
import { getPlayerInfo } from '../../../utils/apiClient';

export const useCanvasClear = (roomId: string) => {
  const [clearSignal, setClearSignal] = useState(0);
  const playerInfo = useMemo(() => getPlayerInfo(), []);

  useEffect(() => {
    if (!playerInfo || !roomId) return;

    let unsubscribe: (() => void) | undefined;
    wsClient.connect(() => {
      unsubscribe = wsClient.subscribe(WS_TOPICS.GAME_CLEAR(roomId), () => {
        setClearSignal(prev => prev + 1);
      });
    });

    return () => {
      if (unsubscribe) unsubscribe();
    };
  }, [roomId]);

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