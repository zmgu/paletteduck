import { useEffect, useState } from 'react';
import { wsClient } from '../../../utils/wsClient';
import { WS_TOPICS } from '../../../constants/wsDestinations';
import type { RoomInfo } from '../../../types/game.types';
import apiClient from '../../../utils/apiClient';

export const useRoomInfo = (roomId: string, initialRoomInfo?: RoomInfo | null) => {
  const [roomInfo, setRoomInfo] = useState<RoomInfo | null>(initialRoomInfo || null);

  useEffect(() => {
    if (!roomId) return;

    // 초기 roomInfo가 없으면 REST API로 fetch
    if (!initialRoomInfo) {
      apiClient.get(`/room/${roomId}`)
        .then(response => {
          setRoomInfo(response.data);
        })
        .catch(error => {
          console.error('Failed to fetch room info:', error);
        });
    }

    // WebSocket 연결 후 RoomInfo 구독 (관전자 목록 실시간 갱신을 위해 필수)
    let unsubscribe: (() => void) | undefined;
    wsClient.connect(() => {
      unsubscribe = wsClient.subscribe(WS_TOPICS.ROOM(roomId), (data: RoomInfo | null) => {
        setRoomInfo(data); // null도 허용하여 방 삭제 감지
      });
    });

    return () => {
      if (unsubscribe) unsubscribe();
    };
  }, [roomId, initialRoomInfo]);

  return roomInfo;
};
