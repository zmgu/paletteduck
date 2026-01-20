import { useState, useMemo, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import PlayerList from './RoomPage/components/PlayerList';
import SpectatorList from './RoomPage/components/SpectatorList';
import GameSettings from './RoomPage/components/GameSettings';
import ChatBox from './GameRoomPage/components/ChatBox';
import type { RoomInfo, ChatMessage, GameSettings as GameSettingsType } from '../types/game.types';
import logo from '../assets/logo.png';

// Mock 데이터
const mockRoomInfo: RoomInfo = {
  roomId: 'preview',
  inviteCode: 'ABC123',
  status: 'WAITING',
  settings: {
    maxPlayers: 18,
    rounds: 3,
    wordChoices: 3,
    drawTime: 80,
    maxSpectators: 10,
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
      playerId: 'player5',
      nickname: '참가자4',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'player6',
      nickname: '참가자5',
      host: false,
      ready: false,
      role: 'PLAYER',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'player7',
      nickname: '참가자6',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'player8',
      nickname: '참가자7',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'player9',
      nickname: '참가자8',
      host: false,
      ready: false,
      role: 'PLAYER',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'player10',
      nickname: '참가자9',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'player11',
      nickname: '참가자10',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'player12',
      nickname: '참가자11',
      host: false,
      ready: false,
      role: 'PLAYER',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'player13',
      nickname: '참가자12',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'player14',
      nickname: '참가자13',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'player15',
      nickname: '참가자14',
      host: false,
      ready: false,
      role: 'PLAYER',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'player16',
      nickname: '참가자15',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'player17',
      nickname: '참가자16',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'player18',
      nickname: '참가자17',
      host: false,
      ready: false,
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
    {
      playerId: 'spectator3',
      nickname: '관전자3',
      host: false,
      ready: false,
      role: 'SPECTATOR',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'spectator4',
      nickname: '관전자4',
      host: false,
      ready: false,
      role: 'SPECTATOR',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'spectator5',
      nickname: '관전자5',
      host: false,
      ready: false,
      role: 'SPECTATOR',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'spectator6',
      nickname: '관전자6',
      host: false,
      ready: false,
      role: 'SPECTATOR',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'spectator7',
      nickname: '관전자7',
      host: false,
      ready: false,
      role: 'SPECTATOR',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'spectator8',
      nickname: '관전자8',
      host: false,
      ready: false,
      role: 'SPECTATOR',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'spectator9',
      nickname: '관전자9',
      host: false,
      ready: false,
      role: 'SPECTATOR',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'spectator10',
      nickname: '관전자10',
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
  const [isSpectatorHovered, setIsSpectatorHovered] = useState(false);
  const [isSpectatorAtTop, setIsSpectatorAtTop] = useState(true);
  const [isSpectatorAtBottom, setIsSpectatorAtBottom] = useState(false);
  const [isPlayerHovered, setIsPlayerHovered] = useState(false);
  const [isPlayerAtTop, setIsPlayerAtTop] = useState(true);
  const [isPlayerAtBottom, setIsPlayerAtBottom] = useState(false);
  const spectatorListRef = useRef<HTMLDivElement>(null);
  const playerListRef = useRef<HTMLUListElement>(null);

  const playerInfo = useMemo(() => ({
    playerId: 'player1',
    nickname: '방장',
    token: 'mock-token',
  }), []);

  // 스크롤 위치 감지
  useEffect(() => {
    const checkSpectatorScroll = () => {
      if (spectatorListRef.current) {
        const { scrollTop, scrollHeight, clientHeight } = spectatorListRef.current;
        setIsSpectatorAtTop(scrollTop <= 1);
        setIsSpectatorAtBottom(scrollTop + clientHeight >= scrollHeight - 1);
      }
    };

    const checkPlayerScroll = () => {
      if (playerListRef.current) {
        const { scrollTop, scrollHeight, clientHeight } = playerListRef.current;
        setIsPlayerAtTop(scrollTop <= 1);
        setIsPlayerAtBottom(scrollTop + clientHeight >= scrollHeight - 1);
      }
    };

    checkSpectatorScroll();
    checkPlayerScroll();

    const spectatorList = spectatorListRef.current;
    const playerList = playerListRef.current;

    if (spectatorList) {
      spectatorList.addEventListener('scroll', checkSpectatorScroll);
    }
    if (playerList) {
      playerList.addEventListener('scroll', checkPlayerScroll);
    }

    return () => {
      if (spectatorList) {
        spectatorList.removeEventListener('scroll', checkSpectatorScroll);
      }
      if (playerList) {
        playerList.removeEventListener('scroll', checkPlayerScroll);
      }
    };
  }, []);

  const handleSpectatorListScroll = (direction: 'up' | 'down') => {
    if (spectatorListRef.current) {
      const scrollAmount = 80;
      const currentScroll = spectatorListRef.current.scrollTop;
      const newScroll = direction === 'down'
        ? currentScroll + scrollAmount
        : Math.max(0, currentScroll - scrollAmount);

      spectatorListRef.current.scrollTo({
        top: newScroll,
        behavior: 'smooth'
      });
    }
  };

  const handlePlayerListScroll = (direction: 'up' | 'down') => {
    if (playerListRef.current) {
      const scrollAmount = 80;
      const currentScroll = playerListRef.current.scrollTop;
      const newScroll = direction === 'down'
        ? currentScroll + scrollAmount
        : Math.max(0, currentScroll - scrollAmount);

      playerListRef.current.scrollTo({
        top: newScroll,
        behavior: 'smooth'
      });
    }
  };

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
          {/* 참가자/관전자 변경 버튼들 */}
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '4px',
            padding: '0 4px'
          }}>
            <button
              onClick={() => handleChangeRole('PLAYER')}
              style={{
                flex: 1,
                padding: '6px 4px',
                fontSize: '11px',
                backgroundColor: currentPlayer?.role === 'PLAYER' ? '#5a7fd4' : '#7a9bea',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: currentPlayer?.role === 'SPECTATOR' && players.length < roomInfo.settings.maxPlayers ? 'pointer' : 'not-allowed',
                fontWeight: 'bold',
                opacity: currentPlayer?.role === 'SPECTATOR' && players.length < roomInfo.settings.maxPlayers ? 1 : 0.7
              }}
              disabled={!(currentPlayer?.role === 'SPECTATOR' && players.length < roomInfo.settings.maxPlayers)}
            >
              참가자 ({players.length}/{roomInfo.settings.maxPlayers})
            </button>
            <button
              onClick={() => handleChangeRole('SPECTATOR')}
              style={{
                flex: 1,
                padding: '6px 4px',
                fontSize: '11px',
                backgroundColor: currentPlayer?.role === 'SPECTATOR' ? '#5a7fd4' : '#7a9bea',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: currentPlayer?.role === 'PLAYER' && spectators.length < roomInfo.settings.maxSpectators ? 'pointer' : 'not-allowed',
                fontWeight: 'bold',
                opacity: currentPlayer?.role === 'PLAYER' && spectators.length < roomInfo.settings.maxSpectators ? 1 : 0.7
              }}
              disabled={!(currentPlayer?.role === 'PLAYER' && spectators.length < roomInfo.settings.maxSpectators)}
            >
              관전자 ({spectators.length}/{roomInfo.settings.maxSpectators})
            </button>
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
          gridTemplateRows: '1fr',
          gap: '0',
          backgroundColor: 'transparent',
          borderRadius: '0 0 8px 8px',
          flexShrink: 0,
          overflow: 'hidden'
        }}>
          {/* 왼쪽: 참가자 목록 + 버튼 영역 */}
          <div style={{
            gridColumn: '1',
            gridRow: '1',
            display: 'flex',
            flexDirection: 'column',
            backgroundColor: 'transparent',
            borderRight: 'none'
          }}>
            {/* 참가자 목록 */}
            <div
              style={{
                display: 'flex',
                flexDirection: 'column',
                backgroundColor: 'transparent',
                borderRight: 'none',
                position: 'relative',
                height: '600px',
                overflow: 'hidden',
                marginTop: '3px'
              }}
              onMouseEnter={() => setIsPlayerHovered(true)}
              onMouseLeave={() => setIsPlayerHovered(false)}
            >
              {!isPlayerAtTop && (
                <button
                  onClick={() => handlePlayerListScroll('up')}
                  style={{
                    position: 'absolute',
                    top: '0',
                    left: '0',
                    right: '0',
                    padding: '1px',
                    backgroundColor: isPlayerHovered ? 'rgba(0, 0, 0, 0.7)' : 'rgba(0, 0, 0, 0.4)',
                    color: '#fff',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    zIndex: 10,
                    fontSize: '14px',
                    fontWeight: 'bold',
                    transition: 'opacity 0.2s, background-color 0.2s',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    opacity: isPlayerHovered ? 0.3 : 0.1
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = 'rgba(0, 0, 0, 0.85)';
                    e.currentTarget.style.opacity = '0.5';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = isPlayerHovered ? 'rgba(0, 0, 0, 0.7)' : 'rgba(0, 0, 0, 0.4)';
                    e.currentTarget.style.opacity = isPlayerHovered ? '0.3' : '0.1';
                  }}
                >
                  <span style={{ fontSize: '20px' }}>▲</span>
                </button>
              )}
              <PlayerList
                ref={playerListRef}
                players={players}
                currentPlayerId={playerInfo.playerId}
                maxPlayers={roomInfo.settings.maxPlayers}
                showScore={false}
              />
              {!isPlayerAtBottom && (
                <button
                  onClick={() => handlePlayerListScroll('down')}
                  style={{
                    position: 'absolute',
                    bottom: '0',
                    left: '0',
                    right: '0',
                    padding: '1px',
                    backgroundColor: isPlayerHovered ? 'rgba(0, 0, 0, 0.7)' : 'rgba(0, 0, 0, 0.4)',
                    color: '#fff',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    zIndex: 10,
                    fontSize: '14px',
                    fontWeight: 'bold',
                    transition: 'opacity 0.2s, background-color 0.2s',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    opacity: isPlayerHovered ? 0.3 : 0.1
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = 'rgba(0, 0, 0, 0.85)';
                    e.currentTarget.style.opacity = '0.5';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = isPlayerHovered ? 'rgba(0, 0, 0, 0.7)' : 'rgba(0, 0, 0, 0.4)';
                    e.currentTarget.style.opacity = isPlayerHovered ? '0.3' : '0.1';
                  }}
                >
                  <span style={{ fontSize: '20px' }}>▼</span>
                </button>
              )}
            </div>

          </div>

          {/* 중앙: 이미지 영역 + 게임 설정 + 버튼 */}
          <div style={{
            gridColumn: '2',
            gridRow: '1',
            display: 'flex',
            flexDirection: 'column',
            backgroundColor: 'transparent',
            borderRight: 'none',
            position: 'relative',
            overflow: 'hidden'
          }}>
            {/* 캔버스 크기 영역 */}
            <div style={{
              flex: 1,
              display: 'flex',
              alignItems: 'flex-start',
              justifyContent: 'center',
              padding: '3px'
            }}>
              {/* 흰색 배경 컨테이너 (캔버스 크기) */}
              <div style={{
                width: '800px',
                height: '600px',
                backgroundColor: '#ffffff',
                position: 'relative'
              }}>
                {/* 반투명 오버레이 */}
                <div style={{
                  position: 'absolute',
                  top: 0,
                  left: 0,
                  width: '800px',
                  height: '600px',
                  backgroundColor: 'rgba(0, 0, 0, 0.6)',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  justifyContent: 'center',
                  padding: '20px',
                  boxSizing: 'border-box',
                  zIndex: 10
                }}>
                  {/* 게임 설정 영역 */}
                  <div style={{
                    width: '360px'
                  }}>
                    <GameSettings
                      settings={roomInfo.settings}
                      isHost={isHost}
                      currentPlayerCount={players.length}
                      onSettingsChange={(newSettings) => handleUpdateSettings(newSettings)}
                    />
                  </div>
                </div>

                {/* 초대코드 복사 / 준비 완료/시작하기 버튼들 */}
                <div style={{
                  position: 'absolute',
                  bottom: '10px',
                  left: '10px',
                  right: '10px',
                  display: 'flex',
                  gap: '10px',
                  zIndex: 20
                }}>
                  <button
                    onClick={handleCopyInviteCode}
                    style={{
                      flex: 0.8,
                      padding: '14px',
                      fontSize: '15px',
                      backgroundColor: isCopied ? '#4caf50' : '#7a9bea',
                      color: 'white',
                      border: 'none',
                      borderRadius: '6px',
                      cursor: 'pointer',
                      fontWeight: 'bold',
                      transition: 'background-color 0.5s ease',
                      outline: 'none'
                    }}
                    onMouseEnter={(e) => {
                      if (!isCopied) {
                        e.currentTarget.style.backgroundColor = '#6585d8';
                      }
                    }}
                    onMouseLeave={(e) => {
                      if (!isCopied) {
                        e.currentTarget.style.backgroundColor = '#7a9bea';
                      }
                    }}
                    onMouseDown={(e) => {
                      if (!isCopied) {
                        e.currentTarget.style.backgroundColor = '#5a75c8';
                      }
                    }}
                    onMouseUp={(e) => {
                      if (!isCopied) {
                        e.currentTarget.style.backgroundColor = '#6585d8';
                      }
                    }}
                  >
                    {isCopied ? '초대코드 복사 완료' : '초대코드 복사'}
                  </button>

                  {isHost ? (
                    <button
                      onClick={handleStartGame}
                      style={{
                        flex: 1.2,
                        padding: '14px',
                        fontSize: '15px',
                        backgroundColor: canStartGame ? '#d97aa0' : '#ccc',
                        color: 'white',
                        border: 'none',
                        borderRadius: '6px',
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
                        flex: 1.2,
                        padding: '14px',
                        fontSize: '15px',
                        backgroundColor: currentPlayer.ready ? '#ff9800' : '#d97aa0',
                        color: 'white',
                        border: 'none',
                        borderRadius: '6px',
                        cursor: 'pointer',
                        fontWeight: 'bold'
                      }}
                    >
                      {currentPlayer.ready ? '준비 취소' : '준비 완료'}
                    </button>
                  ) : (
                    <div style={{
                      flex: 1.2,
                      padding: '14px',
                      fontSize: '15px',
                      backgroundColor: '#e0e0e0',
                      color: '#888',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      borderRadius: '6px',
                      fontWeight: 'bold'
                    }}>
                      -
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* 우측: 관전자 목록 + 채팅창 (GameRoomPreview와 동일) */}
          <div style={{
            gridColumn: '3',
            gridRow: '1',
            display: 'flex',
            flexDirection: 'column',
            gap: '0',
            height: '609px',
            maxHeight: '609px',
            boxSizing: 'border-box',
            overflow: 'hidden',
            padding: '0'
          }}>
            {/* 관전자 목록 */}
            <div
              style={{
                flex: '1',
                display: 'flex',
                flexDirection: 'column',
                backgroundColor: 'transparent',
                borderBottom: 'none',
                padding: '8px 0 2px 0',
                minHeight: '156px',
                maxHeight: '156px',
                position: 'relative'
              }}
              onMouseEnter={() => setIsSpectatorHovered(true)}
              onMouseLeave={() => setIsSpectatorHovered(false)}
            >
              {!isSpectatorAtTop && (
                <button
                  onClick={() => handleSpectatorListScroll('up')}
                  style={{
                    position: 'absolute',
                    top: '0',
                    left: '0',
                    right: '0',
                    padding: '1px',
                    backgroundColor: isSpectatorHovered ? 'rgba(0, 0, 0, 0.7)' : 'rgba(0, 0, 0, 0.4)',
                    color: '#fff',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    zIndex: 10,
                    fontSize: '14px',
                    fontWeight: 'bold',
                    transition: 'opacity 0.2s, background-color 0.2s',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    opacity: isSpectatorHovered ? 0.3 : 0.1
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = 'rgba(0, 0, 0, 0.85)';
                    e.currentTarget.style.opacity = '0.5';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = isSpectatorHovered ? 'rgba(0, 0, 0, 0.7)' : 'rgba(0, 0, 0, 0.4)';
                    e.currentTarget.style.opacity = isSpectatorHovered ? '0.3' : '0.1';
                  }}
                >
                  <span style={{ fontSize: '20px' }}>▲</span>
                </button>
              )}
              <SpectatorList
                ref={spectatorListRef}
                spectators={spectators}
                currentPlayerId={playerInfo.playerId}
                maxSpectators={roomInfo.settings.maxSpectators}
              />
              {!isSpectatorAtBottom && (
                <button
                  onClick={() => handleSpectatorListScroll('down')}
                  style={{
                    position: 'absolute',
                    bottom: '0',
                    left: '0',
                    right: '0',
                    padding: '1px',
                    backgroundColor: isSpectatorHovered ? 'rgba(0, 0, 0, 0.7)' : 'rgba(0, 0, 0, 0.4)',
                    color: '#fff',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    zIndex: 10,
                    fontSize: '14px',
                    fontWeight: 'bold',
                    transition: 'opacity 0.2s, background-color 0.2s',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    opacity: isSpectatorHovered ? 0.3 : 0.1
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = 'rgba(0, 0, 0, 0.85)';
                    e.currentTarget.style.opacity = '0.5';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = isSpectatorHovered ? 'rgba(0, 0, 0, 0.7)' : 'rgba(0, 0, 0, 0.4)';
                    e.currentTarget.style.opacity = isSpectatorHovered ? '0.3' : '0.1';
                  }}
                >
                  <span style={{ fontSize: '20px' }}>▼</span>
                </button>
              )}
            </div>

            {/* 채팅창 */}
            <div style={{
              flex: '2.9',
              display: 'flex',
              flexDirection: 'column',
              backgroundColor: 'transparent',
              padding: '8px 0 6px 0',
              minHeight: 0
            }}>
              <ChatBox
                messages={messages}
                onSendMessage={handleSendMessage}
                disabled={false}
                currentPlayerId={playerInfo.playerId}
                isCorrect={false}
                isDrawer={false}
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
