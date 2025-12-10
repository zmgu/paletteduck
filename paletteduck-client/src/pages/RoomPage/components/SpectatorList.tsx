import type { RoomPlayer } from '../../../types/game.types';

interface SpectatorListProps {
  spectators: RoomPlayer[];
  currentPlayerId: string;
  maxSpectators: number;
}

export default function SpectatorList({
  spectators,
  currentPlayerId
}: SpectatorListProps) {
  return (
    <div style={{
      display: 'grid',
      gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
      gap: '6px',
      height: '100%',
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
  );
}