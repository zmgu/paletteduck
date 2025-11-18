import React from 'react';
import Canvas from './Canvas';
import type { TurnInfo, DrawData } from '../../../types/game.types';

interface DrawingAreaProps {
  turnInfo: TurnInfo;
  isDrawer: boolean;
  drawData: DrawData | null;
  clearSignal: number;
  onDrawComplete?: (data: DrawData) => void;
  onClearCanvas?: () => void;
}

export default function DrawingArea({
  turnInfo,
  isDrawer,
  drawData,
  clearSignal,
  onDrawComplete,
  onClearCanvas,
}: DrawingAreaProps) {
  return (
    <>
      {/* 단어 표시 */}
      <div style={{ 
        marginTop: '20px', 
        padding: '30px', 
        border: '2px solid #4caf50',
        borderRadius: '12px',
        backgroundColor: '#e8f5e9',
        textAlign: 'center'
      }}>
        {isDrawer ? (
          <p style={{ 
            fontSize: '32px', 
            fontWeight: 'bold', 
            margin: 0,
            color: '#2e7d32'
          }}>
            단어: {turnInfo.word}
          </p>
        ) : (
          <p style={{ 
            fontSize: '28px', 
            fontWeight: 'bold',
            margin: 0,
            letterSpacing: '8px',
            color: '#666'
          }}>
            {turnInfo.word?.replace(/./g, '_')}
          </p>
        )}
      </div>

      {/* Canvas */}
      <div style={{ marginTop: '20px', display: 'flex', justifyContent: 'center' }}>
        <Canvas
          isDrawer={isDrawer}
          onPathComplete={onDrawComplete}
          drawData={drawData}
          clearSignal={clearSignal}
          onClearRequest={onClearCanvas}
        />
      </div>
    </>
  );
}