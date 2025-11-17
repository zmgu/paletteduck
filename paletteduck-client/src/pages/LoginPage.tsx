import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import apiClient, { getPlayerInfo } from '../utils/apiClient';
import { getSessionId } from '../utils/sessionManager';
import type { PlayerJoinResponse } from '../types/game.types';

export default function LoginPage() {
  const [nickname, setNickname] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const { roomId } = useParams<{ roomId?: string }>();

  useEffect(() => {
    const playerInfo = getPlayerInfo();
    if (playerInfo && roomId) {
      navigate(`/room/${roomId}/lobby`);
    } else if (playerInfo) {
      navigate('/main');
    }
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!nickname.trim()) {
      setError('닉네임을 입력해주세요');
      return;
    }

    try {
      const sessionId = getSessionId();
      
      const { data } = await apiClient.post<PlayerJoinResponse>('/player/join', {
        nickname: nickname.trim()
      });
      
      sessionStorage.setItem('paletteduck_token', data.token);
      
      if (roomId) {
        navigate(`/room/${roomId}/lobby`);
      } else {
        navigate('/main');
      }
    } catch (err) {
      setError('입장에 실패했습니다');
    }
  };

  return (
    <div style={{ 
      display: 'flex', 
      flexDirection: 'column', 
      alignItems: 'center', 
      justifyContent: 'center', 
      height: '100vh' 
    }}>
      <h1>PaletteDuck</h1>
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
        <input
          type="text"
          value={nickname}
          onChange={(e) => setNickname(e.target.value)}
          placeholder="닉네임 입력"
          maxLength={10}
          style={{ padding: '10px', fontSize: '16px' }}
        />
        {error && <span style={{ color: 'red' }}>{error}</span>}
        <button type="submit" style={{ padding: '10px', fontSize: '16px' }}>
          입장
        </button>
      </form>
    </div>
  );
}