import { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import PlayerList from './RoomPage/components/PlayerList';
import SpectatorList from './RoomPage/components/SpectatorList';
import GameSettings from './RoomPage/components/GameSettings';
import ChatBox from './RoomPage/components/ChatBox';
import type { RoomInfo, ChatMessage, GameSettings as GameSettingsType } from '../types/game.types';
import logo from '../assets/logo.png';

// Mock 데이터
const mockRoomInfo: RoomInfo = {
  roomId: 'preview',
  inviteCode: 'ABC123',
  status: 'WAITING',
  settings: {
    maxPlayers: 8,
    rounds: 3,
    wordChoices: 3,
    drawTime: 80,
    maxSpectators: 5,
  },
  players: [
    {
      playerId: 'player1',
      nickname: '방장',
      host: true,
      ready: true,
      role: 'PLAYER',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'player2',
      nickname: '참가자1',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'player3',
      nickname: '참가자2',
      host: false,
      ready: false,
      role: 'PLAYER',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'player4',
      nickname: '참가자3',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'spectator1',
      nickname: '관전자1',
      host: false,
      ready: false,
      role: 'SPECTATOR',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'spectator2',
      nickname: '관전자2',
      host: false,
      ready: false,
      role: 'SPECTATOR',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
  ],
};

const mockMessages: ChatMessage[] = [
  {
    playerId: 'system',
    nickname: '시스템',
    message: '대기방에 오신 것을 환영합니다!',
    type: 'SYSTEM',
    timestamp: Date.now() - 60000,
  },
  {
    playerId: 'player1',
    nickname: '방장',
    message: '안녕하세요!',
    type: 'NORMAL',
    timestamp: Date.now() - 50000,
  },
  {
    playerId: 'player2',
    nickname: '참가자1',
    message: '반갑습니다~',
    type: 'NORMAL',
    timestamp: Date.now() - 40000,
  },
  {
    playerId: 'player3',
    nickname: '참가자2',
    message: '게임 시작해요!',
    type: 'NORMAL',
    timestamp: Date.now() - 30000,
  },
];

export default function RoomPreview() {
  const navigate = useNavigate();
  const [roomInfo, setRoomInfo] = useState<RoomInfo>(mockRoomInfo);
  const [messages, setMessages] = useState<ChatMessage[]>(mockMessages);
  const [isCopied, setIsCopied] = useState(false);
  const [currentRole, setCurrentRole] = useState<'PLAYER' | 'SPECTATOR'>('PLAYER');

  const playerInfo = useMemo(() => ({
    playerId: 'player1',
    nickname: '방장',
    token: 'mock-token',
  }), []);

  const currentPlayer = roomInfo.players.find(p => p.playerId === playerInfo.playerId);
  const isHost = currentPlayer?.host || false;
  const players = roomInfo.players.filter(p => p.role === 'PLAYER');
  const spectators = roomInfo.players.filter(p => p.role === 'SPECTATOR');

  const allPlayersReady = players.filter(p => !p.host && !p.ready).length === 0;
  const canStartGame = isHost && allPlayersReady && players.length >= 2;

  const handleCopyInviteCode = () => {
    setIsCopied(true);
    console.log('초대코드 복사:', roomInfo.inviteCode);
    setTimeout(() => {
      setIsCopied(false);
    }, 3000);
  };

  const handleToggleReady = () => {
    console.log('준비 상태 토글');
  };

  const handleChangeRole = (role: 'PLAYER' | 'SPECTATOR') => {
    console.log('역할 변경:', role);
    setCurrentRole(role);
  };

  const handleUpdateSettings = (newSettings: Partial<GameSettingsType>) => {
    console.log('설정 변경:', newSettings);
    setRoomInfo(prev => ({
      ...prev,
      settings: { ...prev.settings, ...newSettings }
    }));
  };

  const handleStartGame = () => {
    console.log('게임 시작');
  };

  const handleSendMessage = (message: string) => {
    const newMessage: ChatMessage = {
      playerId: playerInfo.playerId,
      nickname: playerInfo.nickname,
      message,
      type: 'NORMAL',
      timestamp: Date.now(),
    };
    setMessages([...messages, newMessage]);
  };

  const handleLeaveRoom = () => {
    console.log('방 나가기');
    navigate('/');
  };

  return (
    <div style={{ margin: '0 auto' }}>
      {/* 방 나가기 버튼 - 왼쪽 상단 고정 */}
      <button
        onClick={handleLeaveRoom}
        style={{
          position: 'fixed',
          top: '20px',
          left: '20px',
          padding: '12px 24px',
          fontSize: '14px',
          backgroundColor: '#3d2626',
          color: 'white',
          border: 'none',
          borderRadius: '4px',
          cursor: 'pointer',
          fontWeight: 'bold',
          zIndex: 1000
        }}
      >
        방 나가기
      </button>

      <div style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'flex-start',
        width: '1310px',
        margin: '10px auto 0'
      }}>
        {/* 헤더 영역 */}
        <div style={{
          width: '100%',
          height: '70px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          backgroundColor: 'transparent',
          borderRadius: '8px 8px 0 0',
          position: 'relative',
          flexShrink: 0
        }}>
          {/* 로고 */}
          <div style={{
            position: 'absolute',
            left: '20px',
            top: '50%',
            transform: 'translateY(-50%)',
          }}>
            <img
              src={logo}
              alt="PaletteDuck Logo"
              style={{
                height: '50px',
                width: 'auto'
              }}
            />
          </div>
        </div>

        {/* 서브헤더 영역 */}
        <div style={{
          width: '100%',
          height: '50px',
          display: 'grid',
          gridTemplateColumns: '200px 810px 300px',
          alignItems: 'center',
          backgroundColor: '#8CA9FF',
          borderBottom: 'none',
          boxSizing: 'border-box',
          flexShrink: 0
        }}>
          <div style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '18px',
            fontWeight: 'bold',
            color: '#fff',
            padding: '0 10px'
          }}>
            대기방
          </div>
          <div style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '16px',
            fontWeight: 'bold',
            color: '#fff'
          }}>
            초대코드: {roomInfo.inviteCode}
          </div>
          <div></div>
        </div>

        {/* 메인 레이아웃 */}
        <div style={{
          width: '100%',
          height: '606px',
          display: 'grid',
          gridTemplateColumns: '200px 810px 300px',
          gridTemplateRows: '200px 1fr',
          gap: '0',
          backgroundColor: 'transparent',
          borderRadius: '0 0 8px 8px',
          flexShrink: 0,
          overflow: 'hidden'
        }}>
          {/* 왼쪽: 참가자 목록 + 버튼 영역 (전체 높이) */}
          <div style={{
            gridColumn: '1',
            gridRow: '1 / 3',
            display: 'flex',
            flexDirection: 'column',
            backgroundColor: 'transparent',
            borderRight: 'none',
            marginTop: '3px'
          }}>
            {/* 참가자 목록 */}
            <div style={{ flex: 1, overflow: 'hidden', position: 'relative' }}>
              <PlayerList
                players={players}
                currentPlayerId={playerInfo.playerId}
                maxPlayers={roomInfo.settings.maxPlayers}
              />
            </div>

            {/* 참가자/관전자 변경 버튼들 */}
            <div style={{ display: 'flex', gap: '0' }}>
              <button
                onClick={() => handleChangeRole('PLAYER')}
                style={{
                  flex: 1,
                  padding: '12px',
                  fontSize: '13px',
                  backgroundColor: '#8a8a8a',
                  color: 'white',
                  border: 'none',
                  borderTop: 'none',
                  borderRight: '1px solid #ddd',
                  cursor: currentPlayer?.role === 'SPECTATOR' && players.length < roomInfo.settings.maxPlayers ? 'pointer' : 'not-allowed',
                  fontWeight: 'bold',
                  opacity: currentPlayer?.role === 'SPECTATOR' && players.length < roomInfo.settings.maxPlayers ? 1 : 0.5
                }}
                disabled={!(currentPlayer?.role === 'SPECTATOR' && players.length < roomInfo.settings.maxPlayers)}
              >
                참가자로 변경 ({players.length}/{roomInfo.settings.maxPlayers})
              </button>
              <button
                onClick={() => handleChangeRole('SPECTATOR')}
                style={{
                  flex: 1,
                  padding: '12px',
                  fontSize: '13px',
                  backgroundColor: '#8a8a8a',
                  color: 'white',
                  border: 'none',
                  borderTop: 'none',
                  cursor: currentPlayer?.role === 'PLAYER' && spectators.length < roomInfo.settings.maxSpectators ? 'pointer' : 'not-allowed',
                  fontWeight: 'bold',
                  opacity: currentPlayer?.role === 'PLAYER' && spectators.length < roomInfo.settings.maxSpectators ? 1 : 0.5
                }}
                disabled={!(currentPlayer?.role === 'PLAYER' && spectators.length < roomInfo.settings.maxSpectators)}
              >
                관전자로 변경 ({spectators.length}/{roomInfo.settings.maxSpectators})
              </button>
            </div>
          </div>

          {/* 중앙 상단: 이미지 영역 */}
          <div style={{
            gridColumn: '2',
            gridRow: '1',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            backgroundColor: 'transparent',
            borderRight: 'none',
            borderBottom: 'none',
            marginTop: '3px'
          }}>
            <span style={{ fontSize: '18px', color: '#666', fontWeight: 'bold' }}>이미지</span>
          </div>

          {/* 중앙 하단: 관전자 목록 + 버튼 영역 */}
          <div style={{
            gridColumn: '2',
            gridRow: '2',
            display: 'flex',
            flexDirection: 'column',
            backgroundColor: 'transparent',
            borderRight: 'none'
          }}>
            <div style={{ flex: 1, overflow: 'hidden', position: 'relative' }}>
              <SpectatorList
                spectators={spectators}
                currentPlayerId={playerInfo.playerId}
                maxSpectators={roomInfo.settings.maxSpectators}
              />
            </div>

            {/* 초대코드 복사 / 준비 완료/시작하기 버튼들 */}
            <div style={{ display: 'flex', gap: '0' }}>
              <button
                onClick={handleCopyInviteCode}
                style={{
                  flex: 1,
                  padding: '12px',
                  fontSize: '13px',
                  backgroundColor: isCopied ? '#4caf50' : '#7a5a9a',
                  color: 'white',
                  border: 'none',
                  borderTop: 'none',
                  borderRight: '1px solid #ddd',
                  cursor: 'pointer',
                  fontWeight: 'bold',
                  transition: 'background-color 0.3s'
                }}
              >
                {isCopied ? '초대코드 복사 완료' : '초대코드 복사'}
              </button>

              {isHost ? (
                <button
                  onClick={handleStartGame}
                  style={{
                    flex: 1,
                    padding: '12px',
                    fontSize: '13px',
                    backgroundColor: canStartGame ? '#d97aa0' : '#ccc',
                    color: 'white',
                    border: 'none',
                    borderTop: 'none',
                    cursor: canStartGame ? 'pointer' : 'not-allowed',
                    fontWeight: 'bold'
                  }}
                  disabled={!canStartGame}
                >
                  시작하기
                </button>
              ) : currentPlayer?.role === 'PLAYER' ? (
                <button
                  onClick={handleToggleReady}
                  style={{
                    flex: 1,
                    padding: '12px',
                    fontSize: '13px',
                    backgroundColor: currentPlayer.ready ? '#ff9800' : '#d97aa0',
                    color: 'white',
                    border: 'none',
                    borderTop: 'none',
                    cursor: 'pointer',
                    fontWeight: 'bold'
                  }}
                >
                  {currentPlayer.ready ? '준비 취소' : '준비 완료'}
                </button>
              ) : (
                <div style={{
                  flex: 1,
                  padding: '12px',
                  fontSize: '13px',
                  backgroundColor: '#e0e0e0',
                  color: '#888',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  borderTop: 'none',
                  fontWeight: 'bold'
                }}>
                  -
                </div>
              )}
            </div>
          </div>

          {/* 우측: 게임설정 + 채팅창 (전체 높이) */}
          <div style={{
            gridColumn: '3',
            gridRow: '1 / 3',
            display: 'flex',
            flexDirection: 'column',
            gap: '0',
            boxSizing: 'border-box',
            overflow: 'hidden',
            padding: '0'
          }}>
            {/* 게임설정 */}
            <div style={{
              flex: '1',
              display: 'flex',
              flexDirection: 'column',
              backgroundColor: 'transparent',
              borderBottom: 'none',
              padding: '8px 0 2px 0',
              minHeight: 0,
              overflowY: 'auto',
              marginTop: '3px'
            }}>
              <GameSettings
                settings={roomInfo.settings}
                isHost={isHost}
                currentPlayerCount={players.length}
                onSettingsChange={(newSettings) => handleUpdateSettings(newSettings)}
              />
            </div>

            {/* 채팅창 */}
            <div style={{
              flex: '2',
              display: 'flex',
              flexDirection: 'column',
              backgroundColor: 'transparent',
              padding: '8px 0 6px 0',
              minHeight: 0
            }}>
              <ChatBox
                messages={messages}
                onSendMessage={handleSendMessage}
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
