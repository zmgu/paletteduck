import type { RoomPlayer } from '../../../types/game.types';

interface SpectatorListProps {
  spectators: RoomPlayer[];
  currentPlayerId: string;
  maxSpectators: number;
  canChangeToSpectator: boolean;
  onChangeToSpectator: () => void;
}

export default function SpectatorList({
  spectators,
  currentPlayerId,
  maxSpectators,
  canChangeToSpectator,
  onChangeToSpectator
}: SpectatorListProps) {
  return (
    <div style={{ border: '1px solid #ccc', padding: '15px', borderRadius: '8px' }}>
      <h2>관전자 ({spectators.length}/{maxSpectators})</h2>
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
      {canChangeToSpectator && (
        <button onClick={onChangeToSpectator} style={{ marginTop: '10px', width: '100%', padding: '10px' }}>
          관전자로 변경
        </button>
      )}
    </div>
  );
}