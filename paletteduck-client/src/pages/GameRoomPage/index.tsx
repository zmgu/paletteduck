import React from 'react';
import { useParams } from 'react-router-dom';
import { getPlayerInfo } from '../../utils/apiClient';
import { useGameState } from './hooks/useGameState';
import { useDrawing } from './hooks/useDrawing';
import { useCanvasClear } from './hooks/useCanvasClear';
import { useWordSelect } from './hooks/useWordSelect';
import GameHeader from './components/GameHeader';
import WordSelect from './components/WordSelect';
import DrawingArea from './components/DrawingArea';

export default function GameRoomPage() {
  const { roomId } = useParams<{ roomId: string }>();
  const playerInfo = getPlayerInfo();
  
  console.log('[GameRoomPage] Rendering with roomId:', roomId);
  
  const { gameState, timeLeft } = useGameState(roomId!);  // roomId 전달!
  const { drawingData, sendDrawing } = useDrawing(roomId!);
  const { clearSignal, clearCanvas } = useCanvasClear(roomId!);
  const { selectWord } = useWordSelect(roomId!);

  console.log('[GameRoomPage] Current gameState:', gameState);

  if (!gameState) {
    return <div style={{ padding: '20px' }}>게임 로딩 중...</div>;
  }

  const isDrawer = gameState.currentTurn?.drawerId === playerInfo?.playerId;

  console.log('[GameRoomPage] Phase:', gameState.phase);
  console.log('[GameRoomPage] IsDrawer:', isDrawer);
  console.log('[GameRoomPage] Should show WordSelect:', 
    gameState.phase === 'WORD_SELECT' && isDrawer && gameState.currentTurn);

  return (
    <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto' }}>
      <h1>게임 진행 중</h1>
      
      <GameHeader gameState={gameState} timeLeft={timeLeft} isDrawer={isDrawer} />

      {/* 단어 선택 단계 */}
      {gameState.phase === 'WORD_SELECT' && isDrawer && gameState.currentTurn && (
        <>
          <p style={{ color: 'blue', fontWeight: 'bold' }}>단어 선택 화면 렌더링됨</p>
          <WordSelect 
            turnInfo={gameState.currentTurn} 
            onSelectWord={(word) => {
              console.log('[GameRoomPage] Word selected:', word);
              selectWord(word);
            }} 
          />
        </>
      )}

      {/* 그리기 단계 */}
      {gameState.phase === 'DRAWING' && gameState.currentTurn && (
        <DrawingArea
          turnInfo={gameState.currentTurn}
          isDrawer={isDrawer}
          drawingData={drawingData}
          clearSignal={clearSignal}
          onDrawing={isDrawer ? sendDrawing : undefined}
          onClearCanvas={isDrawer ? clearCanvas : undefined}
        />
      )}

      {/* 디버그 정보 */}
      <details style={{ marginTop: '30px' }} open>
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