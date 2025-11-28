import type { Ref } from 'react';
import Canvas from './Canvas';
import type { TurnInfo, VoteType } from '../../../types/game.types';
import type { DrawingData } from '../../../types/drawing.types';
import type { CanvasHandle } from './Canvas/Canvas';

interface DrawingAreaProps {
  turnInfo: TurnInfo;
  isDrawer: boolean;
  drawingData: DrawingData | null;
  initialDrawingEvents?: DrawingData[];
  clearSignal: number;
  currentVote?: VoteType;
  canvasRef?: Ref<CanvasHandle>;
  isSpectatorMidJoin?: boolean;
  onDrawing?: (data: Omit<DrawingData, 'playerId'>) => void;
  onClearCanvas?: () => void;
  onProvideChosungHint?: () => void;
  onProvideLetterHint?: () => void;
  onVote?: (voteType: VoteType) => void;
}

export default function DrawingArea({
  turnInfo,
  isDrawer,
  drawingData,
  initialDrawingEvents,
  clearSignal,
  currentVote,
  canvasRef,
  isSpectatorMidJoin,
  onDrawing,
  onClearCanvas,
  onProvideChosungHint,
  onProvideLetterHint,
  onVote,
}: DrawingAreaProps) {
  const canUseChosungHint = turnInfo.hintLevel >= 2;
  const canUseLetterHint = turnInfo.hintLevel >= 2;

  // 힌트 표시: hintLevel에 따라 다르게 표시
  const displayHint = (() => {
    if (turnInfo.hintLevel === 0) {
      // Level 0: 힌트 없음 - 물음표로 표시
      return '???';
    } else if (turnInfo.hintArray) {
      // Level 1 이상: hintArray 사용
      return turnInfo.hintArray.join(' ');
    } else {
      // fallback
      return '???';
    }
  })();

  return (
    <>
      <div style={{
        marginTop: '20px',
        padding: '30px',
        border: '2px solid #4caf50',
        borderRadius: '12px',
        backgroundColor: '#e8f5e9',
        textAlign: 'center'
      }}>
        {isDrawer ? (
          <>
            <p style={{
              fontSize: '32px',
              fontWeight: 'bold',
              margin: 0,
              color: '#2e7d32'
            }}>
              단어: {turnInfo.word}
            </p>

            {/* 힌트 제공 버튼 */}
            <div style={{ marginTop: '20px', display: 'flex', gap: '10px', justifyContent: 'center' }}>
              <button
                onClick={onProvideChosungHint}
                disabled={!canUseChosungHint}
                style={{
                  padding: '10px 20px',
                  fontSize: '16px',
                  fontWeight: 'bold',
                  backgroundColor: canUseChosungHint ? '#ff9800' : '#ccc',
                  color: '#fff',
                  border: 'none',
                  borderRadius: '8px',
                  cursor: canUseChosungHint ? 'pointer' : 'not-allowed',
                  opacity: canUseChosungHint ? 1 : 0.6,
                  transition: 'all 0.2s',
                }}
                onMouseEnter={(e) => {
                  if (canUseChosungHint) {
                    e.currentTarget.style.backgroundColor = '#fb8c00';
                  }
                }}
                onMouseLeave={(e) => {
                  if (canUseChosungHint) {
                    e.currentTarget.style.backgroundColor = '#ff9800';
                  }
                }}
              >
                💡 초성 힌트 {!canUseChosungHint && '(40초 후)'}
              </button>

              <button
                onClick={onProvideLetterHint}
                disabled={!canUseLetterHint}
                style={{
                  padding: '10px 20px',
                  fontSize: '16px',
                  fontWeight: 'bold',
                  backgroundColor: canUseLetterHint ? '#f44336' : '#ccc',
                  color: '#fff',
                  border: 'none',
                  borderRadius: '8px',
                  cursor: canUseLetterHint ? 'pointer' : 'not-allowed',
                  opacity: canUseLetterHint ? 1 : 0.6,
                  transition: 'all 0.2s',
                }}
                onMouseEnter={(e) => {
                  if (canUseLetterHint) {
                    e.currentTarget.style.backgroundColor = '#e53935';
                  }
                }}
                onMouseLeave={(e) => {
                  if (canUseLetterHint) {
                    e.currentTarget.style.backgroundColor = '#f44336';
                  }
                }}
              >
                🔥 글자 힌트 {!canUseLetterHint && '(40초 후)'}
              </button>
            </div>
          </>
        ) : (
          <p style={{
            fontSize: '28px',
            fontWeight: 'bold',
            margin: 0,
            letterSpacing: '8px',
            color: '#666'
          }}>
            {displayHint}
          </p>
        )}
      </div>

      <div style={{ marginTop: '20px', display: 'flex', justifyContent: 'center' }}>
        <Canvas
          ref={canvasRef}
          isDrawer={isDrawer}
          onDrawing={onDrawing}
          drawingData={drawingData}
          initialDrawingEvents={initialDrawingEvents}
          clearSignal={clearSignal}
          onClearRequest={onClearCanvas}
          turnNumber={turnInfo.turnNumber}
          isSpectatorMidJoin={isSpectatorMidJoin}
        />
      </div>

      {/* 추천/비추천 버튼 - 출제자가 아닐 때만 표시 */}
      {!isDrawer && onVote && (
        <div style={{
          marginTop: '20px',
          display: 'flex',
          gap: '10px',
          justifyContent: 'center'
        }}>
          <button
            onClick={() => onVote(currentVote === 'LIKE' ? 'NONE' : 'LIKE')}
            style={{
              padding: '12px 30px',
              fontSize: '18px',
              fontWeight: 'bold',
              backgroundColor: currentVote === 'LIKE' ? '#4caf50' : '#e0e0e0',
              color: currentVote === 'LIKE' ? '#fff' : '#666',
              border: currentVote === 'LIKE' ? '2px solid #388e3c' : '2px solid #bdbdbd',
              borderRadius: '8px',
              cursor: 'pointer',
              transition: 'all 0.2s',
            }}
            onMouseEnter={(e) => {
              if (currentVote !== 'LIKE') {
                e.currentTarget.style.backgroundColor = '#c8e6c9';
                e.currentTarget.style.borderColor = '#81c784';
              }
            }}
            onMouseLeave={(e) => {
              if (currentVote !== 'LIKE') {
                e.currentTarget.style.backgroundColor = '#e0e0e0';
                e.currentTarget.style.borderColor = '#bdbdbd';
              }
            }}
          >
            👍 추천 {currentVote === 'LIKE' && '✓'}
          </button>

          <button
            onClick={() => onVote(currentVote === 'DISLIKE' ? 'NONE' : 'DISLIKE')}
            style={{
              padding: '12px 30px',
              fontSize: '18px',
              fontWeight: 'bold',
              backgroundColor: currentVote === 'DISLIKE' ? '#f44336' : '#e0e0e0',
              color: currentVote === 'DISLIKE' ? '#fff' : '#666',
              border: currentVote === 'DISLIKE' ? '2px solid #d32f2f' : '2px solid #bdbdbd',
              borderRadius: '8px',
              cursor: 'pointer',
              transition: 'all 0.2s',
            }}
            onMouseEnter={(e) => {
              if (currentVote !== 'DISLIKE') {
                e.currentTarget.style.backgroundColor = '#ffcdd2';
                e.currentTarget.style.borderColor = '#e57373';
              }
            }}
            onMouseLeave={(e) => {
              if (currentVote !== 'DISLIKE') {
                e.currentTarget.style.backgroundColor = '#e0e0e0';
                e.currentTarget.style.borderColor = '#bdbdbd';
              }
            }}
          >
            👎 비추천 {currentVote === 'DISLIKE' && '✓'}
          </button>
        </div>
      )}
    </>
  );
}