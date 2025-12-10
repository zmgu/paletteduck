import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getPlayerInfo } from '../utils/apiClient';
import apiClient from '../utils/apiClient';
import type { RoomCreateResponse, RoomListResponse } from '../types/game.types';
import duckImage from '../assets/duck_painting_canvas.png';

export default function MainPage() {
  const navigate = useNavigate();
  const [nickname, setNickname] = useState('');
  const [showRoomTypeModal, setShowRoomTypeModal] = useState(false);
  const [showJoinRoomModal, setShowJoinRoomModal] = useState(false);
  const [showInviteCodeModal, setShowInviteCodeModal] = useState(false);
  const [showAlertModal, setShowAlertModal] = useState(false);
  const [alertMessage, setAlertMessage] = useState('');
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
      setShowRoomTypeModal(false);
      setAlertMessage('방 만들기에 실패했습니다.');
      setShowAlertModal(true);
    }
  };

  const handleShowJoinRoomModal = async () => {
    try {
      const { data } = await apiClient.get<RoomListResponse[]>('/room/list');
      setRoomList(data);
      setShowJoinRoomModal(true);
      setInviteCode('');
    } catch (err) {
      console.error('Failed to fetch room list', err);
      setAlertMessage('방 목록을 불러오는데 실패했습니다.');
      setShowAlertModal(true);
    }
  };

  const handleShowInviteCodeModal = () => {
    setInviteCode('');
    setShowInviteCodeModal(true);
  };

  const handleRandomJoin = async () => {
    try {
      const { data } = await apiClient.post<RoomCreateResponse>('/room/random');
      navigate(`/room/${data.roomId}/lobby`);
    } catch (err: any) {
      if (err.response?.status === 404) {
        setAlertMessage('사용 가능한 공개방이 없습니다.');
        setShowAlertModal(true);
      } else {
        console.error('Failed to join random room', err);
        setAlertMessage('랜덤 방 입장에 실패했습니다.');
        setShowAlertModal(true);
      }
    }
  };

  const handleJoinRoom = async (roomId: string) => {
    try {
      await apiClient.post(`/room/${roomId}/join`);
      setShowJoinRoomModal(false);
      navigate(`/room/${roomId}/lobby`);
    } catch (err) {
      console.error('Failed to join room', err);
      setAlertMessage('방 입장에 실패했습니다.');
      setShowAlertModal(true);
    }
  };

  const handleJoinByInviteCode = async () => {
    if (!inviteCode.trim()) {
      setAlertMessage('초대코드를 입력해주세요.');
      setShowAlertModal(true);
      return;
    }

    let code = inviteCode.trim();
    const urlMatch = code.match(/\/room\/([^/?#]+)/);
    if (urlMatch) {
      code = urlMatch[1];
    }

    try {
      const { data } = await apiClient.post<RoomCreateResponse>('/room/join-by-code', {
        inviteCode: code
      });
      setShowInviteCodeModal(false);
      setShowJoinRoomModal(false);
      setInviteCode('');
      navigate(`/room/${data.roomId}/lobby`);
    } catch (err: any) {
      if (err.response?.status === 404) {
        setAlertMessage('초대코드에 해당하는 방을 찾을 수 없습니다.');
        setShowAlertModal(true);
      } else {
        console.error('Failed to join room by invite code', err);
        setAlertMessage('방 입장에 실패했습니다.');
        setShowAlertModal(true);
      }
    }
  };

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      minHeight: '100vh',
      padding: '20px'
    }}>
      <img src={duckImage} alt="Duck" style={{ width: '120px', marginBottom: '15px' }} />
      <h1 style={{ margin: '10px 0' }}>PaletteDuck</h1>
      <p>환영합니다, {nickname}님!</p>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '15px', marginTop: '20px', width: '300px' }}>
        <button
          onClick={handleCreateRoomClick}
          style={{
            padding: '20px',
            fontSize: '18px',
            backgroundColor: '#4CAF50',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            cursor: 'pointer',
            fontWeight: 'bold'
          }}
        >
          🎨 방 만들기
        </button>

        <button
          onClick={handleShowJoinRoomModal}
          style={{
            padding: '20px',
            fontSize: '18px',
            backgroundColor: '#2196F3',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            cursor: 'pointer',
            fontWeight: 'bold'
          }}
        >
          🚪 방 참가하기
        </button>

        <button
          onClick={handleChangeNickname}
          style={{
            padding: '12px',
            fontSize: '14px',
            backgroundColor: '#f5f5f5',
            border: '1px solid #ddd',
            borderRadius: '8px',
            cursor: 'pointer',
            marginTop: '10px'
          }}
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

      {/* 방 참가 모달 */}
      {showJoinRoomModal && (
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
          onClick={() => setShowJoinRoomModal(false)}
        >
          <div
            style={{
              backgroundColor: 'white',
              padding: '30px',
              borderRadius: '8px',
              maxWidth: '800px',
              width: '90%',
              maxHeight: '80vh',
              display: 'flex',
              flexDirection: 'column'
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <h2 style={{ marginTop: 0, marginBottom: '20px' }}>방 참가하기</h2>

            {/* 상단 버튼 영역 */}
            <div style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
              <button
                onClick={handleRandomJoin}
                style={{
                  flex: 1,
                  padding: '12px',
                  fontSize: '15px',
                  backgroundColor: '#FF9800',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                🎲 랜덤 방 입장
              </button>

              <button
                onClick={handleShowInviteCodeModal}
                style={{
                  flex: 1,
                  padding: '12px',
                  fontSize: '15px',
                  backgroundColor: '#673AB7',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                🔑 초대코드 입력
              </button>
            </div>

            {/* 방 목록 영역 */}
            <div style={{
              flex: 1,
              overflowY: 'auto',
              marginBottom: '20px',
              minHeight: '200px'
            }}>
              <h3 style={{ marginTop: 0, marginBottom: '15px', fontSize: '16px', color: '#666' }}>
                공개방 목록
              </h3>

              {roomList.length === 0 ? (
                <p style={{ textAlign: 'center', color: '#999', padding: '40px 0' }}>
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
            </div>

            <button
              onClick={() => setShowJoinRoomModal(false)}
              style={{
                padding: '12px',
                fontSize: '14px',
                backgroundColor: '#f5f5f5',
                border: '1px solid #ddd',
                borderRadius: '4px',
                cursor: 'pointer',
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
            backgroundColor: 'rgba(0, 0, 0, 0.6)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1100
          }}
          onClick={() => setShowInviteCodeModal(false)}
        >
          <div
            style={{
              backgroundColor: 'white',
              padding: '30px',
              borderRadius: '8px',
              maxWidth: '500px',
              width: '90%',
              boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)'
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <h2 style={{ marginTop: 0, marginBottom: '20px' }}>초대코드 입력</h2>
            <p style={{ marginBottom: '20px', color: '#666', fontSize: '14px' }}>
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
              placeholder="예: c5e64178 또는 전체 URL"
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

            <div style={{ display: 'flex', gap: '10px' }}>
              <button
                onClick={handleJoinByInviteCode}
                style={{
                  flex: 1,
                  padding: '12px',
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
                  flex: 1,
                  padding: '12px',
                  fontSize: '16px',
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

      {/* 알림 모달 */}
      {showAlertModal && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.6)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1100
          }}
          onClick={() => setShowAlertModal(false)}
        >
          <div
            style={{
              backgroundColor: 'white',
              padding: '30px',
              borderRadius: '8px',
              maxWidth: '400px',
              width: '90%',
              boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)'
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <div style={{
              fontSize: '18px',
              marginBottom: '20px',
              textAlign: 'center',
              color: '#333'
            }}>
              {alertMessage}
            </div>

            <button
              onClick={() => setShowAlertModal(false)}
              style={{
                width: '100%',
                padding: '12px',
                fontSize: '16px',
                backgroundColor: '#2196F3',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              확인
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
