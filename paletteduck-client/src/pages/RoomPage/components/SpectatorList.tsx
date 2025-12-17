import { forwardRef } from 'react';
import type { RoomPlayer } from '../../../types/game.types';

interface SpectatorListProps {
  spectators: RoomPlayer[];
  currentPlayerId: string;
  maxSpectators: number;
}

const SpectatorList = forwardRef<HTMLDivElement, SpectatorListProps>(({
  spectators,
  currentPlayerId
}, ref) => {
  return (
    <div
      ref={ref}
      style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
        gap: '6px',
        height: '100%',
        overflowY: 'auto',
        alignContent: 'start',
        paddingRight: '4px',
        scrollbarWidth: 'none',
        msOverflowStyle: 'none'
      } as React.CSSProperties & { scrollbarWidth?: string; msOverflowStyle?: string }}
      className="hide-scrollbar"
    >
      {spectators.map((player) => (
        <div
          key={player.playerId}
          style={{
            padding: '8px',
            backgroundColor: '#d0e1f9',
            borderRadius: '6px',
            border: player.playerId === currentPlayerId ? '2px solid #4a6bb3' : '2px solid transparent',
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
            color: '#333',
            width: '100%'
          }}>
            {player.nickname}
          </span>
        </div>
      ))}
      <style>
        {`
          .hide-scrollbar::-webkit-scrollbar {
            display: none;
          }
        `}
      </style>
    </div>
  );
});

SpectatorList.displayName = 'SpectatorList';

export default SpectatorList;