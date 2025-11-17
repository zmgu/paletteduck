import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getPlayerInfo } from '../utils/apiClient';
import apiClient from '../utils/apiClient';
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
  }, [roomId, navigate]);

  const loadRoomInfo = async () => {
    try {
      const { data } = await apiClient.get<RoomInfo>(`/room/${roomId}`);
      
      if (data.status !== 'PLAYING') {
        navigate(`/room/${roomId}`);
        return;
      }
      
      setRoomInfo(data);
    } catch (err) {
      console.error('Failed to load room', err);
      navigate('/main');
    }
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