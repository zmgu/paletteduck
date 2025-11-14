// src/pages/RoomPage.tsx
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getPlayerInfo } from '../utils/apiClient';
import apiClient from '../utils/apiClient';
import { wsClient } from '../utils/wsClient';
import type { RoomInfo, RoomPlayer } from '../types/game.types';

export default function RoomPage() {
  const { roomId } = useParams<{ roomId: string }>();
  const navigate = useNavigate();
  const [roomInfo, setRoomInfo] = useState<RoomInfo | null>(null);
  const [currentPlayerId, setCurrentPlayerId] = useState('');

  useEffect(() => {
    const playerInfo = getPlayerInfo();
    
    if (!playerInfo) {
      navigate('/');
      return;
    }
    
    setCurrentPlayerId(playerInfo.playerId);
    loadRoomInfo();
    connectWebSocket();

    return () => {
      wsClient.disconnect();
    };
  }, [roomId, navigate]);

  const loadRoomInfo = async () => {
    try {
      const { data } = await apiClient.get<RoomInfo>(`/room/${roomId}`);
      setRoomInfo(data);
    } catch (err) {
      console.error('Failed to load room', err);
      navigate('/main');
    }
  };

  const connectWebSocket = () => {
    wsClient.connect(() => {
      wsClient.subscribe(`/topic/room/${roomId}`, (data: RoomInfo) => {
        setRoomInfo(data);
      });
    });
  };

  const handleCopyInviteCode = () => {
    const inviteUrl = `${window.location.origin}/room/${roomInfo?.inviteCode}`;
    navigator.clipboard.writeText(inviteUrl);
    alert('초대 코드가 복사되었습니다!');
  };

  const isHost = roomInfo?.players.find(p => p.playerId === currentPlayerId)?.isHost || false;

  if (!roomInfo) {
    return <div>로딩 중...</div>;
  }

  return (
    <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
      <h1>대기방 - {roomInfo.roomId}</h1>
      
      <div style={{ display: 'flex', gap: '20px', marginTop: '20px' }}>
        {/* 참가자 목록 */}
        <div style={{ flex: 1, border: '1px solid #ccc', padding: '15px', borderRadius: '8px' }}>
          <h2>참가자 목록</h2>
          <ul style={{ listStyle: 'none', padding: 0 }}>
            {roomInfo.players.map((player: RoomPlayer) => (
              <li 
                key={player.playerId}
                style={{ 
                  padding: '10px',
                  marginBottom: '5px',
                  backgroundColor: player.playerId === currentPlayerId ? '#e3f2fd' : '#f5f5f5',
                  borderRadius: '4px',
                  fontWeight: player.playerId === currentPlayerId ? 'bold' : 'normal'
                }}
              >
                {player.nickname} {player.isHost && '👑'} {player.isReady && '✅'}
              </li>
            ))}
          </ul>
        </div>

        {/* 게임 설정 */}
        <div style={{ flex: 1, border: '1px solid #ccc', padding: '15px', borderRadius: '8px' }}>
          <h2>게임 설정</h2>
          <div style={{ marginTop: '10px' }}>
            <p>라운드 수: 3라운드</p>
            <p>제한 시간: 60초</p>
            <p style={{ color: '#999', fontSize: '14px', marginTop: '10px' }}>
              {isHost ? '설정 변경 기능 준비중' : '방장만 설정 가능'}
            </p>
          </div>
        </div>
      </div>

      {/* 하단 버튼 */}
      <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
        <button 
          onClick={handleCopyInviteCode}
          style={{ flex: 1, padding: '15px', fontSize: '16px' }}
        >
          초대 코드 복사
        </button>
        
        {isHost ? (
          <button 
            style={{ flex: 1, padding: '15px', fontSize: '16px', backgroundColor: '#4caf50', color: 'white' }}
            disabled
          >
            시작하기
          </button>
        ) : (
          <button 
            style={{ flex: 1, padding: '15px', fontSize: '16px' }}
            disabled
          >
            준비 완료
          </button>
        )}
      </div>
    </div>
  );
}