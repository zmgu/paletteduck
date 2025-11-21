import React from 'react';
import type { GameState } from '../../../types/game.types';

interface GameHeaderProps {
  gameState: GameState;
  timeLeft: number;
  isDrawer: boolean;
}

export default function GameHeader({ gameState, timeLeft, isDrawer }: GameHeaderProps) {
  const currentTurn = gameState.currentTurn;
  
  if (!currentTurn) {
    return null;
  }

  return (
    <div style={{
      padding: '20px',
      backgroundColor: '#f8f9fa',
      borderRadius: '8px',
      marginBottom: '20px',
      border: '2px solid #dee2e6',
    }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        {/* 왼쪽: 라운드 & 턴 정보 */}
        <div>
          {/* ✅ 라운드 표시 */}
          <div style={{ fontSize: '16px', color: '#666', marginBottom: '5px' }}>
            라운드 {gameState.currentRound || 1} / {gameState.totalRounds || 3}
          </div>
          <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#333' }}>
            턴 {currentTurn.turnNumber}
          </div>
          <div style={{ fontSize: '18px', color: '#666', marginTop: '5px' }}>
            출제자: {currentTurn.drawerNickname}
          </div>
        </div>

        {/* 중앙: 역할 */}
        <div style={{
          padding: '15px 30px',
          backgroundColor: isDrawer ? '#e3f2fd' : '#fff3cd',
          border: `2px solid ${isDrawer ? '#2196f3' : '#ffc107'}`,
          borderRadius: '8px',
          fontSize: '20px',
          fontWeight: 'bold',
          color: isDrawer ? '#1976d2' : '#856404',
        }}>
          {isDrawer ? '🎨 출제자' : '🔍 참가자'}
        </div>

        {/* 오른쪽: 타이머 */}
        <div style={{
          fontSize: '48px',
          fontWeight: 'bold',
          color: timeLeft <= 10 ? '#dc3545' : '#28a745',
          textAlign: 'center',
        }}>
          {timeLeft}
          <div style={{ fontSize: '14px', color: '#666', fontWeight: 'normal' }}>
            초 남음
          </div>
        </div>
      </div>
    </div>
  );
}