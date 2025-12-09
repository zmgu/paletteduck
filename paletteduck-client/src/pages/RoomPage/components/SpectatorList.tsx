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
  canChangeToSpectator,
  onChangeToSpectator
}: SpectatorListProps) {
  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
        gap: '6px',
        flex: 1,
        overflowY: 'auto',
        alignContent: 'start',
        paddingRight: '4px',
        scrollbarGutter: 'stable'
      } as React.CSSProperties}>
        {spectators.map((player) => (
          <div
            key={player.playerId}
            style={{
              padding: '8px',
              backgroundColor: '#2b3232ff',
              borderRadius: '6px',
              border: player.playerId === currentPlayerId ? '2px solid #2196f3' : '2px solid transparent',
              display: 'flex',
              alignItems: 'center',
              fontSize: '11px',
              boxSizing: 'border-box',
              minWidth: 0
            }}
          >
            <span style={{
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
              color: 'white',
              width: '100%'
            }}>
              {player.nickname}
            </span>
          </div>
        ))}
      </div>
      {canChangeToSpectator && (
        <button
          onClick={onChangeToSpectator}
          style={{
            marginTop: '10px',
            width: '100%',
            padding: '10px',
            backgroundColor: '#9c27b0',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
            fontWeight: 'bold',
            fontSize: '12px'
          }}
        >
          관전자로 변경
        </button>
      )}
    </div>
  );
}