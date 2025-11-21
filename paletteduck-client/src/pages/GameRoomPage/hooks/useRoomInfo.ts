import { useEffect, useState } from 'react';
import { wsClient } from '../../../utils/wsClient';
import { WS_TOPICS } from '../../../constants/wsDestinations';

export interface RoomInfo {
  roomId: string;
  inviteCode: string;
  players: Array<{
    playerId: string;
    nickname: string;
    host: boolean;
    ready: boolean;
    role: string;
  }>;
  settings: any;
  status: string;
}

export const useRoomInfo = (roomId: string) => {
  const [roomInfo, setRoomInfo] = useState<RoomInfo | null>(null);

  useEffect(() => {
    if (!roomId) return;

    console.log('[useRoomInfo] Subscribing to room:', roomId);
    
    let unsubscribe: (() => void) | null = null;

    const timer = setTimeout(() => {
      unsubscribe = wsClient.subscribe(`/topic/room/${roomId}`, (data: RoomInfo) => {
        console.log('[useRoomInfo] Room update received:', data);
        setRoomInfo(data);
      });
    }, 100);

    return () => {
      clearTimeout(timer);
      if (unsubscribe) {
        console.log('[useRoomInfo] Unsubscribing...');
        unsubscribe();
      }
    };
  }, [roomId]);

  return { roomInfo };
};