import { useEffect, useRef, useState } from 'react';
import type { RoomPlayer } from '../../../types/game.types';

interface PlayerListProps {
  players: RoomPlayer[];
  currentPlayerId: string;
  maxPlayers: number;
}

export default function PlayerList({
  players,
  currentPlayerId
}: PlayerListProps) {
  const listRef = useRef<HTMLUListElement>(null);
  const [hasScroll, setHasScroll] = useState(false);
  const [isAtTop, setIsAtTop] = useState(true);
  const [isAtBottom, setIsAtBottom] = useState(false);

  useEffect(() => {
    const checkScroll = () => {
      if (listRef.current) {
        const { scrollHeight, clientHeight, scrollTop } = listRef.current;
        setHasScroll(scrollHeight > clientHeight);
        setIsAtTop(scrollTop <= 10);
        setIsAtBottom(scrollTop + clientHeight >= scrollHeight - 10);
      }
    };

    checkScroll();
    const list = listRef.current;
    if (list) {
      list.addEventListener('scroll', checkScroll);
      return () => list.removeEventListener('scroll', checkScroll);
    }
  }, [players]);

  return (
    <div style={{ position: 'relative', height: '100%' }}>
      <ul
        ref={listRef}
        style={{
          listStyle: 'none',
          padding: 0,
          margin: 0,
          height: '100%',
          overflowY: 'auto',
          scrollbarWidth: 'none', // Firefox
          msOverflowStyle: 'none', // IE/Edge
        }}
        className="hide-scrollbar"
      >
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

      {/* 스크롤 가능하고 맨 위가 아닐 때 상단 표시 */}
      {hasScroll && !isAtTop && (
        <div style={{
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          height: '40px',
          background: 'linear-gradient(to top, transparent, rgba(107, 117, 97, 0.9))',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          pointerEvents: 'none'
        }}>
          <span style={{ fontSize: '20px', color: 'white' }}>⬆</span>
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
          background: 'linear-gradient(to bottom, transparent, rgba(107, 117, 97, 0.9))',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          pointerEvents: 'none'
        }}>
          <span style={{ fontSize: '20px', color: 'white' }}>⬇</span>
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
}