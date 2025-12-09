import { useParams } from 'react-router-dom';
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
      padding: '10px'
    }}>
      {/* 헤더 영역 */}
      <div style={{
        width: '1000px',
        height: '80px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: 'rgba(255, 255, 255, 0.1)',
        borderRadius: '8px 8px 0 0'
      }}>
        <h1 style={{ margin: 0, fontSize: '24px', color: 'white' }}>
          대기방 - {roomInfo.roomId}
        </h1>
      </div>

      {/* 메인 레이아웃 */}
      <div style={{
        width: '1000px',
        height: '600px',
        display: 'flex',
        gap: '0',
        backgroundColor: 'rgba(255, 255, 255, 0.05)',
        borderRadius: '0 0 8px 8px',
        overflow: 'hidden'
      }}>
        {/* 왼쪽: 참가자 목록 */}
        <div style={{
          width: '220px',
          display: 'flex',
          flexDirection: 'column',
          backgroundColor: 'rgba(150, 130, 130, 0.3)',
          borderRight: '2px solid rgba(255, 255, 255, 0.1)'
        }}>
          <div style={{ flex: 1, overflowY: 'auto', padding: '6px' }}>
            <PlayerList
              players={players}
              currentPlayerId={currentPlayerId}
              maxPlayers={roomInfo.settings.maxPlayers}
              canChangeToPlayer={currentPlayer?.role === 'SPECTATOR' && players.length < roomInfo.settings.maxPlayers}
              onChangeToPlayer={() => changeRole('PLAYER')}
            />
          </div>
        </div>

        {/* 중앙: 게임 설정 + 버튼들 */}
        <div style={{
          width: '460px',
          display: 'flex',
          flexDirection: 'column',
          backgroundColor: 'rgba(180, 100, 100, 0.3)',
          borderRight: '2px solid rgba(255, 255, 255, 0.1)'
        }}>
          {/* 게임 설정 영역 */}
          <div style={{ flex: 1, overflowY: 'auto', padding: '10px' }}>
            <GameSettings
              settings={roomInfo.settings}
              isHost={isHost}
              currentPlayerCount={players.length}
              onSettingsChange={(newSettings) => updateSettings(newSettings, roomInfo.settings)}
            />
          </div>

          {/* 준비 완료 / 시작하기 버튼 */}
          <div style={{
            padding: '10px',
            backgroundColor: 'rgba(180, 180, 50, 0.4)',
            borderTop: '2px solid rgba(255, 255, 255, 0.1)'
          }}>
            {isHost ? (
              <button
                onClick={startGame}
                style={{
                  width: '100%',
                  padding: '15px',
                  fontSize: '16px',
                  backgroundColor: canStartGame ? '#4caf50' : '#ccc',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
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
                  width: '100%',
                  padding: '15px',
                  fontSize: '16px',
                  backgroundColor: currentPlayer.ready ? '#ff9800' : '#2196f3',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontWeight: 'bold'
                }}
              >
                {currentPlayer.ready ? '준비 취소' : '준비 완료'}
              </button>
            ) : null}
          </div>

          {/* 초대코드 복사 / 방 나가기 버튼 */}
          <div style={{
            display: 'flex',
            gap: '0',
            borderTop: '2px solid rgba(255, 255, 255, 0.1)'
          }}>
            <button
              onClick={() => copyInviteCode(roomInfo.inviteCode)}
              style={{
                marginBottom: '8px',
                flex: 1,
                padding: '10px',
                fontSize: '14px',
                backgroundColor: 'rgba(60, 80, 150, 0.6)',
                color: 'white',
                border: 'none',
                borderRight: '1px solid rgba(255, 255, 255, 0.1)',
                cursor: 'pointer',
                fontWeight: 'bold'
              }}
            >
              초대코드 복사
            </button>

            <button
              onClick={leaveRoom}
              style={{
                marginBottom: '8px',
                flex: 1,
                padding: '10px',
                fontSize: '14px',
                backgroundColor: 'rgba(100, 40, 40, 0.6)',
                color: 'white',
                border: 'none',
                cursor: 'pointer',
                fontWeight: 'bold'
              }}
            >
              방 나가기
            </button>
          </div>
        </div>

        {/* 오른쪽: 관전자 목록 + 채팅창 */}
        <div style={{
          width: '320px',
          display: 'flex',
          flexDirection: 'column'
        }}>
          {/* 관전자 목록 */}
          <div style={{
            height: '240px',
            display: 'flex',
            flexDirection: 'column',
            backgroundColor: 'rgba(50, 30, 50, 0.5)',
            borderBottom: '2px solid rgba(255, 255, 255, 0.1)'
          }}>
            <div style={{
              padding: '10px',
              backgroundColor: 'rgba(30, 20, 30, 0.7)',
              borderBottom: '2px solid rgba(255, 255, 255, 0.1)',
              fontWeight: 'bold',
              color: 'white',
              fontSize: '14px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between'
            }}>
              <span style={{ fontSize: '16px' }}>👀</span>
              <span style={{
                backgroundColor: 'rgba(255, 255, 255, 0.2)',
                padding: '2px 8px',
                borderRadius: '10px',
                fontSize: '12px'
              }}>
                {spectators.length}/{roomInfo.settings.maxSpectators}
              </span>
            </div>
            <div style={{ flex: 1, overflowY: 'auto', padding: '10px' }}>
              <SpectatorList
                spectators={spectators}
                currentPlayerId={currentPlayerId}
                maxSpectators={roomInfo.settings.maxSpectators}
                canChangeToSpectator={currentPlayer?.role === 'PLAYER' && spectators.length < roomInfo.settings.maxSpectators}
                onChangeToSpectator={() => changeRole('SPECTATOR')}
              />
            </div>
          </div>

          {/* 채팅창 */}
          <div style={{
            flex: 1,
            backgroundColor: 'rgba(80, 120, 80, 0.3)',
            display: 'flex',
            flexDirection: 'column',
            padding: '8px'
          }}>
            <ChatBox
              messages={chatMessages}
              onSendMessage={(message) => sendChat(message, playerInfo?.nickname || '')}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
