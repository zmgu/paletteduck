import { useEffect, useState } from 'react';
import { wsClient } from '../../../utils/wsClient';
import { WS_TOPICS, WS_DESTINATIONS } from '../../../constants/wsDestinations';
import { getPlayerInfo } from '../../../utils/apiClient';

export const useCanvasClear = (roomId: string) => {
  const [clearSignal, setClearSignal] = useState(0);

  useEffect(() => {
    const playerInfo = getPlayerInfo();
    if (!playerInfo || !roomId) return;

    console.log('[useCanvasClear] Setting up subscription...');
    
    let unsubscribe: (() => void) | null = null;

    const timer = setTimeout(() => {
      console.log('[useCanvasClear] Subscribing to clear:', WS_TOPICS.GAME_CLEAR(roomId));
      
      unsubscribe = wsClient.subscribe(WS_TOPICS.GAME_CLEAR(roomId), () => {
        console.log('[useCanvasClear] Clear signal received - incrementing clearSignal');
        setClearSignal(prev => {
          const newSignal = prev + 1;
          console.log('[useCanvasClear] clearSignal updated:', newSignal);
          return newSignal;
        });
      });
    }, 100);

    return () => {
      clearTimeout(timer);
      if (unsubscribe) {
        console.log('[useCanvasClear] Unsubscribing from clear...');
        unsubscribe();
      }
    };
  }, [roomId]);

  const clearCanvas = () => {
    const playerInfo = getPlayerInfo();
    if (!playerInfo || !roomId) return;
    
    console.log('[useCanvasClear] Sending clear signal with playerId:', playerInfo.playerId);
    
    // ✅ playerId 포함해서 전송
    wsClient.send(WS_DESTINATIONS.GAME_CLEAR(roomId), {
      playerId: playerInfo.playerId,
    });
  };

  return {
    clearSignal,
    clearCanvas,
  };
};