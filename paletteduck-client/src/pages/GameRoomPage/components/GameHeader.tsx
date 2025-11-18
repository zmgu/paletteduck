import React from 'react';
import type { GameState, GamePhase } from '../../../types/game.types';

interface GameHeaderProps {
  gameState: GameState;
  timeLeft: number;
  isDrawer: boolean;
}

export default function GameHeader({ gameState, timeLeft, isDrawer }: GameHeaderProps) {
  const getPhaseTitle = () => {
    if (gameState.phase === 'COUNTDOWN') {
      return '게임이 곧 시작됩니다...';
    }
    if (gameState.phase === 'WORD_SELECT') {
      return isDrawer ? '단어를 선택하세요!' : '출제자가 단어를 선택하고 있습니다...';
    }
    if (gameState.phase === 'DRAWING') {
      return '그림을 그리는 중...';
    }
    return '';
  };

  const getTimerColor = () => {
    if (gameState.phase === 'COUNTDOWN') return '#ff5722';
    if (timeLeft <= 5) return '#ff5722';
    if (timeLeft <= 10) return '#ff9800';
    if (gameState.phase === 'WORD_SELECT') return '#2196f3';
    return '#4caf50';
  };

  return (
    <div style={{ 
      padding: '30px', 
      marginTop: '20px', 
      backgroundColor: '#f5f5f5', 
      borderRadius: '8px',
      textAlign: 'center'
    }}>
      <h2 style={{ fontSize: '28px', margin: '0 0 10px 0' }}>
        {getPhaseTitle()}
      </h2>
      
      <div style={{ 
        fontSize: gameState.phase === 'COUNTDOWN' ? '64px' : '48px', 
        fontWeight: 'bold', 
        color: getTimerColor()
      }}>
        {gameState.phase === 'COUNTDOWN' ? timeLeft : `${timeLeft}초`}
      </div>
      
      <div style={{ marginTop: '20px', fontSize: '18px', color: '#666' }}>
        <p>라운드: {gameState.currentRound} / {gameState.totalRounds}</p>
        {gameState.currentTurn && (
          <p>출제자: <strong>{gameState.currentTurn.drawerNickname}</strong></p>
        )}
      </div>
    </div>
  );
}