import { useEffect, useRef, useState, forwardRef } from 'react';
import type { RoomPlayer } from '../../../types/game.types';
import duckBrushPaletteLeft from '../../../assets/duck_brush_palette_left.png';
import duckBrushPaletteRight from '../../../assets/duck_brush_palette_right.png';
import duckPaintingCanvas from '../../../assets/duck_painting_canvas.png';
import duckPaletteOnlyLeft from '../../../assets/duck_palette_only_left.png';
import duckPaletteOnlyRight from '../../../assets/duck_palette_only_right.png';

const duckImages = [
  duckBrushPaletteLeft,
  duckBrushPaletteRight,
  duckPaintingCanvas,
  duckPaletteOnlyLeft,
  duckPaletteOnlyRight,
];

// playerId를 기반으로 일관된 랜덤 인덱스 생성
const getDuckImageIndex = (playerId: string): number => {
  let hash = 0;
  for (let i = 0; i < playerId.length; i++) {
    hash = ((hash << 5) - hash) + playerId.charCodeAt(i);
    hash = hash & hash;
  }
  return Math.abs(hash) % duckImages.length;
};

interface PlayerListProps {
  players: RoomPlayer[];
  currentPlayerId: string;
  maxPlayers: number;
}

const PlayerList = forwardRef<HTMLUListElement, PlayerListProps>(({
  players,
  currentPlayerId
}, ref) => {
  const listRef = useRef<HTMLUListElement>(null);
  const scrollRef = (ref as React.RefObject<HTMLUListElement>) || listRef;
  const [hasScroll, setHasScroll] = useState(false);
  const [isAtTop, setIsAtTop] = useState(true);
  const [isAtBottom, setIsAtBottom] = useState(false);

  // 점수 기준으로 정렬하여 등수 계산
  const sortedPlayers = [...players].sort((a, b) => (b.score || 0) - (a.score || 0));
  const playerRanks = new Map(sortedPlayers.map((player, index) => [player.playerId, index + 1]));

  useEffect(() => {
    const checkScroll = () => {
      if (scrollRef.current) {
        const { scrollHeight, clientHeight, scrollTop } = scrollRef.current;
        setHasScroll(scrollHeight > clientHeight);
        setIsAtTop(scrollTop <= 10);
        setIsAtBottom(scrollTop + clientHeight >= scrollHeight - 10);
      }
    };

    checkScroll();
    const list = scrollRef.current;
    if (list) {
      list.addEventListener('scroll', checkScroll);
      return () => list.removeEventListener('scroll', checkScroll);
    }
  }, [players, scrollRef]);

  return (
    <div style={{ position: 'relative', height: '100%' }}>
      <ul
        ref={scrollRef}
        style={{
          listStyle: 'none',
          padding: '8px',
          margin: 0,
          height: '100%',
          overflowY: 'auto',
          scrollbarWidth: 'none', // Firefox
          msOverflowStyle: 'none', // IE/Edge
          boxSizing: 'border-box',
        }}
        className="hide-scrollbar"
      >
      {sortedPlayers.map((player) => {
        const rank = playerRanks.get(player.playerId) || 0;
        return (
          <li
            key={player.playerId}
            style={{
              height: '60px',
              marginBottom: '4px',
              padding: '6px',
              backgroundColor: player.ready ? '#d0e1f9' : '#c5d9f5',
              borderRadius: '6px',
              border: player.playerId === currentPlayerId ? '2px solid #4a6bb3' : '2px solid transparent',
              fontWeight: player.playerId === currentPlayerId ? 'bold' : 'normal',
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              boxSizing: 'border-box',
              position: 'relative'
            }}
          >
            {/* 왼쪽: 등수 */}
            <div style={{
              width: '28px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '16px',
              fontWeight: 'bold',
              color: rank === 1 ? '#ffd700' : rank === 2 ? '#c0c0c0' : rank === 3 ? '#cd7f32' : '#333',
              flexShrink: 0
            }}>
              {rank === 1 ? '🥇' : rank === 2 ? '🥈' : rank === 3 ? '🥉' : rank}
            </div>

            {/* 중앙: 닉네임 + 점수/추천/비추천 */}
            <div style={{
              flex: 1,
              display: 'flex',
              flexDirection: 'column',
              gap: '2px',
              overflow: 'hidden',
              minWidth: 0
            }}>
              {/* 위: 닉네임 */}
              <div style={{
                fontSize: '12px',
                color: '#333',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap'
              }}>
                {player.nickname}
              </div>

              {/* 아래: 점수/추천/비추천 */}
              <div style={{
                fontSize: '10px',
                color: '#aaa',
                display: 'flex',
                gap: '6px',
                alignItems: 'center'
              }}>
                <span style={{ color: '#ffd700' }}>{player.score || 0}점</span>
                <span style={{ color: '#ffd75e' }}>👍{player.totalLikes || 0}</span>
                {(player.totalDislikes || 0) > 0 && (
                  <span style={{ color: '#ff8566' }}>👎{player.totalDislikes}</span>
                )}
              </div>
            </div>

            {/* 오른쪽: 캐릭터 */}
            <div style={{
              width: '50px',
              height: '50px',
              borderRadius: '50%',
              backgroundColor: '#ddd',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0,
              position: 'relative',
              overflow: 'hidden'
            }}>
              <img
                src={duckImages[getDuckImageIndex(player.playerId)]}
                alt="duck"
                style={{
                  width: '100%',
                  height: '100%',
                  objectFit: 'cover'
                }}
              />
              {/* 방장 배지 */}
              {player.host && (
                <span style={{
                  position: 'absolute',
                  top: '-4px',
                  right: '-4px',
                  fontSize: '14px',
                  lineHeight: '1'
                }}>
                  👑
                </span>
              )}
            </div>
          </li>
        );
      })}
      </ul>

      {/* 스크롤 가능하고 맨 위가 아닐 때 상단 표시 */}
      {hasScroll && !isAtTop && (
        <div style={{
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          height: '40px',
          background: 'linear-gradient(to top, transparent, rgba(232, 240, 252, 0.95))',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          pointerEvents: 'none'
        }}>
          <span style={{ fontSize: '20px', color: '#4a6bb3' }}>⬆</span>
        </div>
      )}

      {/* 스크롤 가능하고 끝이 아닐 때 하단 표시 */}
      {hasScroll && !isAtBottom && (
        <div style={{
          position: 'absolute',
          bottom: 0,
          left: 0,
          right: 0,
          height: '40px',
          background: 'linear-gradient(to bottom, transparent, rgba(232, 240, 252, 0.95))',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          pointerEvents: 'none'
        }}>
          <span style={{ fontSize: '20px', color: '#4a6bb3' }}>⬇</span>
        </div>
      )}

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

PlayerList.displayName = 'PlayerList';

export default PlayerList;