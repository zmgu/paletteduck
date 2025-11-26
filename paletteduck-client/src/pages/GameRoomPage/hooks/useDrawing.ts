import { useEffect, useState, useRef } from 'react';
import { useLocation } from 'react-router-dom';
import { wsClient } from '../../../utils/wsClient';
import { WS_TOPICS, WS_DESTINATIONS } from '../../../constants/wsDestinations';
import { getPlayerInfo } from '../../../utils/apiClient';
import type { DrawingData } from '../../../types/drawing.types';

export const useDrawing = (roomId: string) => {
  const location = useLocation();
  const [drawingData, setDrawingData] = useState<DrawingData | null>(null);
  const [initialDrawingEvents, setInitialDrawingEvents] = useState<DrawingData[]>([]);
  const hasLoadedInitial = useRef(false);
  const playerInfo = getPlayerInfo();

  // 초기 그림 데이터 로드 (도중 참가자를 위해)
  useEffect(() => {
    if (hasLoadedInitial.current) return;

    const gameState = location.state?.gameState;
    if (gameState?.currentTurn?.drawingEvents) {
      setInitialDrawingEvents(gameState.currentTurn.drawingEvents);
      hasLoadedInitial.current = true;
    }
  }, [location.state]);

  useEffect(() => {
    if (!playerInfo || !roomId) return;

    let unsubscribe: (() => void) | undefined;
    wsClient.connect(() => {
      unsubscribe = wsClient.subscribe(WS_TOPICS.GAME_DRAWING(roomId), (data: DrawingData) => {
        setDrawingData(data);
      });
    });

    return () => {
      if (unsubscribe) unsubscribe();
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
    initialDrawingEvents,
  };
};