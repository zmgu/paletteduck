import { useEffect, useState } from 'react';
import { wsClient } from '../../../utils/wsClient';
import { WS_TOPICS, WS_DESTINATIONS } from '../../../constants/wsDestinations';
import type { ChatMessage } from '../../../types/chat.types';
import { getPlayerInfo } from '../../../utils/apiClient';

export const useChat = (roomId: string, turnNumber?: number) => {
  const [messages, setMessages] = useState<ChatMessage[]>([]);

  // 턴이 바뀔 때마다 채팅 초기화
  useEffect(() => {
    if (turnNumber !== undefined) {
      setMessages([]);
    }
  }, [turnNumber]);

  useEffect(() => {
    const playerInfo = getPlayerInfo();
    if (!playerInfo || !roomId) return;

    let unsubscribe: (() => void) | null = null;

    const timer = setTimeout(() => {
      unsubscribe = wsClient.subscribe(WS_TOPICS.ROOM_CHAT(roomId), (data: ChatMessage) => {
        setMessages(prev => [...prev, data]);
      });
    }, 100);

    return () => {
      clearTimeout(timer);
      if (unsubscribe) {
        unsubscribe();
      }
    };
  }, [roomId]);

  const sendMessage = (message: string) => {
    const playerInfo = getPlayerInfo();
    if (!playerInfo || !roomId) return;

    wsClient.send(WS_DESTINATIONS.GAME_CHAT(roomId), {
      playerId: playerInfo.playerId,
      nickname: playerInfo.nickname,
      message,
    });
  };

  return {
    messages,
    sendMessage,
  };
};