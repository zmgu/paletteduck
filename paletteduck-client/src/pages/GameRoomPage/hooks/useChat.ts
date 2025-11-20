import { useEffect, useState } from 'react';
import { wsClient } from '../../../utils/wsClient';
import { WS_TOPICS, WS_DESTINATIONS } from '../../../constants/wsDestinations';
import type { ChatMessage } from '../../../types/chat.types';
import { getPlayerInfo } from '../../../utils/apiClient';

export const useChat = (roomId: string) => {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const playerInfo = getPlayerInfo();

  useEffect(() => {
    if (!playerInfo || !roomId) return;
    
    let unsubscribe: (() => void) | null = null;

    const timer = setTimeout(() => {
      
      // ✅ unsubscribe 함수 받기
      unsubscribe = wsClient.subscribe(WS_TOPICS.ROOM_CHAT(roomId), (data: ChatMessage) => {
          console.log('=============================');
  console.log('[useChat] Message received!!!');
  console.log('[useChat] Type:', data.type);
  console.log('[useChat] Message:', data.message);
  console.log('[useChat] Nickname:', data.nickname);
  console.log('[useChat] Full data:', JSON.stringify(data, null, 2));
  console.log('=============================');
        setMessages(prev => [...prev, data]);
      });
    }, 100);

    // ✅ cleanup에서 unsubscribe 호출
    return () => {
      clearTimeout(timer);
      if (unsubscribe) {
        unsubscribe();
      }
    };
  }, [roomId, playerInfo]);

  const sendMessage = (message: string) => {
    if (!playerInfo || !roomId) return;
    
    console.log('[useChat] Sending message:', message);
    
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