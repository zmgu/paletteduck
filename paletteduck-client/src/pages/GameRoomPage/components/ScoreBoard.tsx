import React from 'react';
import type { Player } from '../../../types/game.types';

interface ScoreBoardProps {
  players: Player[];
  currentDrawerId?: string;
}

export default function ScoreBoard({ players, currentDrawerId }: ScoreBoardProps) {
  // 점수 순으로 정렬
  const sortedPlayers = [...players].sort((a, b) => (b.score || 0) - (a.score || 0));

  return (
    <div style={{
      border: '2px solid #ccc',
      borderRadius: '8px',
      backgroundColor: '#fff',
      padding: '15px',
      minWidth: '250px',
    }}>
      <h3 style={{ margin: '0 0 15px 0', fontSize: '18px', fontWeight: 'bold' }}>
        점수판
      </h3>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
        {sortedPlayers.map((player, index) => {
          const isDrawer = player.playerId === currentDrawerId;
          const isCorrect = player.isCorrect;
          const rank = index + 1;

          return (
            <div
              key={player.playerId}
              style={{
                display: 'flex',
                alignItems: 'center',
                padding: '10px',
                borderRadius: '6px',
                backgroundColor: isDrawer 
                  ? '#e3f2fd' 
                  : isCorrect 
                    ? '#e8f5e9' 
                    : '#f5f5f5',
                border: isDrawer 
                  ? '2px solid #2196f3' 
                  : isCorrect 
                    ? '2px solid #4caf50' 
                    : '1px solid #ddd',
                transition: 'all 0.3s ease',
              }}
            >
              {/* 순위 */}
              <div style={{
                width: '30px',
                height: '30px',
                borderRadius: '50%',
                backgroundColor: rank === 1 
                  ? '#ffd700' 
                  : rank === 2 
                    ? '#c0c0c0' 
                    : rank === 3 
                      ? '#cd7f32' 
                      : '#e0e0e0',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontWeight: 'bold',
                fontSize: '14px',
                marginRight: '10px',
                color: rank <= 3 ? '#fff' : '#666',
              }}>
                {rank}
              </div>

              {/* 이름 */}
              <div style={{ flex: 1 }}>
                <div style={{
                  fontSize: '14px',
                  fontWeight: 'bold',
                  color: '#333',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px',
                }}>
                  {player.playerName}
                  {isDrawer && (
                    <span style={{
                      fontSize: '11px',
                      padding: '2px 6px',
                      backgroundColor: '#2196f3',
                      color: '#fff',
                      borderRadius: '3px',
                    }}>
                      출제자
                    </span>
                  )}
                  {isCorrect && !isDrawer && (
                    <span style={{
                      fontSize: '11px',
                      padding: '2px 6px',
                      backgroundColor: '#4caf50',
                      color: '#fff',
                      borderRadius: '3px',
                    }}>
                      정답
                    </span>
                  )}
                </div>
              </div>

              {/* 점수 */}
              <div style={{
                fontSize: '18px',
                fontWeight: 'bold',
                color: '#333',
              }}>
                {player.score || 0}
                <span style={{ fontSize: '12px', color: '#666', marginLeft: '2px' }}>점</span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}