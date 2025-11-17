// src/pages/MainPage.tsx
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getPlayerInfo } from '../utils/apiClient';
import apiClient from '../utils/apiClient';
import type { RoomCreateResponse } from '../types/game.types';

export default function MainPage() {
  const navigate = useNavigate();
  const [nickname, setNickname] = useState('');

  useEffect(() => {
    const playerInfo = getPlayerInfo();
    
    if (!playerInfo) {
      navigate('/');
      return;
    }
    
    setNickname(playerInfo.nickname);
  }, [navigate]);

  const handleChangeNickname = () => {
    sessionStorage.removeItem('paletteduck_token');
    navigate('/');
  };

  const handleCreateRoom = async () => {
    try {
      const { data } = await apiClient.post<RoomCreateResponse>('/room/create');
      navigate(`/room/${data.roomId}/lobby`);
    } catch (err) {
      console.error('Failed to create room', err);
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h1>PaletteDuck - 메인</h1>
      <p>환영합니다, {nickname}님!</p>
      
      <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginTop: '20px', maxWidth: '300px' }}>
        <button 
          onClick={handleCreateRoom}
          style={{ padding: '15px', fontSize: '16px' }}
        >
          방 만들기
        </button>
        
        <button 
          style={{ padding: '15px', fontSize: '16px' }}
          disabled
        >
          초대코드 입력
        </button>
        
        <button 
          style={{ padding: '15px', fontSize: '16px' }}
          disabled
        >
          방 목록
        </button>
        
        <button 
          onClick={handleChangeNickname}
          style={{ padding: '10px', fontSize: '14px', marginTop: '20px' }}
        >
          닉네임 변경하기
        </button>
      </div>
    </div>
  );
}