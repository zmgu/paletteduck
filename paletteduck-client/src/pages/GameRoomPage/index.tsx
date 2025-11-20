import React from 'react';
import { useParams } from 'react-router-dom';
import { getPlayerInfo } from '../../utils/apiClient';
import { useGameState } from './hooks/useGameState';
import { useDrawing } from './hooks/useDrawing';
import { useCanvasClear } from './hooks/useCanvasClear';
import { useWordSelect } from './hooks/useWordSelect';
import { useChat } from './hooks/useChat';
import GameHeader from './components/GameHeader';
import WordSelect from './components/WordSelect';
import DrawingArea from './components/DrawingArea';
import ChatBox from './components/ChatBox';

export default function GameRoomPage() {
  const { roomId } = useParams<{ roomId: string }>();
  const playerInfo = getPlayerInfo();
  
  const { gameState, timeLeft } = useGameState(roomId!);
  const { drawingData, sendDrawing } = useDrawing(roomId!);
  const { clearSignal, clearCanvas } = useCanvasClear(roomId!);
  const { selectWord } = useWordSelect(roomId!);
  const { messages, sendMessage } = useChat(roomId!);

  if (!gameState) {
    return <div style={{ padding: '20px' }}>게임 로딩 중...</div>;
  }

  const isDrawer = gameState.currentTurn?.drawerId === playerInfo?.playerId;
  
  // 정답 맞춘 사람 확인
  const currentPlayer = gameState.players?.find(p => p.playerId === playerInfo?.playerId);
  const isCorrect = currentPlayer?.isCorrect || false;
  
  // 채팅 비활성화 조건: 출제자이거나 이미 정답 맞춤
  const isChatDisabled = isDrawer || isCorrect;

  return (
    <div style={{ padding: '20px', maxWidth: '1400px', margin: '0 auto' }}>
      <h1>게임 진행 중</h1>
      
      <GameHeader gameState={gameState} timeLeft={timeLeft} isDrawer={isDrawer} />

      {/* 단어 선택 */}
      {gameState.phase === 'WORD_SELECT' && isDrawer && gameState.currentTurn && (
        <WordSelect 
          turnInfo={gameState.currentTurn} 
          onSelectWord={selectWord} 
        />
      )}

      {/* 그리기 + 채팅 */}
      {gameState.phase === 'DRAWING' && gameState.currentTurn && (
        <div style={{ display: 'flex', gap: '20px', marginTop: '20px' }}>
          {/* 왼쪽: 그림 영역 */}
          <div style={{ flex: 1 }}>
            <DrawingArea
              turnInfo={gameState.currentTurn}
              isDrawer={isDrawer}
              drawingData={drawingData}
              clearSignal={clearSignal}
              onDrawing={isDrawer ? sendDrawing : undefined}
              onClearCanvas={isDrawer ? clearCanvas : undefined}
            />
          </div>

          {/* 오른쪽: 채팅 */}
          <div style={{ width: '350px' }}>
            <h3>채팅</h3>
            {isCorrect && (
              <div style={{
                padding: '10px',
                backgroundColor: '#d4edda',
                border: '1px solid #28a745',
                borderRadius: '4px',
                marginBottom: '10px',
                textAlign: 'center',
                fontWeight: 'bold',
                color: '#155724',
              }}>
                🎉 정답을 맞췄습니다!
              </div>
            )}
            {isDrawer && (
              <div style={{
                padding: '10px',
                backgroundColor: '#d1ecf1',
                border: '1px solid #0c5460',
                borderRadius: '4px',
                marginBottom: '10px',
                textAlign: 'center',
                fontWeight: 'bold',
                color: '#0c5460',
              }}>
                출제자는 채팅을 볼 수 없습니다
              </div>
            )}
            <ChatBox
              messages={messages}
              onSendMessage={sendMessage}
              disabled={isChatDisabled}
              currentPlayerId={playerInfo?.playerId || ''}
              isCorrect={isCorrect}
            />
          </div>
        </div>
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