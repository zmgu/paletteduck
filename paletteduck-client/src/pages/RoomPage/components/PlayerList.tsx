import type { RoomPlayer } from '../../../types/game.types';

interface PlayerListProps {
  players: RoomPlayer[];
  currentPlayerId: string;
  maxPlayers: number;
  canChangeToPlayer: boolean;
  onChangeToPlayer: () => void;
}

export default function PlayerList({
  players,
  currentPlayerId,
  maxPlayers,
  canChangeToPlayer,
  onChangeToPlayer
}: PlayerListProps) {
  return (
    <div style={{ border: '1px solid #ccc', padding: '15px', borderRadius: '8px', marginBottom: '15px' }}>
      <h2>참가자 ({players.length}/{maxPlayers})</h2>
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
      {canChangeToPlayer && (
        <button onClick={onChangeToPlayer} style={{ marginTop: '10px', width: '100%', padding: '10px' }}>
          참가자로 변경
        </button>
      )}
    </div>
  );
}