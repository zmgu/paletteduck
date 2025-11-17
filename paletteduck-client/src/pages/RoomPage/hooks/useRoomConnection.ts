import { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { getPlayerInfo } from '../../../utils/apiClient';
import apiClient from '../../../utils/apiClient';
import { wsClient } from '../../../utils/wsClient';
import { WS_DESTINATIONS, WS_TOPICS } from '../../../constants/wsDestinations';
import type { RoomInfo, ChatMessage } from '../../../types/game.types';

export const useRoomConnection = (roomId: string) => {
  const navigate = useNavigate();
  const [roomInfo, setRoomInfo] = useState<RoomInfo | null>(null);
  const [currentPlayerId, setCurrentPlayerId] = useState('');
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
  const [hasJoined, setHasJoined] = useState(false);
  const isGameStarting = useRef(false); // 게임 시작 플래그 추가

  useEffect(() => {
    const playerInfo = getPlayerInfo();
    
    if (!playerInfo) {
      navigate('/');
      return;
    }
    
    setCurrentPlayerId(playerInfo.playerId);
    joinAndLoadRoom();

    return () => {
      // 게임 시작으로 인한 페이지 이동이 아닐 때만 방 나가기 처리
      if (!isGameStarting.current) {
        handleLeaveRoom();
        wsClient.disconnect();
      }
    };
  }, [roomId, navigate]);

  const joinAndLoadRoom = async () => {
    try {
      await apiClient.post(`/room/${roomId}/join`);
      setHasJoined(true);
      
      const { data } = await apiClient.get<RoomInfo>(`/room/${roomId}`);
      setRoomInfo(data);
      
      connectWebSocket();
    } catch (err) {
      console.error('Failed to join room', err);
      alert('방 입장에 실패했습니다.');
      navigate('/main');
    }
  };

  const connectWebSocket = () => {
    wsClient.connect(() => {
      const playerInfo = getPlayerInfo();
      if (!playerInfo) return;

      // 세션 등록
      wsClient.send(WS_DESTINATIONS.ROOM_REGISTER(roomId), playerInfo.playerId);
      
      // 입장 메시지
      wsClient.send(WS_DESTINATIONS.ROOM_CHAT(roomId), {
        playerId: '',
        nickname: '',
        message: `${playerInfo.nickname}님이 입장하셨습니다.`,
        type: 'SYSTEM'
      });
      
      // 방 정보 갱신
      wsClient.send(WS_DESTINATIONS.ROOM_UPDATE(roomId));
      
      // 구독 설정
      wsClient.subscribe(WS_TOPICS.ROOM(roomId), (data: RoomInfo) => {
        setRoomInfo(data);
      });

      wsClient.subscribe(WS_TOPICS.ROOM_CHAT(roomId), (data: ChatMessage) => {
        setChatMessages(prev => [...prev, data]);
      });

      wsClient.subscribe(WS_TOPICS.ROOM_START(roomId), (data: RoomInfo) => {
        if (data.status === 'PLAYING') {
          isGameStarting.current = true; // 게임 시작 플래그 설정
          navigate(`/room/${roomId}/game`);
        }
      });
    });
  };

  const handleLeaveRoom = async () => {
    if (!hasJoined) return;
    
    try {
      await apiClient.post(`/room/${roomId}/leave`);
      
      const playerInfo = getPlayerInfo();
      if (playerInfo) {
        wsClient.send(WS_DESTINATIONS.ROOM_CHAT(roomId), {
          playerId: '',
          nickname: '',
          message: `${playerInfo.nickname}님이 방을 나갔습니다.`,
          type: 'SYSTEM'
        });
        
        wsClient.send(WS_DESTINATIONS.ROOM_UPDATE(roomId));
      }
    } catch (err) {
      console.error('Failed to leave room', err);
    }
  };

  return {
    roomInfo,
    currentPlayerId,
    chatMessages,
    setChatMessages,
  };
};