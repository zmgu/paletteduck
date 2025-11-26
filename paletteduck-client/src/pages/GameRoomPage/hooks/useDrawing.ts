import { useEffect, useState, useRef } from 'react';
import { wsClient } from '../../../utils/wsClient';
import { WS_TOPICS, WS_DESTINATIONS } from '../../../constants/wsDestinations';
import { getPlayerInfo } from '../../../utils/apiClient';
import type { DrawingData } from '../../../types/drawing.types';
import type { GameState } from '../../../types/game.types';

export const useDrawing = (roomId: string, gameState?: GameState | null) => {
  const [drawingData, setDrawingData] = useState<DrawingData | null>(null);
  const [initialDrawingEvents, setInitialDrawingEvents] = useState<DrawingData[]>([]);
  const currentTurnNumber = useRef<number | null>(null);
  const hasLoadedEvents = useRef(false);
  const lastPlayerIdRef = useRef<string | null>(null);

  // 초기 그림 데이터 로드 (도중 참가자를 위해)
  useEffect(() => {
    const turnNumber = gameState?.currentTurn?.turnNumber;
    const drawingEvents = gameState?.currentTurn?.drawingEvents;
    const drawingEventsLength = drawingEvents?.length || 0;

    // 턴이 바뀌면 초기화
    if (turnNumber !== undefined && turnNumber !== currentTurnNumber.current) {
      currentTurnNumber.current = turnNumber;
      hasLoadedEvents.current = false;
      setInitialDrawingEvents([]);

      // 새 턴의 그림 이벤트가 있으면 설정
      if (drawingEvents && drawingEvents.length > 0) {
        console.log(`[useDrawing] Loading ${drawingEvents.length} drawing events for turn ${turnNumber}`);
        setInitialDrawingEvents(drawingEvents);
        hasLoadedEvents.current = true;
      }
    }
    // 같은 턴이지만 drawingEvents가 새로 들어온 경우 (도중 참가)
    else if (turnNumber === currentTurnNumber.current &&
             !hasLoadedEvents.current &&
             drawingEventsLength > 0) {
      console.log(`[useDrawing] Mid-game join - Loading ${drawingEventsLength} drawing events for turn ${turnNumber}`);
      setInitialDrawingEvents(drawingEvents!);
      hasLoadedEvents.current = true;
    }
  }, [gameState?.currentTurn?.turnNumber, gameState?.currentTurn?.drawingEvents?.length]);

  useEffect(() => {
    // ✅ 매번 최신 playerInfo를 가져옴 (관전자 -> 참가자 전환 대응)
    const playerInfo = getPlayerInfo();
    if (!playerInfo || !roomId) return;

    // playerInfo가 변경되었는지 확인 (도중 참가 시 재구독)
    const currentPlayerId = playerInfo.playerId;
    const hasPlayerChanged = lastPlayerIdRef.current !== currentPlayerId;

    if (hasPlayerChanged) {
      console.log(`[useDrawing] PlayerInfo changed: ${lastPlayerIdRef.current} -> ${currentPlayerId}`);
      lastPlayerIdRef.current = currentPlayerId;
    }

    let unsubscribe: (() => void) | undefined;
    wsClient.connect(() => {
      unsubscribe = wsClient.subscribe(WS_TOPICS.GAME_DRAWING(roomId), (data: DrawingData) => {
        setDrawingData(data);
      });
    });

    return () => {
      if (unsubscribe) unsubscribe();
    };
  }, [roomId, gameState?.phase]); // gameState.phase를 의존성에 추가하여 페이즈 변경 시에도 재구독

  const sendDrawing = (data: Omit<DrawingData, 'playerId'>) => {
    const playerInfo = getPlayerInfo();
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