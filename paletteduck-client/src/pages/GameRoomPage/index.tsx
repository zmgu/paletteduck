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
  const [autoReturnCountdown, setAutoReturnCountdown] = useState<number>(20); // 20초 카운트다운 (테스트용)
  const wasInGameEnd = useRef<boolean>(false); // GAME_END 상태 추적

  // 관전자 도중 참가 판단
  const [spectatorJoinTurn, setSpectatorJoinTurn] = useState<number | null>(null);
  const seenWordSelectForTurnRef = useRef<Set<number>>(new Set());

  // Extract initial roomInfo from location state
  const initialRoomInfo = location.state?.roomInfo as RoomInfo | undefined;

  const { gameState, timeLeft } = useGameState(roomId!);
  const roomInfo = useRoomInfo(roomId!, initialRoomInfo);
  const { drawingData, sendDrawing, initialDrawingEvents } = useDrawing(roomId!, gameState);
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

  const handleReturnToWaiting = useCallback(() => {
    if (!roomId || !playerInfo?.playerId) return;
    // 대기방 복귀 요청 (playerId 전달)
    wsClient.send(`/app/room/${roomId}/return-to-waiting`, playerInfo.playerId);
    // 페이지 이동
    navigate(`/room/${roomId}`, { state: { returnFromGame: true } });
  }, [roomId, playerInfo?.playerId, navigate]);

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

  // 새로운 턴 시작 시 캔버스 이미지 초기화
  useLayoutEffect(() => {
    if (gameState?.phase === 'WORD_SELECT') {
      setCanvasImageUrl('');
    }
  }, [gameState?.phase]);

  // 게임 시작 시 직접 입력 횟수 초기화
  useEffect(() => {
    if (!gameState || !roomId || !playerInfo) return;

    if (gameState.currentRound === 1 && gameState.phase === 'COUNTDOWN') {
      const storageKey = `customWordUsed_${roomId}_${playerInfo.playerId}`;
      const wasUsed = localStorage.getItem(storageKey);

      if (wasUsed === 'true') {
        localStorage.removeItem(storageKey);
      }
    }
  }, [gameState?.currentRound, gameState?.phase, roomId, playerInfo]);

  if (!gameState) {
    return <div style={{ padding: '20px' }}>게임 로딩 중...</div>;
  }

  const isDrawer = gameState.currentTurn?.drawerId === playerInfo?.playerId;

  const currentPlayer = gameState.players?.find(p => p.playerId === playerInfo?.playerId);
  const isCorrect = currentPlayer?.isCorrect || false;

  // 관전자 여부 확인
  const currentRoomPlayer = roomInfo?.players?.find(p => p.playerId === playerInfo?.playerId);
  const isSpectator = currentRoomPlayer?.role === 'SPECTATOR';

  // 관전자 도중 참가 감지
  useEffect(() => {
    if (!isSpectator) {
      setSpectatorJoinTurn(null);
      seenWordSelectForTurnRef.current.clear();
      return;
    }

    if (!gameState?.currentTurn) return;

    const currentTurn = gameState.currentTurn.turnNumber;
    const currentPhase = gameState.phase;

    if (currentPhase === 'WORD_SELECT') {
      seenWordSelectForTurnRef.current.add(currentTurn);
    }

    if (currentPhase === 'DRAWING' && !seenWordSelectForTurnRef.current.has(currentTurn) && spectatorJoinTurn === null) {
      setSpectatorJoinTurn(currentTurn);
    }
  }, [isSpectator, gameState, spectatorJoinTurn]);

  const isSpectatorMidJoin = isSpectator &&
    spectatorJoinTurn !== null &&
    gameState?.currentTurn?.turnNumber === spectatorJoinTurn;

  const isChatDisabled = isDrawer || isSpectator;

  // GAME_END 페이즈에서 20초 카운트다운 및 자동 복귀 처리
  useEffect(() => {
    if (gameState?.phase !== 'GAME_END') {
      setAutoReturnCountdown(20); // 다른 페이즈로 변경되면 리셋 (테스트용)
      return;
    }

    // 카운트다운 시작
    const intervalId = setInterval(() => {
      setAutoReturnCountdown((prev) => {
        if (prev <= 1) {
          // 0초가 되면 타이머만 종료 (메시지 전송하지 않음)
          // 서버 타이머가 20초에 자동으로 처리함:
          // - 아무도 수동 복귀 안했으면 → 방 삭제 → null 브로드캐스트 → 모달 표시
          // - 누군가 수동 복귀했으면 → 방 유지 → WAITING 상태 → 자동 navigate
          clearInterval(intervalId);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(intervalId);
  }, [gameState?.phase, roomId, playerInfo?.playerId, navigate]);

  // GAME_END 상태 추적
  useEffect(() => {
    if (gameState?.phase === 'GAME_END') {
      wasInGameEnd.current = true;
    }
  }, [gameState?.phase]);

  // 자동 복귀 처리
  useEffect(() => {
    if (!wasInGameEnd.current) return; // 게임 종료 후가 아니면 무시

    // 카운트다운이 0이고 방 상태가 WAITING이면 자동으로 대기방으로 이동
    if (roomInfo?.status === 'WAITING' && autoReturnCountdown === 0) {
      navigate(`/room/${roomId}`, { state: { returnFromGame: true } });
    }
  }, [roomInfo, autoReturnCountdown, roomId, navigate]);

  // 현재 사용자의 투표 상태
  const currentVote = gameState.currentTurn?.votes?.[playerInfo?.playerId || ''] || 'NONE';

  // 순위 계산 (점수 내림차순)
  const sortedPlayers = [...(gameState.players || [])].sort((a, b) => b.score - a.score);

  // 추천수 1위 계산 (베스트 아티스트)
  const bestArtist = gameState.players?.reduce((best, player) => {
    const playerLikes = player.totalLikes || 0;
    const bestLikes = best?.totalLikes || 0;
    return playerLikes > bestLikes ? player : best;
  }, gameState.players?.[0]);

  return (
    <div style={{ padding: '20px', maxWidth: '1400px', margin: '0 auto' }}>
      {/* 게임 종료 화면이 아닐 때만 표시 */}
      {gameState.phase !== 'GAME_END' && (
        <>
          <h1>게임 진행 중</h1>
          <GameHeader gameState={gameState} timeLeft={timeLeft} isDrawer={isDrawer} />
        </>
      )}

      {gameState.phase === 'WORD_SELECT' && isDrawer && gameState.currentTurn && (
        <WordSelect
          turnInfo={gameState.currentTurn}
          onSelectWord={selectWord}
          roomId={roomId!}
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
              isSpectatorMidJoin={isSpectatorMidJoin}
              onDrawing={isDrawer ? sendDrawing : undefined}
              onClearCanvas={isDrawer ? clearCanvas : undefined}
              onProvideChosungHint={isDrawer ? provideChosungHint : undefined}
              onProvideLetterHint={isDrawer ? provideLetterHint : undefined}
              onVote={!isSpectator ? handleVote : undefined}
            />
          </div>

          <div style={{ width: '350px', display: 'flex', flexDirection: 'column', gap: '15px' }}>
            {/* 관전자 목록 */}
            {roomInfo && roomInfo.players?.filter(p => p && p.role === 'SPECTATOR').length > 0 && (
              <div>
                <h3 style={{ marginBottom: '10px' }}>👀 관전자 ({roomInfo.players.filter(p => p && p.role === 'SPECTATOR').length})</h3>
                <div style={{
                  backgroundColor: '#f8f9fa',
                  border: '1px solid #dee2e6',
                  borderRadius: '4px',
                  padding: '10px',
                  maxHeight: '150px',
                  overflowY: 'auto'
                }}>
                  {roomInfo.players
                    .filter(p => p && p.role === 'SPECTATOR' && p.playerId)
                    .map((spectator, index) => (
                      <div
                        key={spectator.playerId || `spectator-${index}`}
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
          <h2 style={{ textAlign: 'center', marginBottom: '10px' }}>🎉 게임 종료! 🎉</h2>

          {/* 베스트 아티스트 표시 */}
          {bestArtist && (bestArtist.totalLikes || 0) > 0 && (
            <div style={{
              textAlign: 'center',
              marginBottom: '30px',
              padding: '15px',
              backgroundColor: '#f0e5ff',
              borderRadius: '8px',
              border: '2px solid #9c27b0',
            }}>
              <div style={{ fontSize: '24px', marginBottom: '5px' }}>🎨 베스트 아티스트 🎨</div>
              <div style={{ fontSize: '20px', fontWeight: 'bold', color: '#9c27b0' }}>
                {bestArtist.nickname} ({bestArtist.totalLikes}개 추천)
              </div>
            </div>
          )}

          <div style={{ maxWidth: '600px', margin: '0 auto' }}>
            <h3 style={{ marginBottom: '20px' }}>최종 순위</h3>
            {sortedPlayers.map((player, index) => {
              const isBestArtist = bestArtist?.playerId === player.playerId && (bestArtist?.totalLikes || 0) > 0;
              return (
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
                    position: 'relative',
                  }}
                >
                  {/* 베스트 아티스트 왕관 */}
                  {isBestArtist && (
                    <div style={{
                      position: 'absolute',
                      top: '-10px',
                      right: '-10px',
                      fontSize: '32px',
                      transform: 'rotate(15deg)',
                    }}>
                      👑
                    </div>
                  )}

                  <span style={{
                    fontSize: '24px',
                    fontWeight: 'bold',
                    marginRight: '20px',
                    width: '40px',
                    textAlign: 'center',
                  }}>
                    {index === 0 ? '🥇' : index === 1 ? '🥈' : index === 2 ? '🥉' : `${index + 1}위`}
                  </span>

                  <div style={{ flex: 1 }}>
                    <div style={{
                      fontSize: '18px',
                      fontWeight: player.playerId === playerInfo?.playerId ? 'bold' : 'normal',
                      color: player.playerId === playerInfo?.playerId ? '#007bff' : '#000',
                    }}>
                      {player.nickname}
                      {player.playerId === playerInfo?.playerId && ' (나)'}
                    </div>
                    {/* 추천수 표시 */}
                    <div style={{ fontSize: '14px', color: '#666', marginTop: '4px' }}>
                      👍 추천 {player.totalLikes || 0}개
                      {(player.totalDislikes || 0) > 0 && ` • 👎 ${player.totalDislikes}개`}
                    </div>
                  </div>

                  <span style={{ fontSize: '20px', fontWeight: 'bold' }}>
                    {player.score}점
                  </span>
                </div>
              );
            })}
          </div>

          {/* 20초 카운트다운 표시 */}
          <div style={{
            textAlign: 'center',
            marginTop: '30px',
            padding: '15px',
            backgroundColor: autoReturnCountdown === 0 && roomInfo?.status !== 'WAITING'
              ? '#f8d7da'
              : autoReturnCountdown <= 10 ? '#fff3cd' : '#f0f8ff',
            borderRadius: '8px',
            border: `2px solid ${
              autoReturnCountdown === 0 && roomInfo?.status !== 'WAITING'
                ? '#dc3545'
                : autoReturnCountdown <= 10 ? '#ffc107' : '#007bff'
            }`,
          }}>
            <div style={{
              fontSize: '18px',
              fontWeight: 'bold',
              color: autoReturnCountdown === 0 && roomInfo?.status !== 'WAITING'
                ? '#721c24'
                : autoReturnCountdown <= 10 ? '#856404' : '#004085',
            }}>
              {autoReturnCountdown > 0
                ? `⏰ ${autoReturnCountdown}초 후 자동으로 대기방으로 복귀합니다`
                : roomInfo?.status === 'WAITING'
                  ? '복귀 중...'
                  : '⚠️ 아무도 대기방으로 이동하지 않아 방이 사라졌습니다'}
            </div>
            <div style={{ fontSize: '14px', color: '#666', marginTop: '5px' }}>
              {autoReturnCountdown > 0
                ? '아래 버튼을 눌러 바로 복귀할 수 있습니다. 아무도 복귀하지 않으면 방이 사라집니다.'
                : roomInfo?.status === 'WAITING'
                  ? ''
                  : '메인 화면으로 이동해주세요.'}
            </div>
          </div>

          <div style={{ textAlign: 'center', marginTop: '20px', display: 'flex', gap: '15px', justifyContent: 'center' }}>
            {/* 방이 삭제되지 않았을 때만 대기방 복귀 버튼 표시 */}
            {!(autoReturnCountdown === 0 && roomInfo?.status !== 'WAITING') && (
              <button
                onClick={handleReturnToWaiting}
                disabled={autoReturnCountdown === 0}
                style={{
                  padding: '12px 40px',
                  fontSize: '16px',
                  backgroundColor: autoReturnCountdown === 0 ? '#ccc' : '#28a745',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: autoReturnCountdown === 0 ? 'not-allowed' : 'pointer',
                  fontWeight: 'bold',
                }}
              >
                대기방으로 돌아가기
              </button>
            )}
            <button
              onClick={() => navigate('/')}
              style={{
                padding: '12px 40px',
                fontSize: '16px',
                backgroundColor: '#6c757d',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
              }}
            >
              메인으로
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