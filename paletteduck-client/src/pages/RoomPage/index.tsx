import { useParams } from 'react-router-dom';
import { useState } from 'react';
import { getPlayerInfo } from '../../utils/apiClient';
import { useRoomConnection } from './hooks/useRoomConnection';
import { useRoomActions } from './hooks/useRoomActions';
import PlayerList from './components/PlayerList';
import SpectatorList from './components/SpectatorList';
import GameSettings from './components/GameSettings';
import ChatBox from './components/ChatBox';

export default function RoomPage() {
  const { roomId } = useParams<{ roomId: string }>();
  const { roomInfo, currentPlayerId, chatMessages, loading } = useRoomConnection(roomId!);
  const { toggleReady, changeRole, updateSettings, startGame, sendChat, copyInviteCode, leaveRoom } = useRoomActions(roomId!, currentPlayerId);
  const [isCopied, setIsCopied] = useState(false);

  // 초대코드 복사 핸들러
  const handleCopyInviteCode = (inviteCode: string) => {
    copyInviteCode(inviteCode);
    setIsCopied(true);
    setTimeout(() => {
      setIsCopied(false);
    }, 3000);
  };

  if (loading || !roomInfo) {
    return <div>로딩 중...</div>;
  }

  const playerInfo = getPlayerInfo();
  const currentPlayer = roomInfo.players.find(p => p.playerId === currentPlayerId);
  const isHost = currentPlayer?.host || false;
  const players = roomInfo.players.filter(p => p.role === 'PLAYER');
  const spectators = roomInfo.players.filter(p => p.role === 'SPECTATOR');

  const allPlayersReady = players.filter(p => !p.host && !p.ready).length === 0;
  const canStartGame = isHost && allPlayersReady && players.length >= 2;

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '10px',
      position: 'relative'
    }}>
      {/* 방 나가기 버튼 - 왼쪽 상단 고정 */}
      <button
        onClick={leaveRoom}
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

      {/* 헤더 영역 */}
      <div style={{
        width: '1120px',
        height: '60px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#d4d4d4',
        borderRadius: '8px 8px 0 0'
      }}>
        <h1 style={{ margin: 0, fontSize: '20px', color: '#333' }}>
          헤더
        </h1>
      </div>

      {/* 메인 레이아웃 */}
      <div style={{
        width: '1120px',
        height: '640px',
        display: 'grid',
        gridTemplateColumns: '240px 580px 300px',
        gridTemplateRows: '250px 1fr',
        gap: '0',
        backgroundColor: '#f0f0f0',
        borderRadius: '0 0 8px 8px',
        overflow: 'hidden'
      }}>
        {/* 왼쪽: 참가자 목록 + 버튼 영역 (전체 높이) */}
        <div style={{
          gridRow: '1 / 3',
          display: 'flex',
          flexDirection: 'column',
          backgroundColor: '#6b7561',
          borderRight: '2px solid #ddd'
        }}>
          {/* 참가자 목록 */}
          <div style={{ flex: 1, overflowY: 'auto', padding: '6px' }}>
            <PlayerList
              players={players}
              currentPlayerId={currentPlayerId}
              maxPlayers={roomInfo.settings.maxPlayers}
            />
          </div>

          {/* 참가자/관전자 변경 버튼들 */}
          <div style={{ display: 'flex', gap: '0' }}>
            <button
              onClick={() => changeRole('PLAYER')}
              style={{
                flex: 1,
                padding: '12px',
                fontSize: '13px',
                backgroundColor: '#8a8a8a',
                color: 'white',
                border: 'none',
                borderTop: '2px solid #ddd',
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
              onClick={() => changeRole('SPECTATOR')}
              style={{
                flex: 1,
                padding: '12px',
                fontSize: '13px',
                backgroundColor: '#8a8a8a',
                color: 'white',
                border: 'none',
                borderTop: '2px solid #ddd',
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
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          backgroundColor: '#b97a7a',
          borderRight: '2px solid #ddd',
          borderBottom: '2px solid #ddd'
        }}>
          <span style={{ fontSize: '18px', color: '#fff', fontWeight: 'bold' }}>이미지</span>
        </div>

        {/* 우측 상단: 게임설정 */}
        <div style={{
          display: 'flex',
          flexDirection: 'column',
          backgroundColor: '#2a1a2a',
          borderBottom: '2px solid #ddd',
          overflowY: 'auto',
          padding: '10px'
        }}>
          <GameSettings
            settings={roomInfo.settings}
            isHost={isHost}
            currentPlayerCount={players.length}
            onSettingsChange={(newSettings) => updateSettings(newSettings, roomInfo.settings)}
          />
        </div>

        {/* 중앙 하단: 관전자 목록 + 버튼 영역 */}
        <div style={{
          display: 'flex',
          flexDirection: 'column',
          backgroundColor: '#b8dbb8',
          borderRight: '2px solid #ddd'
        }}>
          <div style={{ flex: 1, overflowY: 'auto', padding: '10px' }}>
            <SpectatorList
              spectators={spectators}
              currentPlayerId={currentPlayerId}
              maxSpectators={roomInfo.settings.maxSpectators}
            />
          </div>

          {/* 초대코드 복사 / 준비 완료/시작하기 버튼들 */}
          <div style={{ display: 'flex', gap: '0' }}>
            <button
              onClick={() => handleCopyInviteCode(roomInfo.inviteCode)}
              style={{
                flex: 1,
                padding: '12px',
                fontSize: '13px',
                backgroundColor: isCopied ? '#4caf50' : '#7a5a9a',
                color: 'white',
                border: 'none',
                borderTop: '2px solid #ddd',
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
                onClick={startGame}
                style={{
                  flex: 1,
                  padding: '12px',
                  fontSize: '13px',
                  backgroundColor: canStartGame ? '#d97aa0' : '#ccc',
                  color: 'white',
                  border: 'none',
                  borderTop: '2px solid #ddd',
                  cursor: canStartGame ? 'pointer' : 'not-allowed',
                  fontWeight: 'bold'
                }}
                disabled={!canStartGame}
              >
                시작하기
              </button>
            ) : currentPlayer?.role === 'PLAYER' ? (
              <button
                onClick={toggleReady}
                style={{
                  flex: 1,
                  padding: '12px',
                  fontSize: '13px',
                  backgroundColor: currentPlayer.ready ? '#ff9800' : '#d97aa0',
                  color: 'white',
                  border: 'none',
                  borderTop: '2px solid #ddd',
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
                borderTop: '2px solid #ddd',
                fontWeight: 'bold'
              }}>
                -
              </div>
            )}
          </div>
        </div>

        {/* 우측 하단: 채팅창 */}
        <div style={{
          display: 'flex',
          flexDirection: 'column',
          backgroundColor: '#4a8c4a',
          padding: '6px'
        }}>
          <ChatBox
            messages={chatMessages}
            onSendMessage={(message) => sendChat(message, playerInfo?.nickname || '')}
          />
        </div>
      </div>
    </div>
  );
}
