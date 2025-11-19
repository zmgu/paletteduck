import React from 'react';
import Canvas from './Canvas';
import type { TurnInfo } from '../../../types/game.types';
import type { DrawingData } from '../../../types/drawing.types';

interface DrawingAreaProps {
  turnInfo: TurnInfo;
  isDrawer: boolean;
  drawingData: DrawingData | null;
  clearSignal: number;
  onDrawing?: (data: Omit<DrawingData, 'playerId'>) => void;
  onClearCanvas?: () => void;
}

export default function DrawingArea({
  turnInfo,
  isDrawer,
  drawingData,
  clearSignal,
  onDrawing,
  onClearCanvas,
}: DrawingAreaProps) {
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

      <div style={{ marginTop: '20px', display: 'flex', justifyContent: 'center' }}>
        <Canvas
          isDrawer={isDrawer}
          onDrawing={onDrawing}
          drawingData={drawingData}
          clearSignal={clearSignal}
          onClearRequest={onClearCanvas}
        />
      </div>
    </>
  );
}