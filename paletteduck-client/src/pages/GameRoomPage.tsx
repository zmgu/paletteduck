import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getPlayerInfo } from '../utils/apiClient';
import apiClient from '../utils/apiClient';
import { wsClient } from '../utils/wsClient';
import { WS_DESTINATIONS, WS_TOPICS } from '../constants/wsDestinations';
import type { RoomInfo } from '../types/game.types';

export default function GameRoomPage() {
  const { roomId } = useParams<{ roomId: string }>();
  const navigate = useNavigate();
  const [roomInfo, setRoomInfo] = useState<RoomInfo | null>(null);

  useEffect(() => {
    const playerInfo = getPlayerInfo();
    
    if (!playerInfo) {
      navigate('/');
      return;
    }
    
    loadRoomInfo();
    connectWebSocket(playerInfo.playerId);

    return () => {
      // 게임 페이지에서는 WebSocket 유지
    };
  }, [roomId, navigate]);

  const loadRoomInfo = async () => {
    try {
      const { data } = await apiClient.get<RoomInfo>(`/room/${roomId}`);
      
      if (data.status !== 'PLAYING') {
        navigate(`/room/${roomId}/lobby`);
        return;
      }
      
      setRoomInfo(data);
    } catch (err) {
      console.error('Failed to load room', err);
      navigate('/main');
    }
  };

  const connectWebSocket = (playerId: string) => {
    // WebSocket이 이미 연결되어 있으면 재사용
    if (!wsClient.isConnected()) {
      wsClient.connect(() => {
        setupSubscriptions(playerId);
      });
    } else {
      setupSubscriptions(playerId);
    }
  };

  const setupSubscriptions = (playerId: string) => {
    // 세션 재등록
    wsClient.send(WS_DESTINATIONS.ROOM_REGISTER(roomId!), playerId);

    // 방 정보 구독
    wsClient.subscribe(WS_TOPICS.ROOM(roomId!), (data: RoomInfo) => {
      setRoomInfo(data);
    });
  };

  if (!roomInfo) {
    return <div>로딩 중...</div>;
  }

  return (
    <div style={{ padding: '20px' }}>
      <h1>게임 진행 중 - {roomInfo.roomId}</h1>
      <p>게임 페이지 (구현 예정)</p>
      <div style={{ marginTop: '20px', padding: '20px', border: '1px solid #ccc', borderRadius: '8px' }}>
        <h2>참가자 목록</h2>
        <ul>
          {roomInfo.players.filter(p => p.role === 'PLAYER').map(player => (
            <li key={player.playerId}>
              {player.host && '👑 '}
              {player.nickname} - {player.score}점
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}