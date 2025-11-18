import { useParams } from 'react-router-dom';
import { getPlayerInfo } from '../../utils/apiClient';
import { useRoomConnection } from './hooks/useRoomConnection';
import { useRoomActions } from './hooks/useRoomActions';
import PlayerList from './components/PlayerList';
import SpectatorList from './components/SpectatorList';
import GameSettings from './components/GameSettings';
import ChatBox from './components/ChatBox';

export default function RoomPage() {  // export default 확인
  const { roomId } = useParams<{ roomId: string }>();
  const { roomInfo, currentPlayerId, chatMessages, loading } = useRoomConnection(roomId!);
  const { toggleReady, changeRole, updateSettings, startGame, sendChat, copyInviteCode } = useRoomActions(roomId!, currentPlayerId);

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
    <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto' }}>
      <h1>대기방 - {roomInfo.roomId}</h1>
      
      <div style={{ display: 'flex', gap: '20px', marginTop: '20px' }}>
        <div style={{ flex: 1 }}>
          <PlayerList
            players={players}
            currentPlayerId={currentPlayerId}
            maxPlayers={roomInfo.settings.maxPlayers}
            canChangeToPlayer={currentPlayer?.role === 'SPECTATOR' && players.length < roomInfo.settings.maxPlayers}
            onChangeToPlayer={() => changeRole('PLAYER')}
          />
          
          <SpectatorList
            spectators={spectators}
            currentPlayerId={currentPlayerId}
            maxSpectators={roomInfo.settings.maxSpectators}
            canChangeToSpectator={currentPlayer?.role === 'PLAYER' && spectators.length < roomInfo.settings.maxSpectators}
            onChangeToSpectator={() => changeRole('SPECTATOR')}
          />
        </div>

        <GameSettings
          settings={roomInfo.settings}
          isHost={isHost}
          onSettingsChange={(newSettings) => updateSettings(newSettings, roomInfo.settings)}
        />
      </div>

      <ChatBox
        messages={chatMessages}
        onSendMessage={(message) => sendChat(message, playerInfo?.nickname || '')}
      />

      <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
        <button 
          onClick={() => copyInviteCode(roomInfo.inviteCode)} 
          style={{ flex: 1, padding: '15px', fontSize: '16px' }}
        >
          초대 코드 복사
        </button>
        
        {isHost ? (
          <button 
            onClick={startGame}
            style={{ 
              flex: 1, 
              padding: '15px', 
              fontSize: '16px', 
              backgroundColor: canStartGame ? '#4caf50' : '#ccc', 
              color: 'white',
              cursor: canStartGame ? 'pointer' : 'not-allowed'
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