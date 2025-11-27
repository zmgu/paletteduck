import { useEffect, useState } from 'react';
import { wsClient } from '../../../utils/wsClient';
import { WS_TOPICS, WS_DESTINATIONS } from '../../../constants/wsDestinations';
import { getPlayerInfo } from '../../../utils/apiClient';

export const useCanvasClear = (roomId: string) => {
  const [clearSignal, setClearSignal] = useState(0);

  useEffect(() => {
    // ✅ 매번 최신 playerInfo를 가져옴 (도중 참가 관전자 대응)
    const playerInfo = getPlayerInfo();
    if (!playerInfo || !roomId) {
      console.warn('[useCanvasClear] Missing playerInfo or roomId!');
      return;
    }

    let unsubscribe: (() => void) | undefined;
    wsClient.connect(() => {
      console.log(`[useCanvasClear] Subscribing to GAME_CLEAR - roomId: ${roomId}`);
      unsubscribe = wsClient.subscribe(WS_TOPICS.GAME_CLEAR(roomId), () => {
        console.log('[useCanvasClear] Clear signal received!');
        setClearSignal(prev => prev + 1);
      });
    });

    return () => {
      if (unsubscribe) {
        console.log(`[useCanvasClear] Unsubscribing from GAME_CLEAR - roomId: ${roomId}`);
        unsubscribe();
      }
    };
  }, [roomId]);

  const clearCanvas = () => {
    const playerInfo = getPlayerInfo();
    if (!playerInfo) return;
    setClearSignal(prev => prev + 1);
    wsClient.send(WS_DESTINATIONS.GAME_CLEAR(roomId), playerInfo.playerId);
  };

  return {
    clearSignal,
    clearCanvas,
  };
};