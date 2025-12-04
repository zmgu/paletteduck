import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getPlayerInfo } from '../utils/apiClient';
import apiClient from '../utils/apiClient';
import type { RoomCreateResponse, RoomListResponse } from '../types/game.types';

export default function MainPage() {
  const navigate = useNavigate();
  const [nickname, setNickname] = useState('');
  const [showRoomTypeModal, setShowRoomTypeModal] = useState(false);
  const [showRoomListModal, setShowRoomListModal] = useState(false);
  const [showInviteCodeModal, setShowInviteCodeModal] = useState(false);
  const [roomList, setRoomList] = useState<RoomListResponse[]>([]);
  const [inviteCode, setInviteCode] = useState('');

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

  const handleShowRoomList = async () => {
    try {
      const { data } = await apiClient.get<RoomListResponse[]>('/room/list');
      setRoomList(data);
      setShowRoomListModal(true);
    } catch (err) {
      console.error('Failed to fetch room list', err);
      alert('방 목록을 불러오는데 실패했습니다.');
    }
  };

  const handleJoinRoom = async (roomId: string) => {
    try {
      await apiClient.post(`/room/${roomId}/join`);
      setShowRoomListModal(false);
      navigate(`/room/${roomId}/lobby`);
    } catch (err) {
      console.error('Failed to join room', err);
      alert('방 입장에 실패했습니다.');
    }
  };

  const handleShowInviteCodeModal = () => {
    setInviteCode('');
    setShowInviteCodeModal(true);
  };

  const handleJoinByInviteCode = async () => {
    if (!inviteCode.trim()) {
      alert('초대코드를 입력해주세요.');
      return;
    }

    // URL에서 초대코드 추출 (전체 URL을 복사한 경우 대응)
    let code = inviteCode.trim();

    // URL 형식인 경우 roomId 추출: http://localhost:5173/room/c5e64178
    const urlMatch = code.match(/\/room\/([^/?#]+)/);
    if (urlMatch) {
      code = urlMatch[1];
    }

    try {
      const { data } = await apiClient.post<RoomCreateResponse>('/room/join-by-code', {
        inviteCode: code
      });
      setShowInviteCodeModal(false);
      setInviteCode('');
      navigate(`/room/${data.roomId}/lobby`);
    } catch (err: any) {
      if (err.response?.status === 404) {
        alert('초대코드에 해당하는 방을 찾을 수 없습니다.');
      } else {
        console.error('Failed to join room by invite code', err);
        alert('방 입장에 실패했습니다.');
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
          onClick={handleShowRoomList}
          style={{ padding: '15px', fontSize: '16px' }}
        >
          방 목록
        </button>

        <button
          onClick={handleShowInviteCodeModal}
          style={{ padding: '15px', fontSize: '16px' }}
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

      {/* 방 목록 모달 */}
      {showRoomListModal && (
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
          onClick={() => setShowRoomListModal(false)}
        >
          <div
            style={{
              backgroundColor: 'white',
              padding: '30px',
              borderRadius: '8px',
              maxWidth: '800px',
              width: '90%',
              maxHeight: '80vh',
              overflow: 'auto'
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <h2 style={{ marginTop: 0, marginBottom: '20px' }}>공개방 목록</h2>

            {roomList.length === 0 ? (
              <p style={{ textAlign: 'center', color: '#666', padding: '40px 0' }}>
                공개방이 없습니다.
              </p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                {roomList.map((room) => (
                  <div
                    key={room.roomId}
                    onClick={() => handleJoinRoom(room.roomId)}
                    style={{
                      padding: '15px',
                      border: '1px solid #ddd',
                      borderRadius: '4px',
                      cursor: 'pointer',
                      backgroundColor: '#f9f9f9',
                      transition: 'background-color 0.2s'
                    }}
                    onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f0f0f0'}
                    onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#f9f9f9'}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <div style={{ fontSize: '16px', fontWeight: 'bold', marginBottom: '5px' }}>
                          방장: {room.hostNickname}
                        </div>
                        <div style={{ fontSize: '14px', color: '#666' }}>
                          상태: {room.status === 'WAITING' ? '대기중' : '게임중'} |
                          인원: {room.currentPlayers}/{room.maxPlayers}
                          {room.status === 'PLAYING' && room.currentRound !== null && room.totalRounds !== null && (
                            <> | 라운드: {room.currentRound}/{room.totalRounds}</>
                          )}
                        </div>
                      </div>
                      <div style={{
                        padding: '8px 16px',
                        backgroundColor: room.status === 'WAITING' ? '#4CAF50' : '#2196F3',
                        color: 'white',
                        borderRadius: '4px',
                        fontSize: '14px'
                      }}>
                        {room.status === 'WAITING' ? '입장' : '관전'}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}

            <button
              onClick={() => setShowRoomListModal(false)}
              style={{
                padding: '10px',
                fontSize: '14px',
                backgroundColor: '#f5f5f5',
                border: '1px solid #ddd',
                borderRadius: '4px',
                cursor: 'pointer',
                marginTop: '20px',
                width: '100%'
              }}
            >
              닫기
            </button>
          </div>
        </div>
      )}

      {/* 초대코드 입력 모달 */}
      {showInviteCodeModal && (
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
          onClick={() => setShowInviteCodeModal(false)}
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
            <h2 style={{ marginTop: 0, marginBottom: '20px' }}>초대코드 입력</h2>
            <p style={{ marginBottom: '20px', color: '#666' }}>
              방의 초대코드 또는 URL을 입력하세요
            </p>

            <input
              type="text"
              value={inviteCode}
              onChange={(e) => setInviteCode(e.target.value)}
              onKeyPress={(e) => {
                if (e.key === 'Enter') {
                  handleJoinByInviteCode();
                }
              }}
              placeholder="예: c5e64178 또는 http://localhost:5173/room/c5e64178"
              autoFocus
              style={{
                width: '100%',
                padding: '12px',
                fontSize: '16px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                marginBottom: '20px',
                boxSizing: 'border-box'
              }}
            />

            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              <button
                onClick={handleJoinByInviteCode}
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
                입장하기
              </button>

              <button
                onClick={() => setShowInviteCodeModal(false)}
                style={{
                  padding: '10px',
                  fontSize: '14px',
                  backgroundColor: '#f5f5f5',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                  cursor: 'pointer'
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