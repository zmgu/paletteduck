import { useState, useRef, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import GameHeader from './GameRoomPage/components/GameHeader';
import WordSelect from './GameRoomPage/components/WordSelect';
import DrawingArea from './GameRoomPage/components/DrawingArea';
import ChatBox from './GameRoomPage/components/ChatBox';
import TurnResult from './GameRoomPage/components/TurnResult';
import type { GameState, RoomInfo, ChatMessage, GamePhase } from '../types/game.types';
import type { CanvasHandle } from './GameRoomPage/components/Canvas/Canvas';

// 플레이어 Mock 데이터
const mockPlayers = [
  {
    playerId: 'player1',
    nickname: '나',
    score: 150,
    isCorrect: false,
    totalLikes: 3,
    totalDislikes: 0,
  },
  {
    playerId: 'player2',
    nickname: '그림쟁이',
    score: 200,
    isCorrect: false,
    totalLikes: 5,
    totalDislikes: 1,
  },
  {
    playerId: 'player3',
    nickname: '정답맞춤',
    score: 180,
    isCorrect: true,
    totalLikes: 2,
    totalDislikes: 0,
  },
  {
    playerId: 'player4',
    nickname: '플레이어4',
    score: 120,
    isCorrect: false,
    totalLikes: 1,
    totalDislikes: 2,
  },
];

const mockRoomInfo: RoomInfo = {
  roomId: 'preview',
  inviteCode: 'ABC123',
  status: 'PLAYING',
  settings: {
    maxPlayers: 6,
    rounds: 3,
    wordChoices: 3,
    drawTime: 80,
    maxSpectators: 10,
  },
  players: [
    {
      playerId: 'player1',
      nickname: '나',
      host: true,
      ready: true,
      role: 'PLAYER',
      score: 150,
      totalLikes: 3,
      totalDislikes: 0,
    },
    {
      playerId: 'player2',
      nickname: '그림쟁이',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 200,
      totalLikes: 5,
      totalDislikes: 1,
    },
    {
      playerId: 'player3',
      nickname: '정답맞춤',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 180,
      totalLikes: 2,
      totalDislikes: 0,
    },
    {
      playerId: 'player4',
      nickname: '플레이어4',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 120,
      totalLikes: 1,
      totalDislikes: 2,
    },
    {
      playerId: 'spectator1',
      nickname: '관전자1',
      host: false,
      ready: false,
      role: 'SPECTATOR',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'spectator2',
      nickname: '관전자2',
      host: false,
      ready: false,
      role: 'SPECTATOR',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
  ],
};

const mockMessages: ChatMessage[] = [
  {
    playerId: 'system',
    nickname: '시스템',
    message: '게임이 시작되었습니다!',
    type: 'SYSTEM',
    timestamp: Date.now() - 60000,
  },
  {
    playerId: 'player1',
    nickname: '나',
    message: '바나나?',
    type: 'NORMAL',
    timestamp: Date.now() - 45000,
  },
  {
    playerId: 'player4',
    nickname: '플레이어4',
    message: '오렌지',
    type: 'NORMAL',
    timestamp: Date.now() - 30000,
  },
  {
    playerId: 'player3',
    nickname: '정답맞춤',
    message: '사과!',
    type: 'CORRECT',
    timestamp: Date.now() - 15000,
  },
  {
    playerId: 'system',
    nickname: '시스템',
    message: '정답맞춤 님이 정답을 맞췄습니다!',
    type: 'SYSTEM',
    timestamp: Date.now() - 14000,
  },
];

// 각 페이즈별 Mock 데이터 생성 함수
const createMockGameState = (phase: GamePhase): GameState => {
  const baseState = {
    roomId: 'preview',
    currentRound: 2,
    totalRounds: 3,
    phase,
    phaseStartTime: Date.now(),
    drawTime: 80,
    turnOrder: ['player1', 'player2', 'player3', 'player4'],
    players: mockPlayers,
  };

  switch (phase) {
    case 'COUNTDOWN':
      return {
        ...baseState,
        currentTurn: {
          turnNumber: 5,
          drawerId: 'player2',
          drawerNickname: '그림쟁이',
          word: null,
          wordChoices: [],
          timeLeft: 3,
          correctPlayerIds: [],
          hintLevel: 0,
          currentHint: null,
          hintArray: null,
          revealedChosungPositions: [],
          revealedLetterPositions: [],
          votes: {},
          turnScores: {},
        },
      };

    case 'WORD_SELECT':
      return {
        ...baseState,
        currentTurn: {
          turnNumber: 5,
          drawerId: 'player2',
          drawerNickname: '그림쟁이',
          word: null,
          wordChoices: ['사과', '바나나', '포도'],
          timeLeft: 10,
          correctPlayerIds: [],
          hintLevel: 0,
          currentHint: null,
          hintArray: null,
          revealedChosungPositions: [],
          revealedLetterPositions: [],
          votes: {},
          turnScores: {},
        },
      };

    case 'DRAWING':
      return {
        ...baseState,
        currentTurn: {
          turnNumber: 5,
          drawerId: 'player2',
          drawerNickname: '그림쟁이',
          word: '사과',
          wordChoices: [],
          timeLeft: 45,
          correctPlayerIds: ['player3'],
          hintLevel: 1,
          currentHint: '사_',
          hintArray: ['사', '_'],
          revealedChosungPositions: [0],
          revealedLetterPositions: [],
          votes: {
            player1: 'NONE',
            player3: 'LIKE',
            player4: 'NONE',
          },
          turnScores: {},
        },
      };

    case 'TURN_RESULT':
      return {
        ...baseState,
        currentTurn: {
          turnNumber: 5,
          drawerId: 'player2',
          drawerNickname: '그림쟁이',
          word: '사과',
          wordChoices: [],
          timeLeft: 0,
          correctPlayerIds: ['player3', 'player1'],
          hintLevel: 2,
          currentHint: '사과',
          hintArray: ['사', '과'],
          revealedChosungPositions: [0, 1],
          revealedLetterPositions: [0, 1],
          votes: {
            player1: 'LIKE',
            player3: 'LIKE',
            player4: 'DISLIKE',
          },
          turnScores: {
            player2: 50,
            player3: 100,
            player1: 80,
          },
          turnEndReason: 'TIME_OUT',
        },
      };

    case 'ROUND_END':
      return {
        ...baseState,
        currentTurn: {
          turnNumber: 5,
          drawerId: 'player2',
          drawerNickname: '그림쟁이',
          word: '사과',
          wordChoices: [],
          timeLeft: 0,
          correctPlayerIds: ['player3', 'player1'],
          hintLevel: 2,
          currentHint: '사과',
          hintArray: ['사', '과'],
          revealedChosungPositions: [0, 1],
          revealedLetterPositions: [0, 1],
          votes: {},
          turnScores: {},
        },
      };

    case 'GAME_END':
      return {
        ...baseState,
        currentRound: 3,
        currentTurn: null,
      };

    default:
      return baseState as GameState;
  }
};

export default function GameRoomPreview() {
  const navigate = useNavigate();
  const [currentPhase, setCurrentPhase] = useState<GamePhase>('DRAWING');
  const [gameState, setGameState] = useState<GameState>(createMockGameState('DRAWING'));
  const [messages, setMessages] = useState<ChatMessage[]>(mockMessages);
  const [timeLeft, setTimeLeft] = useState(45);
  const [currentVote, setCurrentVote] = useState<'LIKE' | 'DISLIKE' | 'NONE'>('NONE');
  const [canvasImageUrl] = useState<string>('');
  const canvasRef = useRef<CanvasHandle>(null);

  const playerInfo = useMemo(() => ({
    playerId: 'player1',
    nickname: '나',
    token: 'mock-token',
  }), []);

  const handlePhaseChange = (phase: GamePhase) => {
    setCurrentPhase(phase);
    setGameState(createMockGameState(phase));

    // 페이즈별 시간 초기값 설정
    switch (phase) {
      case 'COUNTDOWN':
        setTimeLeft(3);
        break;
      case 'WORD_SELECT':
        setTimeLeft(10);
        break;
      case 'DRAWING':
        setTimeLeft(45);
        break;
      default:
        setTimeLeft(0);
    }
  };

  const isDrawer = gameState.currentTurn?.drawerId === playerInfo?.playerId;
  const currentPlayer = gameState.players?.find(p => p.playerId === playerInfo?.playerId);
  const isCorrect = currentPlayer?.isCorrect || false;

  const handleSendMessage = (message: string) => {
    const newMessage: ChatMessage = {
      playerId: playerInfo.playerId,
      nickname: playerInfo.nickname,
      message,
      type: 'NORMAL',
      timestamp: Date.now(),
    };
    setMessages([...messages, newMessage]);
  };

  const handleVote = (voteType: 'LIKE' | 'DISLIKE' | 'NONE') => {
    setCurrentVote(voteType);
    console.log('Vote:', voteType);
  };

  // 순위 계산 (GAME_END용)
  const sortedPlayers = [...(gameState.players || [])].sort((a, b) => b.score - a.score);
  const bestArtist = gameState.players?.reduce((best, player) => {
    const playerLikes = player.totalLikes || 0;
    const bestLikes = best?.totalLikes || 0;
    return playerLikes > bestLikes ? player : best;
  }, gameState.players?.[0]);

  return (
    <div style={{ padding: '20px', maxWidth: '1400px', margin: '0 auto' }}>
      {/* 게임 종료 화면이 아닐 때만 표시 */}
      {gameState.phase !== 'GAME_END' && (
        <GameHeader gameState={gameState} timeLeft={timeLeft} isDrawer={isDrawer} />
      )}

      {gameState.phase === 'WORD_SELECT' && isDrawer && gameState.currentTurn && (
        <WordSelect
          turnInfo={gameState.currentTurn}
          onSelectWord={(word) => console.log('Selected word:', word)}
          roomId="preview"
        />
      )}

      {gameState.phase === 'WORD_SELECT' && !isDrawer && (
        <div style={{
          marginTop: '20px',
          padding: '30px',
          backgroundColor: '#f8f9fa',
          borderRadius: '8px',
          textAlign: 'center',
        }}>
          <h2>출제자가 단어를 선택하고 있습니다...</h2>
          <p style={{ marginTop: '20px', color: '#666' }}>잠시만 기다려주세요.</p>
        </div>
      )}

      {gameState.phase === 'DRAWING' && gameState.currentTurn && (
        <div style={{ display: 'flex', gap: '20px', marginTop: '20px' }}>
          <div style={{ flex: 1 }}>
            <DrawingArea
              turnInfo={gameState.currentTurn}
              isDrawer={isDrawer}
              drawingData={null}
              initialDrawingEvents={[]}
              clearSignal={0}
              currentVote={currentVote}
              canvasRef={canvasRef}
              isSpectatorMidJoin={false}
              onDrawing={isDrawer ? () => {} : undefined}
              onClearCanvas={isDrawer ? () => console.log('Clear canvas') : undefined}
              onProvideChosungHint={isDrawer ? () => console.log('Chosung hint') : undefined}
              onProvideLetterHint={isDrawer ? () => console.log('Letter hint') : undefined}
              onVote={handleVote}
            />
          </div>

          <div style={{ width: '350px', display: 'flex', flexDirection: 'column', gap: '15px' }}>
            {/* 관전자 목록 */}
            {mockRoomInfo && mockRoomInfo.players?.filter(p => p && p.role === 'SPECTATOR').length > 0 && (
              <div>
                <h3 style={{ marginBottom: '10px' }}>
                  👀 관전자 ({mockRoomInfo.players.filter(p => p && p.role === 'SPECTATOR').length})
                </h3>
                <div style={{
                  backgroundColor: '#f8f9fa',
                  border: '1px solid #dee2e6',
                  borderRadius: '4px',
                  padding: '10px',
                  maxHeight: '150px',
                  overflowY: 'auto'
                }}>
                  {mockRoomInfo.players
                    .filter(p => p && p.role === 'SPECTATOR' && p.playerId)
                    .map((spectator, index) => (
                      <div
                        key={spectator.playerId || `spectator-${index}`}
                        style={{
                          padding: '5px 10px',
                          marginBottom: '5px',
                          backgroundColor: 'white',
                          borderRadius: '4px',
                          border: '1px solid #e0e0e0',
                          fontSize: '14px'
                        }}
                      >
                        {spectator.nickname}
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
              <ChatBox
                messages={messages}
                onSendMessage={handleSendMessage}
                disabled={isDrawer}
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
          isSpectatorMidJoin={false}
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

          <div style={{ textAlign: 'center', marginTop: '20px', display: 'flex', gap: '15px', justifyContent: 'center' }}>
            <button
              onClick={() => console.log('대기방으로')}
              style={{
                padding: '12px 40px',
                fontSize: '16px',
                backgroundColor: '#28a745',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontWeight: 'bold',
              }}
            >
              대기방으로 돌아가기
            </button>
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

      {/* 페이즈 전환 컨트롤 */}
      <div style={{
        marginTop: '20px',
        padding: '15px',
        backgroundColor: '#e3f2fd',
        borderRadius: '8px',
        border: '1px solid #2196f3',
      }}>
        <h4>페이즈 전환</h4>
        <div style={{ display: 'flex', gap: '10px', marginTop: '10px', flexWrap: 'wrap' }}>
          {(['COUNTDOWN', 'WORD_SELECT', 'DRAWING', 'TURN_RESULT', 'ROUND_END', 'GAME_END'] as GamePhase[]).map((phase) => (
            <button
              key={phase}
              onClick={() => handlePhaseChange(phase)}
              style={{
                padding: '8px 16px',
                backgroundColor: currentPhase === phase ? '#2196f3' : '#90caf9',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontWeight: currentPhase === phase ? 'bold' : 'normal',
              }}
            >
              {phase}
            </button>
          ))}
        </div>
      </div>

      {/* 타이머 컨트롤 (테스트용) */}
      {['COUNTDOWN', 'WORD_SELECT', 'DRAWING'].includes(currentPhase) && (
        <div style={{
          marginTop: '20px',
          padding: '15px',
          backgroundColor: '#fff3cd',
          borderRadius: '8px',
          border: '1px solid #ffc107',
        }}>
          <h4>타이머 컨트롤</h4>
          <div style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
            <button
              onClick={() => setTimeLeft(Math.max(0, timeLeft - 5))}
              style={{
                padding: '8px 16px',
                backgroundColor: '#ff9800',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
              }}
            >
              시간 -5초
            </button>
            <button
              onClick={() => setTimeLeft(timeLeft + 5)}
              style={{
                padding: '8px 16px',
                backgroundColor: '#4caf50',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
              }}
            >
              시간 +5초
            </button>
            <span style={{ marginLeft: '10px', lineHeight: '32px' }}>
              현재 시간: {timeLeft}초
            </span>
          </div>
        </div>
      )}

      {/* 프리뷰 알림 배너 */}
      <div style={{
        marginTop: '20px',
        padding: '15px',
        backgroundColor: '#f0f0f0',
        border: '2px solid #999',
        borderRadius: '8px',
        textAlign: 'center',
        fontWeight: 'bold',
        color: '#555',
      }}>
        🎨 게임 페이지 프리뷰 모드 (서버 연결 없음)
      </div>
    </div>
  );
}
