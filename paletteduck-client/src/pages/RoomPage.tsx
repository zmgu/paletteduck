import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getPlayerInfo } from '../utils/apiClient';
import apiClient from '../utils/apiClient';
import { wsClient } from '../utils/wsClient';
import type { RoomInfo, PlayerRole, ChatMessage, GameSettings } from '../types/game.types';

export default function RoomPage() {
  const { roomId } = useParams<{ roomId: string }>();
  const navigate = useNavigate();
  const [roomInfo, setRoomInfo] = useState<RoomInfo | null>(null);
  const [currentPlayerId, setCurrentPlayerId] = useState('');
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
  const [chatInput, setChatInput] = useState('');
  const [hasJoined, setHasJoined] = useState(false);

useEffect(() => {
    const playerInfo = getPlayerInfo();
    
    if (!playerInfo) {
      navigate('/');
      return;
    }
    
    console.log('RoomPage mounted - playerId:', playerInfo.playerId);
    setCurrentPlayerId(playerInfo.playerId);
    joinAndLoadRoom();

  // beforeunload로 탭/브라우저 닫힐 때 방 나가기
    const handleBeforeUnload = () => {
      console.log('beforeunload triggered - hasJoined:', hasJoined, 'playerId:', playerInfo.playerId);
      
      // sendBeacon 사용
      const leaveUrl = `http://localhost:8083/api/room/${roomId}/leave-beacon`;
      const blob = new Blob(
        [JSON.stringify({ playerId: playerInfo.playerId })],
        { type: 'application/json' }
      );
      
      console.log('Sending beacon to:', leaveUrl);
      const result = navigator.sendBeacon(leaveUrl, blob);
      console.log('sendBeacon result:', result);
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    window.addEventListener('unload', handleBeforeUnload);

    return () => {
    console.log('RoomPage unmounting - calling handleLeaveRoom');
    handleLeaveRoom();
    wsClient.disconnect();
    window.removeEventListener('beforeunload', handleBeforeUnload);
    window.removeEventListener('unload', handleBeforeUnload);
    };
  }, [roomId, navigate]);

  const joinAndLoadRoom = async () => {
    try {
      console.log('Joining room:', roomId);
      await apiClient.post(`/room/${roomId}/join`);
      setHasJoined(true);
      console.log('Room joined successfully');
      
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
      if (playerInfo) {
        console.log('WebSocket connected - registering session');
        
        // 세션 등록
        wsClient.send(`/app/room/${roomId}/register`, playerInfo.playerId);
        
        // 입장 메시지 전송
        wsClient.send(`/app/room/${roomId}/chat`, {
          playerId: '',
          nickname: '',
          message: `${playerInfo.nickname}님이 입장하셨습니다.`,
          type: 'SYSTEM'
        });
        
        // 방 정보 갱신 요청
        console.log('Requesting room update after join');
        wsClient.send(`/app/room/${roomId}/update`);
      }
      
      wsClient.subscribe(`/topic/room/${roomId}`, (data: RoomInfo) => {
        console.log('Room info updated:', data);
        setRoomInfo(data);
      });

      wsClient.subscribe(`/topic/room/${roomId}/chat`, (data: ChatMessage) => {
        setChatMessages(prev => [...prev, data]);
      });

      wsClient.subscribe(`/topic/room/${roomId}/start`, (data: RoomInfo) => {
        console.log('Game starting...', data);
        if (data.status === 'PLAYING') {
          navigate(`/room/${roomId}/game`);
        }
      });
    });
  };

  const handleLeaveRoom = async () => {
    console.log('handleLeaveRoom called - hasJoined:', hasJoined);
    if (!hasJoined) return;
    
    try {
      const playerInfo = getPlayerInfo();
      console.log('Leaving room - playerId:', playerInfo?.playerId);
      
      await apiClient.post(`/room/${roomId}/leave`);
      console.log('Leave room API call successful');
      
      if (playerInfo) {
        wsClient.send(`/app/room/${roomId}/chat`, {
          playerId: '',
          nickname: '',
          message: `${playerInfo.nickname}님이 방을 나갔습니다.`,
          type: 'SYSTEM'
        });
        
        // 방 정보 갱신 요청
        console.log('Requesting room update after leave');
        wsClient.send(`/app/room/${roomId}/update`);
      }
    } catch (err) {
      console.error('Failed to leave room', err);
    }
  };

  const handleToggleReady = () => {
    wsClient.send(`/app/room/${roomId}/ready`, currentPlayerId);
  };

  const handleChangeRole = (newRole: PlayerRole) => {
    wsClient.send(`/app/room/${roomId}/role`, {
      playerId: currentPlayerId,
      newRole
    });
  };

  const handleSettingsChange = (newSettings: Partial<GameSettings>) => {
    if (!roomInfo) return;
    
    const updatedSettings = { ...roomInfo.settings, ...newSettings };
    
    wsClient.send(`/app/room/${roomId}/settings`, {
      playerId: currentPlayerId,
      settings: updatedSettings
    });
  };

  const handleStartGame = () => {
    console.log('Starting game...', currentPlayerId);
    wsClient.send(`/app/room/${roomId}/start`, currentPlayerId);
  };

  const handleSendChat = (e: React.FormEvent) => {
    e.preventDefault();
    if (!chatInput.trim() || !roomInfo) return;

    const playerInfo = getPlayerInfo();
    if (!playerInfo) return;

    wsClient.send(`/app/room/${roomId}/chat`, {
      playerId: currentPlayerId,
      nickname: playerInfo.nickname,
      message: chatInput.trim(),
      type: 'NORMAL'
    });

    setChatInput('');
  };

  const handleCopyInviteCode = () => {
    const inviteUrl = `${window.location.origin}/room/${roomInfo?.inviteCode}`;
    navigator.clipboard.writeText(inviteUrl);
    alert('초대 코드가 복사되었습니다!');
  };

  if (!roomInfo) {
    return <div>로딩 중...</div>;
  }

  const currentPlayer = roomInfo.players.find(p => p.playerId === currentPlayerId);
  const isHost = currentPlayer?.host || false;
  const players = roomInfo.players.filter(p => p.role === 'PLAYER');
  const spectators = roomInfo.players.filter(p => p.role === 'SPECTATOR');

  return (
    <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto' }}>
      <h1>대기방 - {roomInfo.roomId}</h1>
      
      <div style={{ display: 'flex', gap: '20px', marginTop: '20px' }}>
        {/* 왼쪽: 참가자/관전자 목록 */}
        <div style={{ flex: 1 }}>
          {/* 참가자 목록 */}
          <div style={{ border: '1px solid #ccc', padding: '15px', borderRadius: '8px', marginBottom: '15px' }}>
            <h2>참가자 ({players.length}/{roomInfo.settings.maxPlayers})</h2>
            <ul style={{ listStyle: 'none', padding: 0 }}>
              {players.map((player) => (
                <li 
                  key={player.playerId}
                  style={{ 
                    padding: '10px',
                    marginBottom: '5px',
                    backgroundColor: player.playerId === currentPlayerId ? '#e3f2fd' : '#f5f5f5',
                    borderRadius: '4px',
                    fontWeight: player.playerId === currentPlayerId ? 'bold' : 'normal',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px'
                  }}
                >
                  {player.host && <span>👑</span>}
                  <span>{player.nickname}</span>
                  {player.ready && <span>✅</span>}
                </li>
              ))}
            </ul>
            {currentPlayer?.role === 'SPECTATOR' && players.length < roomInfo.settings.maxPlayers && (
              <button onClick={() => handleChangeRole('PLAYER')} style={{ marginTop: '10px', width: '100%', padding: '10px' }}>
                참가자로 변경
              </button>
            )}
          </div>

          {/* 관전자 목록 */}
            <div style={{ border: '1px solid #ccc', padding: '15px', borderRadius: '8px' }}>
              <h2>관전자 ({spectators.length}/{roomInfo.settings.maxSpectators})</h2>
              <ul style={{ listStyle: 'none', padding: 0 }}>
                {spectators.map((player) => (
                  <li 
                    key={player.playerId}
                    style={{ 
                      padding: '10px',
                      marginBottom: '5px',
                      backgroundColor: player.playerId === currentPlayerId ? '#e3f2fd' : '#f5f5f5',
                      borderRadius: '4px',
                      fontWeight: player.playerId === currentPlayerId ? 'bold' : 'normal',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '8px'
                    }}
                  >
                    <span>👁️</span>
                    <span>{player.nickname}</span>
                  </li>
                ))}
              </ul>
              {currentPlayer?.role === 'PLAYER' && spectators.length < roomInfo.settings.maxSpectators && (
                <button onClick={() => handleChangeRole('SPECTATOR')} style={{ marginTop: '10px', width: '100%', padding: '10px' }}>
                  관전자로 변경
                </button>
              )}
            </div>
      </div>

        {/* 오른쪽: 게임 설정 */}
        <div style={{ flex: 1, border: '1px solid #ccc', padding: '15px', borderRadius: '8px' }}>
          <h2>게임 설정 {isHost && <span style={{ fontSize: '14px', color: '#666' }}>(방장)</span>}</h2>
          <div style={{ marginTop: '15px', display: 'flex', flexDirection: 'column', gap: '15px' }}>
            <div>
              <label style={{ display: 'block', marginBottom: '5px' }}>참가자 수: {roomInfo.settings.maxPlayers}명</label>
              {isHost ? (
                <input
                  type="range"
                  min="2"
                  max="20"
                  value={roomInfo.settings.maxPlayers}
                  onChange={(e) => handleSettingsChange({ maxPlayers: parseInt(e.target.value) })}
                  style={{ width: '100%' }}
                />
              ) : null}
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '5px' }}>라운드: {roomInfo.settings.rounds}라운드</label>
              {isHost ? (
                <input
                  type="range"
                  min={roomInfo.settings.mode === 'CUSTOM' ? 2 : 2}
                  max={roomInfo.settings.mode === 'CUSTOM' ? 3 : 10}
                  value={roomInfo.settings.rounds}
                  onChange={(e) => handleSettingsChange({ rounds: parseInt(e.target.value) })}
                  style={{ width: '100%' }}
                />
              ) : null}
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '5px' }}>단어 선택지: {roomInfo.settings.wordChoices}개</label>
              {isHost && roomInfo.settings.mode === 'NORMAL' ? (
                <input
                  type="range"
                  min="2"
                  max="4"
                  value={roomInfo.settings.wordChoices}
                  onChange={(e) => handleSettingsChange({ wordChoices: parseInt(e.target.value) })}
                  style={{ width: '100%' }}
                />
              ) : roomInfo.settings.mode === 'CUSTOM' ? (
                <span> (고정)</span>
              ) : null}
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '5px' }}>그리기 시간: {roomInfo.settings.drawTime}초</label>
              {isHost ? (
                <select
                  value={roomInfo.settings.drawTime}
                  onChange={(e) => handleSettingsChange({ drawTime: parseInt(e.target.value) })}
                  style={{ width: '100%', padding: '8px' }}
                >
                  <option value="30">30초</option>
                  <option value="60">60초</option>
                  <option value="120">120초</option>
                  <option value="240">240초</option>
                </select>
              ) : null}
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '5px' }}>모드: {roomInfo.settings.mode === 'NORMAL' ? '일반' : '커스텀'}</label>
              {isHost ? (
                <select
                  value={roomInfo.settings.mode}
                  onChange={(e) => {
                    const mode = e.target.value as 'NORMAL' | 'CUSTOM';
                    handleSettingsChange({ 
                      mode,
                      rounds: mode === 'CUSTOM' ? 2 : roomInfo.settings.rounds,
                      wordChoices: mode === 'CUSTOM' ? 2 : roomInfo.settings.wordChoices
                    });
                  }}
                  style={{ width: '100%', padding: '8px' }}
                >
                  <option value="NORMAL">일반 모드</option>
                  <option value="CUSTOM">커스텀 모드</option>
                </select>
              ) : null}
            </div>
          </div>
        </div>
      </div>

      {/* 채팅 */}
      <div style={{ marginTop: '20px', border: '1px solid #ccc', padding: '15px', borderRadius: '8px' }}>
        <h3>채팅</h3>
        <div style={{ height: '200px', overflowY: 'auto', border: '1px solid #eee', padding: '10px', marginBottom: '10px', backgroundColor: '#fafafa' }}>
          {chatMessages.map((msg, idx) => (
            <div key={idx} style={{ 
              marginBottom: '5px',
              color: msg.type === 'SYSTEM' ? 'green' : msg.type === 'CORRECT' ? 'gray' : 'black'
            }}>
              {msg.type === 'SYSTEM' ? (
                msg.message
              ) : (
                <><strong>{msg.nickname}:</strong> {msg.message}</>
              )}
            </div>
          ))}
        </div>
        <form onSubmit={handleSendChat} style={{ display: 'flex', gap: '10px' }}>
          <input
            value={chatInput}
            onChange={(e) => setChatInput(e.target.value)}
            placeholder="메시지 입력 (최대 100자)"
            maxLength={100}
            style={{ flex: 1, padding: '10px' }}
          />
          <button type="submit" style={{ padding: '10px 20px' }}>전송</button>
        </form>
      </div>

      {/* 하단 버튼 */}
      <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
        <button onClick={handleCopyInviteCode} style={{ flex: 1, padding: '15px', fontSize: '16px' }}>
          초대 코드 복사
        </button>
        
        {isHost ? (
          <button 
            onClick={handleStartGame}
            style={{ 
              flex: 1, 
              padding: '15px', 
              fontSize: '16px', 
              backgroundColor: players.filter(p => !p.host && !p.ready).length > 0 || players.length < 2 ? '#ccc' : '#4caf50', 
              color: 'white',
              cursor: players.filter(p => !p.host && !p.ready).length > 0 || players.length < 2 ? 'not-allowed' : 'pointer'
            }}
            disabled={players.filter(p => !p.host && !p.ready).length > 0 || players.length < 2}
          >
            시작하기
          </button>
        ) : currentPlayer?.role === 'PLAYER' ? (
          <button 
            onClick={handleToggleReady}
            style={{ 
              flex: 1, 
              padding: '15px', 
              fontSize: '16px',
              backgroundColor: currentPlayer.ready ? '#ff9800' : '#2196f3',
              color: 'white'
            }}
          >
            {currentPlayer.ready ? '준비 취소' : '준비 완료'}
          </button>
        ) : null}
      </div>
    </div>
  );
}