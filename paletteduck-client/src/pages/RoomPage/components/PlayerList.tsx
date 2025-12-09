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
  canChangeToPlayer,
  onChangeToPlayer
}: PlayerListProps) {
  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <ul style={{
        listStyle: 'none',
        padding: 0,
        margin: 0,
        flex: 1,
        overflowY: 'auto'
      }}>
        {players.map((player) => (
          <li
            key={player.playerId}
            style={{
              height: '50px',
              marginBottom: '6px',
              padding: '10px',
              backgroundColor: player.ready ? '#2d4a2d' : '#2b3232ff',
              borderRadius: '8px',
              border: player.playerId === currentPlayerId ? '2px solid #2196f3' : '2px solid transparent',
              fontWeight: player.playerId === currentPlayerId ? 'bold' : 'normal',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              boxSizing: 'border-box',
              position: 'relative'
            }}
          >
            {/* 왼쪽: 이미지 공간 */}
            <div style={{
              width: '40px',
              height: '40px',
              borderRadius: '50%',
              backgroundColor: '#ddd',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '20px',
              flexShrink: 0,
              position: 'relative'
            }}>
              🦆
              {/* 방장 배지 */}
              {player.host && (
                <span style={{
                  position: 'absolute',
                  top: '-2px',
                  right: '-2px',
                  fontSize: '14px',
                  lineHeight: '1'
                }}>
                  👑
                </span>
              )}
            </div>

            {/* 오른쪽: 닉네임 */}
            <div style={{
              flex: 1,
              overflow: 'hidden',
              paddingRight: player.ready ? '20px' : '0'
            }}>
              <span style={{
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
                fontSize: '12px',
                color: 'white',
                display: 'block'
              }}>
                {player.nickname}
              </span>
            </div>

            {/* 준비 완료 아이콘 - 절대 위치 */}
            {player.ready && (
              <span style={{
                position: 'absolute',
                right: '6px',
                top: '50%',
                transform: 'translateY(-50%)',
                fontSize: '16px',
                lineHeight: '1'
              }}>
                ✅
              </span>
            )}
          </li>
        ))}
      </ul>
      {canChangeToPlayer && (
        <button
          onClick={onChangeToPlayer}
          style={{
            marginTop: '10px',
            width: '100%',
            padding: '10px',
            backgroundColor: '#4caf50',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
            fontWeight: 'bold'
          }}
        >
          참가자로 변경
        </button>
      )}
    </div>
  );
}