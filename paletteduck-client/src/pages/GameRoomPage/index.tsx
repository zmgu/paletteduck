import React from 'react';
import { useParams } from 'react-router-dom';
import { getPlayerInfo } from '../../utils/apiClient';
import { useGameState } from './hooks/useGameState';
import { useGameWebSocket } from './hooks/useGameWebSocket';
import GameHeader from './components/GameHeader';
import WordSelect from './components/WordSelect';
import DrawingArea from './components/DrawingArea';

export default function GameRoomPage() {
  const { roomId } = useParams<{ roomId: string }>();
  const playerInfo = getPlayerInfo();
  const { gameState, setGameState, timeLeft } = useGameState();
  const {
    drawData,
    clearSignal,
    selectWord,
    handleDrawComplete,
    handleClearCanvas,
  } = useGameWebSocket({
    roomId: roomId!,
    onGameStateUpdate: setGameState,
  });

  if (!gameState) {
    return <div style={{ padding: '20px' }}>게임 로딩 중...</div>;
  }

  const isDrawer = gameState.currentTurn?.drawerId === playerInfo?.playerId;

  return (
    <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto' }}>
      <h1>게임 진행 중</h1>
      
      {/* Phase 표시 + 카운트다운 */}
      <GameHeader gameState={gameState} timeLeft={timeLeft} isDrawer={isDrawer} />

      {/* 단어 선택 (출제자만) */}
      {gameState.phase === 'WORD_SELECT' && isDrawer && gameState.currentTurn && (
        <WordSelect turnInfo={gameState.currentTurn} onSelectWord={selectWord} />
      )}

      {/* 그리기 단계 */}
      {gameState.phase === 'DRAWING' && gameState.currentTurn && (
        <DrawingArea
          turnInfo={gameState.currentTurn}
          isDrawer={isDrawer}
          drawData={drawData}
          clearSignal={clearSignal}
          onDrawComplete={isDrawer ? handleDrawComplete : undefined}
          onClearCanvas={isDrawer ? handleClearCanvas : undefined}
        />
      )}

      {/* 디버그 정보 */}
      <details style={{ marginTop: '30px' }}>
        <summary style={{ cursor: 'pointer', fontSize: '14px', color: '#666' }}>
          디버그 정보 보기
        </summary>
        <pre style={{ 
          backgroundColor: '#f5f5f5', 
          padding: '15px', 
          borderRadius: '4px',
          overflow: 'auto',
          fontSize: '12px',
          marginTop: '10px'
        }}>
          {JSON.stringify(gameState, null, 2)}
        </pre>
      </details>
    </div>
  );
}