import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { wsClient } from '../../../utils/wsClient';
import { WS_DESTINATIONS, WS_TOPICS } from '../../../constants/wsDestinations';
import type { RoomInfo, ChatMessage, GameState } from '../../../types/game.types';
import { getPlayerInfo } from '../../../utils/apiClient';
import apiClient from '../../../utils/apiClient';

export const useRoomConnection = (roomId: string) => {
  const navigate = useNavigate();
  const isGameStarting = useRef(false);
  const [roomInfo, setRoomInfo] = useState<RoomInfo | null>(null);
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
  const [loading, setLoading] = useState(true);
  
  const playerInfo = getPlayerInfo();

  useEffect(() => {
    if (!playerInfo) {
      navigate('/');
      return;
    }

    joinAndLoadRoom();

    return () => {
      if (!isGameStarting.current) {
        handleLeaveRoom();
        wsClient.disconnect();
      }
    };
  }, [roomId]);

  const joinAndLoadRoom = async () => {
    if (!playerInfo) return;

    try {
      // 1. REST API로 방 입장 (입장 메시지는 서버에서 브로드캐스트)
      await apiClient.post(`/room/${roomId}/join`);
      
      // 2. 방 정보 로드
      const response = await apiClient.get(`/room/${roomId}`);
      setRoomInfo(response.data);
      
      // 3. WebSocket 연결
      connectWebSocket();
      
      setLoading(false);
    } catch (error) {
      console.error('Failed to join room:', error);
      alert('방 입장에 실패했습니다.');
      navigate('/');
    }
  };

  const connectWebSocket = () => {
    if (!playerInfo) return;

    wsClient.connect(() => {
      const currentPlayerInfo = getPlayerInfo();
      if (!currentPlayerInfo) return;

      const { playerId } = currentPlayerInfo;

      // 세션 등록
      wsClient.send(WS_DESTINATIONS.ROOM_REGISTER(roomId), playerId);
      
      // 방 정보 갱신 요청
      wsClient.send(WS_DESTINATIONS.ROOM_UPDATE(roomId));

      // 구독 설정
      wsClient.subscribe(WS_TOPICS.ROOM(roomId), (data: RoomInfo) => setRoomInfo(data));
      wsClient.subscribe(WS_TOPICS.ROOM_CHAT(roomId), (data: ChatMessage) =>
        setChatMessages((prev) => [...prev, data])
      );

      wsClient.subscribe(WS_TOPICS.ROOM_START(roomId), (data: RoomInfo) => {
        if (data.status === 'PLAYING') {
          setRoomInfo(data);
        }
      });

      wsClient.subscribe(WS_TOPICS.GAME_START(roomId), (data: GameState) => {
        isGameStarting.current = true;
        console.log('Game starting with state:', data);
        navigate(`/room/${roomId}/game`, { state: { gameState: data } });
      });
    });
  };

  const handleLeaveRoom = async () => {
    if (!playerInfo) return;

    try {
      await apiClient.post(`/room/${roomId}/leave`);
    } catch (error) {
      console.error('Failed to leave room:', error);
    }
  };

  return {
    roomInfo,
    chatMessages,
    currentPlayerId: playerInfo?.playerId || '',
    currentNickname: playerInfo?.nickname || '',
    loading,
  };
};