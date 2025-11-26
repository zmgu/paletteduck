import { useMemo, useCallback, useRef, useEffect, useLayoutEffect, useState } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { getPlayerInfo } from '../../utils/apiClient';
import { wsClient } from '../../utils/wsClient';
import { useGameState } from './hooks/useGameState';
import { useDrawing } from './hooks/useDrawing';
import { useCanvasClear } from './hooks/useCanvasClear';
import { useWordSelect } from './hooks/useWordSelect';
import { useChat } from './hooks/useChat';
import { useRoomInfo } from './hooks/useRoomInfo';
import GameHeader from './components/GameHeader';
import WordSelect from './components/WordSelect';
import DrawingArea from './components/DrawingArea';
import ChatBox from './components/ChatBox';
import TurnResult from './components/TurnResult';
import type { CanvasHandle } from './components/Canvas/Canvas';
import type { RoomInfo } from '../../types/game.types';

export default function GameRoomPage() {
  const { roomId } = useParams<{ roomId: string }>();
  const navigate = useNavigate();
  const location = useLocation();

  const playerInfo = useMemo(() => getPlayerInfo(), []);
  const canvasRef = useRef<CanvasHandle>(null);
  const [canvasImageUrl, setCanvasImageUrl] = useState<string>('');
  const prevPhaseRef = useRef<string | undefined>(undefined);

  // Extract initial roomInfo from location state
  const initialRoomInfo = location.state?.roomInfo as RoomInfo | undefined;

  const { gameState, timeLeft } = useGameState(roomId!);
  const roomInfo = useRoomInfo(roomId!, initialRoomInfo);
  const { drawingData, sendDrawing, initialDrawingEvents } = useDrawing(roomId!);
  const { clearSignal, clearCanvas } = useCanvasClear(roomId!);
  const { selectWord } = useWordSelect(roomId!);
  const { messages, sendMessage } = useChat(roomId!, gameState?.currentTurn?.turnNumber);

  const provideChosungHint = useCallback(() => {
    if (!playerInfo?.playerId) return;
    wsClient.send(`/app/room/${roomId}/game/hint/chosung`, playerInfo.playerId);
  }, [roomId, playerInfo?.playerId]);

  const provideLetterHint = useCallback(() => {
    if (!playerInfo?.playerId) return;
    wsClient.send(`/app/room/${roomId}/game/hint/letter`, playerInfo.playerId);
  }, [roomId, playerInfo?.playerId]);

  const handleVote = useCallback((voteType: 'LIKE' | 'DISLIKE' | 'NONE') => {
    if (!playerInfo?.playerId) return;
    wsClient.send(`/app/room/${roomId}/game/vote`, {
      voterId: playerInfo.playerId,
      voteType: voteType,
    });
  }, [roomId, playerInfo?.playerId]);

  // DRAWING 페이즈 중 주기적으로 캔버스 이미지 캡처 (백업)
  useEffect(() => {
    if (gameState?.phase !== 'DRAWING') return;

    const intervalId = setInterval(() => {
      if (canvasRef.current) {
        const imageUrl = canvasRef.current.captureImage();
        setCanvasImageUrl(imageUrl);
      }
    }, 1000); // 1초마다 캡처

    return () => clearInterval(intervalId);
  }, [gameState?.phase]);

  // 페이즈 전환 시 처리
  useLayoutEffect(() => {
    const currentPhase = gameState?.phase;

    // 새로운 턴 시작 시 이미지 초기화
    if (currentPhase === 'WORD_SELECT') {
      setCanvasImageUrl('');
    }

    // 현재 페이즈 저장
    prevPhaseRef.current = currentPhase;
  }, [gameState?.phase]);

  if (!gameState) {
    return <div style={{ padding: '20px' }}>게임 로딩 중...</div>;
  }

  const isDrawer = gameState.currentTurn?.drawerId === playerInfo?.playerId;

  const currentPlayer = gameState.players?.find(p => p.playerId === playerInfo?.playerId);
  const isCorrect = currentPlayer?.isCorrect || false;

  // 관전자 여부 확인
  const currentRoomPlayer = roomInfo?.players?.find(p => p.playerId === playerInfo?.playerId);
  const isSpectator = currentRoomPlayer?.role === 'SPECTATOR';

  const isChatDisabled = isDrawer || isSpectator;

  // 현재 사용자의 투표 상태
  const currentVote = gameState.currentTurn?.votes?.[playerInfo?.playerId || ''] || 'NONE';

  // 순위 계산 (점수 내림차순)
  const sortedPlayers = [...(gameState.players || [])].sort((a, b) => b.score - a.score);

  return (
    <div style={{ padding: '20px', maxWidth: '1400px', margin: '0 auto' }}>
      <h1>게임 진행 중</h1>
      
      <GameHeader gameState={gameState} timeLeft={timeLeft} isDrawer={isDrawer} />

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
              initialDrawingEvents={initialDrawingEvents}
              clearSignal={clearSignal}
              currentVote={currentVote}
              canvasRef={canvasRef}
              onDrawing={isDrawer ? sendDrawing : undefined}
              onClearCanvas={isDrawer ? clearCanvas : undefined}
              onProvideChosungHint={isDrawer ? provideChosungHint : undefined}
              onProvideLetterHint={isDrawer ? provideLetterHint : undefined}
              onVote={!isSpectator ? handleVote : undefined}
            />
          </div>

          <div style={{ width: '350px', display: 'flex', flexDirection: 'column', gap: '15px' }}>
            {/* 관전자 목록 */}
            {roomInfo && roomInfo.players.filter(p => p.role === 'SPECTATOR').length > 0 && (
              <div>
                <h3 style={{ marginBottom: '10px' }}>👀 관전자 ({roomInfo.players.filter(p => p.role === 'SPECTATOR').length})</h3>
                <div style={{
                  backgroundColor: '#f8f9fa',
                  border: '1px solid #dee2e6',
                  borderRadius: '4px',
                  padding: '10px',
                  maxHeight: '150px',
                  overflowY: 'auto'
                }}>
                  {roomInfo.players
                    .filter(p => p.role === 'SPECTATOR')
                    .map(spectator => (
                      <div
                        key={spectator.playerId}
                        style={{
                          padding: '5px 10px',
                          marginBottom: '5px',
                          backgroundColor: spectator.playerId === playerInfo?.playerId ? '#e3f2fd' : 'white',
                          borderRadius: '4px',
                          border: '1px solid #e0e0e0',
                          fontSize: '14px'
                        }}
                      >
                        {spectator.nickname}
                        {spectator.playerId === playerInfo?.playerId && ' (나)'}
                      </div>
                    ))}
                </div>
              </div>
            )}

            {/* 채팅 */}
            <div style={{ flex: 1 }}>
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
              {isSpectator && (
                <div style={{
                  padding: '10px',
                  backgroundColor: '#fff3cd',
                  border: '1px solid #ffc107',
                  borderRadius: '4px',
                  marginBottom: '10px',
                  textAlign: 'center',
                  fontWeight: 'bold',
                  color: '#856404',
                }}>
                  👀 관전자는 채팅을 입력할 수 없습니다
                </div>
              )}
              <ChatBox
                messages={messages}
                onSendMessage={sendMessage}
                disabled={isChatDisabled}
                currentPlayerId={playerInfo?.playerId || ''}
                isCorrect={isCorrect}
                isDrawer={isDrawer}
              />
            </div>
          </div>
        </div>
      )}

      {gameState.phase === 'TURN_RESULT' && gameState.currentTurn && (
        <TurnResult
          turnInfo={gameState.currentTurn}
          players={gameState.players}
          canvasImageUrl={canvasImageUrl}
        />
      )}

      {gameState.phase === 'ROUND_END' && gameState.currentTurn && (
        <div style={{
          marginTop: '20px',
          padding: '30px',
          backgroundColor: '#f8f9fa',
          borderRadius: '8px',
          textAlign: 'center',
        }}>
          <h2>라운드 종료!</h2>
          <p style={{ fontSize: '18px', marginTop: '10px' }}>
            정답: <strong>{gameState.currentTurn.word}</strong>
          </p>
          <p style={{ marginTop: '20px', color: '#666' }}>
            다음 라운드가 곧 시작됩니다...
          </p>
        </div>
      )}

      {gameState.phase === 'GAME_END' && (
        <div style={{
          marginTop: '20px',
          padding: '40px',
          backgroundColor: '#fff',
          borderRadius: '8px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
        }}>
          <h2 style={{ textAlign: 'center', marginBottom: '30px' }}>게임 종료!</h2>

          <div style={{ maxWidth: '600px', margin: '0 auto' }}>
            <h3 style={{ marginBottom: '20px' }}>최종 순위</h3>
            {sortedPlayers.map((player, index) => (
              <div
                key={player.playerId}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  padding: '15px 20px',
                  marginBottom: '10px',
                  backgroundColor: index === 0 ? '#fff3cd' : '#f8f9fa',
                  border: index === 0 ? '2px solid #ffc107' : '1px solid #dee2e6',
                  borderRadius: '8px',
                }}
              >
                <span style={{
                  fontSize: '24px',
                  fontWeight: 'bold',
                  marginRight: '20px',
                  width: '40px',
                  textAlign: 'center',
                }}>
                  {index === 0 ? '🥇' : index === 1 ? '🥈' : index === 2 ? '🥉' : `${index + 1}위`}
                </span>
                <span style={{
                  flex: 1,
                  fontSize: '18px',
                  fontWeight: player.playerId === playerInfo?.playerId ? 'bold' : 'normal',
                  color: player.playerId === playerInfo?.playerId ? '#007bff' : '#000',
                }}>
                  {player.nickname}
                  {player.playerId === playerInfo?.playerId && ' (나)'}
                </span>
                <span style={{ fontSize: '20px', fontWeight: 'bold' }}>
                  {player.score}점
                </span>
              </div>
            ))}
          </div>

          <div style={{ textAlign: 'center', marginTop: '40px' }}>
            <button
              onClick={() => navigate('/')}
              style={{
                padding: '12px 40px',
                fontSize: '16px',
                backgroundColor: '#007bff',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
              }}
            >
              메인으로 돌아가기
            </button>
          </div>
        </div>
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