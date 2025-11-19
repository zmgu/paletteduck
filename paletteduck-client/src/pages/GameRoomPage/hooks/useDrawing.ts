import { useEffect, useState } from 'react';
import { wsClient } from '../../../utils/wsClient';
import { WS_TOPICS, WS_DESTINATIONS } from '../../../constants/wsDestinations';
import { getPlayerInfo } from '../../../utils/apiClient';
import type { DrawingData } from '../../../types/drawing.types';

export const useDrawing = (roomId: string) => {
  const [drawingData, setDrawingData] = useState<DrawingData | null>(null);
  const playerInfo = getPlayerInfo();

  useEffect(() => {
    if (!playerInfo || !roomId) return;

    const timer = setTimeout(() => {
      wsClient.subscribe(WS_TOPICS.GAME_DRAWING(roomId), (data: DrawingData) => {
        setDrawingData(data);
      });
    }, 100);

    return () => {
      clearTimeout(timer);
    };
  }, [roomId, playerInfo]);

  const sendDrawing = (data: Omit<DrawingData, 'playerId'>) => {
    if (!playerInfo) return;
    
    wsClient.send(WS_DESTINATIONS.GAME_DRAWING(roomId), {
      ...data,
      playerId: playerInfo.playerId,
    });
  };

  return {
    drawingData,
    sendDrawing,
  };
};