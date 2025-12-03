import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getPlayerInfo } from '../utils/apiClient';
import apiClient from '../utils/apiClient';
import type { RoomCreateResponse } from '../types/game.types';

export default function MainPage() {
  const navigate = useNavigate();
  const [nickname, setNickname] = useState('');
  const [showRoomTypeModal, setShowRoomTypeModal] = useState(false);

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

  const handleCreateRoomClick = () => {
    setShowRoomTypeModal(true);
  };

  const handleCreateRoom = async (isPublic: boolean) => {
    try {
      const { data } = await apiClient.post<RoomCreateResponse>('/room/create', {
        isPublic: isPublic
      });
      setShowRoomTypeModal(false);
      navigate(`/room/${data.roomId}/lobby`);
    } catch (err) {
      console.error('Failed to create room', err);
      alert('방 만들기에 실패했습니다.');
    }
  };

  const handleRandomJoin = async () => {
    try {
      const { data } = await apiClient.post<RoomCreateResponse>('/room/random');
      navigate(`/room/${data.roomId}/lobby`);
    } catch (err: any) {
      if (err.response?.status === 404) {
        alert('사용 가능한 공개방이 없습니다.');
      } else {
        console.error('Failed to join random room', err);
        alert('랜덤 방 입장에 실패했습니다.');
      }
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h1>PaletteDuck - 메인</h1>
      <p>환영합니다, {nickname}님!</p>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginTop: '20px', maxWidth: '300px' }}>
        <button
          onClick={handleCreateRoomClick}
          style={{ padding: '15px', fontSize: '16px' }}
        >
          방 만들기
        </button>

        <button
          onClick={handleRandomJoin}
          style={{ padding: '15px', fontSize: '16px' }}
        >
          랜덤 방 입장
        </button>

        <button
          style={{ padding: '15px', fontSize: '16px' }}
          disabled
        >
          초대코드 입력
        </button>

        <button
          onClick={handleChangeNickname}
          style={{ padding: '10px', fontSize: '14px', marginTop: '20px' }}
        >
          닉네임 변경하기
        </button>
      </div>

      {/* 방 타입 선택 모달 */}
      {showRoomTypeModal && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000
          }}
          onClick={() => setShowRoomTypeModal(false)}
        >
          <div
            style={{
              backgroundColor: 'white',
              padding: '30px',
              borderRadius: '8px',
              maxWidth: '400px',
              width: '90%'
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <h2 style={{ marginTop: 0, marginBottom: '20px' }}>방 만들기</h2>
            <p style={{ marginBottom: '20px', color: '#666' }}>
              어떤 타입의 방을 만드시겠습니까?
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              <button
                onClick={() => handleCreateRoom(true)}
                style={{
                  padding: '15px',
                  fontSize: '16px',
                  backgroundColor: '#4CAF50',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                🌍 공개방 만들기
              </button>

              <button
                onClick={() => handleCreateRoom(false)}
                style={{
                  padding: '15px',
                  fontSize: '16px',
                  backgroundColor: '#FF9800',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                🔒 비밀방 만들기
              </button>

              <button
                onClick={() => setShowRoomTypeModal(false)}
                style={{
                  padding: '10px',
                  fontSize: '14px',
                  backgroundColor: '#f5f5f5',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  marginTop: '10px'
                }}
              >
                취소
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}