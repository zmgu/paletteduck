import React, { useMemo } from 'react';
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
import ScoreBoard from './components/ScoreBoard';
import TurnEnd from './components/TurnEnd';
import GameEnd from './components/GameEnd';

export default function GameRoomPage() {
  const { roomId } = useParams<{ roomId: string }>();
  
  const playerInfo = useMemo(() => getPlayerInfo(), []);
  
  const { gameState, timeLeft } = useGameState(roomId!);
  const { drawingData, sendDrawing } = useDrawing(roomId!);
  const { clearSignal, clearCanvas } = useCanvasClear(roomId!);
  const { selectWord } = useWordSelect(roomId!);
  const { messages, sendMessage } = useChat(roomId!);

  if (!gameState) {
    return <div style={{ padding: '20px' }}>게임 로딩 중...</div>;
  }

  // ✅ GameState에서 직접 확인
  const isHost = gameState.hostId === playerInfo?.playerId;
  
  console.log('[GameRoomPage] gameState.hostId:', gameState.hostId);
  console.log('[GameRoomPage] playerInfo?.playerId:', playerInfo?.playerId);
  console.log('[GameRoomPage] isHost:', isHost);

  const isDrawer = gameState.currentTurn?.drawerId === playerInfo?.playerId;
  
  const currentPlayer = gameState.players?.find(p => p.playerId === playerInfo?.playerId);
  const isCorrect = currentPlayer?.isCorrect || false;
  
  const isChatDisabled = isDrawer;

  return (
    <div style={{ padding: '20px', maxWidth: '1400px', margin: '0 auto' }}>
      <h1>게임 진행 중</h1>
      
      {gameState.phase !== 'GAME_END' && (
        <GameHeader gameState={gameState} timeLeft={timeLeft} isDrawer={isDrawer} />
      )}

      {gameState.phase === 'WORD_SELECT' && isDrawer && gameState.currentTurn && (
        <WordSelect 
          turnInfo={gameState.currentTurn} 
          onSelectWord={selectWord} 
        />
      )}

      {gameState.phase === 'DRAWING' && gameState.currentTurn && (
        <div style={{ display: 'flex', gap: '20px', marginTop: '20px' }}>
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

          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px', width: '350px' }}>
            <ScoreBoard 
              players={gameState.players || []} 
              currentDrawerId={gameState.currentTurn?.drawerId}
            />

            <div>
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
        </div>
      )}

      {gameState.phase === 'ROUND_END' && gameState.currentTurn && (
        <TurnEnd turnInfo={gameState.currentTurn} />
      )}

      {/* ✅ isHost 전달 */}
      {gameState.phase === 'GAME_END' && (
        <GameEnd 
          gameState={gameState} 
          roomId={roomId!} 
          isHost={isHost}
        />
      )}

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
