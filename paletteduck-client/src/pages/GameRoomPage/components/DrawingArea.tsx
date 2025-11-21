import React from 'react';
import Canvas from './Canvas/Canvas';
import type { TurnInfo } from '../../../types/game.types';
import type { DrawingData } from '../../../types/drawing.types';

interface DrawingAreaProps {
  turnInfo: TurnInfo;
  isDrawer: boolean;
  drawingData?: DrawingData | null;
  clearSignal?: number;
  onDrawing?: (data: Omit<DrawingData, 'playerId'>) => void;
  onClearCanvas?: () => void;
}

function DrawingArea({
  turnInfo,
  isDrawer,
  drawingData,
  clearSignal,
  onDrawing,
  onClearCanvas,
}: DrawingAreaProps) {
  // ✅ 힌트 표시 로직
  const getDisplayWord = () => {
    if (isDrawer) {
      // 출제자: 전체 단어 표시
      return turnInfo.word;
    }
    
    // 참가자: 힌트에 따라 표시
    const word = turnInfo.word || '';
    const revealedHints = turnInfo.revealedHints || 0;
    
    let display = '';
    for (let i = 0; i < word.length; i++) {
      if (i < revealedHints) {
        display += word[i] + ' ';  // 공개된 글자
      } else {
        display += '_ ';  // 숨겨진 글자
      }
    }
    
    return display.trim();
  };

  const displayWord = getDisplayWord();

  return (
    <div>
      <div style={{
        marginBottom: '15px',
        padding: '15px',
        backgroundColor: '#f8f9fa',
        borderRadius: '8px',
        border: '2px solid #dee2e6',
      }}>
        <div style={{ fontSize: '14px', color: '#666', marginBottom: '5px' }}>
          {isDrawer ? '출제 단어' : '힌트'}
        </div>
        <div style={{
          fontSize: '28px',
          fontWeight: 'bold',
          letterSpacing: isDrawer ? '2px' : '8px',
          color: '#333',
          textAlign: 'center',
        }}>
          {displayWord}
        </div>
        {/* ✅ 힌트 공개 상황 표시 */}
        {!isDrawer && (
          <div style={{
            fontSize: '12px',
            color: '#666',
            textAlign: 'center',
            marginTop: '8px',
          }}>
            {turnInfo.revealedHints || 0} / {turnInfo.word?.length || 0} 글자 공개
          </div>
        )}
      </div>

      <Canvas
        key={`canvas-turn-${turnInfo.turnNumber}`}
        isDrawer={isDrawer}
        drawingData={drawingData}
        clearSignal={clearSignal}
        onDrawing={onDrawing}
        onClearRequest={onClearCanvas}
      />
    </div>
  );
}

export default React.memo(DrawingArea);