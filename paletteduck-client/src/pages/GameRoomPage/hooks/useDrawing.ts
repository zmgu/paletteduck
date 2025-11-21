import { useEffect, useState } from 'react';
import { wsClient } from '../../../utils/wsClient';
import { WS_TOPICS, WS_DESTINATIONS } from '../../../constants/wsDestinations';
import { getPlayerInfo } from '../../../utils/apiClient';
import type { DrawingData } from '../../../types/drawing.types';

export const useDrawing = (roomId: string) => {
  const [drawingData, setDrawingData] = useState<DrawingData | null>(null);

  useEffect(() => {
    const playerInfo = getPlayerInfo();
    if (!playerInfo || !roomId) return;

    console.log('[useDrawing] Setting up subscription...');
    
    let unsubscribe: (() => void) | null = null;

    const timer = setTimeout(() => {
      console.log('[useDrawing] Subscribing to drawing:', WS_TOPICS.GAME_DRAWING(roomId));
      
      unsubscribe = wsClient.subscribe(WS_TOPICS.GAME_DRAWING(roomId), (data: any) => {
        // ✅ 본인이 보낸 데이터는 무시 (이미 로컬에 그려짐)
        const currentPlayerInfo = getPlayerInfo();
        if (data.playerId === currentPlayerInfo?.playerId) {
          return;
        }
        
        setDrawingData(data);
      });
    }, 100);

    return () => {
      clearTimeout(timer);
      if (unsubscribe) {
        console.log('[useDrawing] Unsubscribing from drawing...');
        unsubscribe();
      }
    };
  }, [roomId]);

  const sendDrawing = (data: Omit<DrawingData, 'playerId'>) => {
    const playerInfo = getPlayerInfo();
    if (!playerInfo || !roomId) return;
    
    // ✅ playerId 포함해서 전송
    const payload = {
      ...data,
      playerId: playerInfo.playerId,
    };
    
    console.log('[useDrawing] Sending drawing data with playerId:', playerInfo.playerId);
    wsClient.send(WS_DESTINATIONS.GAME_DRAWING(roomId), payload);
  };

  return {
    drawingData,
    sendDrawing,
  };
};