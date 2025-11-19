import React, { useEffect, useState } from 'react';
import { useCanvas } from '../../hooks/useCanvas';
import CanvasToolbar from './CanvasToolbar';
import type { Tool, DrawingData } from '../../../../types/drawing.types';
import { CANVAS_CONFIG } from '../../../../constants/canvas.constants';

interface CanvasProps {
  isDrawer: boolean;
  onDrawing?: (data: Omit<DrawingData, 'playerId'>) => void;
  drawingData?: DrawingData | null;
  clearSignal?: number;
  onClearRequest?: () => void;
}

export default function Canvas({ 
  isDrawer, 
  onDrawing,
  drawingData,
  clearSignal, 
  onClearRequest 
}: CanvasProps) {
  const [tool, setTool] = useState<Tool>('pen');
  const [color, setColor] = useState('#000000');
  const [width, setWidth] = useState(4);

  const {
    canvasRef,
    ctx,
    handleMouseDown,
    handleMouseMove,
    handleMouseUp,
    clearCanvas,
  } = useCanvas({ isDrawer, onDrawing });

  // 다른 사람의 그림 수신 (실시간)
  useEffect(() => {
    if (!drawingData || !ctx || isDrawer) return;

    const { t, c, w, p, s } = drawingData;
    
    if (!p || p.length < 2) return;

    const penColor = t === 0 ? c : CANVAS_CONFIG.BACKGROUND_COLOR;
    
    ctx.strokeStyle = penColor;
    ctx.lineWidth = w;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';

    if (s) {
      ctx.beginPath();
      ctx.moveTo(p[0], p[1]);
    }

    for (let i = s ? 2 : 0; i < p.length; i += 2) {
      ctx.lineTo(p[i], p[i + 1]);
    }
    ctx.stroke();
  }, [drawingData, ctx, isDrawer]);

  // Clear 신호
  useEffect(() => {
    if (clearSignal && clearSignal > 0) {
      console.log('[Canvas] Received clear signal:', clearSignal);
      clearCanvas();
    }
  }, [clearSignal, clearCanvas]);  // clearCanvas는 useCallback으로 안정화됨

  const handleClear = () => {
    console.log('[Canvas] Clear button clicked');
    clearCanvas();
    
    if (isDrawer && onClearRequest) {
      onClearRequest();
    }
  };

  return (
    <div style={{ border: '2px solid #ccc', borderRadius: '8px', overflow: 'hidden' }}>
      {isDrawer && (
        <CanvasToolbar
          tool={tool}
          color={color}
          width={width}
          onToolChange={setTool}
          onColorChange={setColor}
          onWidthChange={setWidth}
          onClear={handleClear}
        />
      )}

      <canvas
        ref={canvasRef}
        width={CANVAS_CONFIG.WIDTH}
        height={CANVAS_CONFIG.HEIGHT}
        style={{
          display: 'block',
          cursor: isDrawer ? 'crosshair' : 'default',
          touchAction: 'none',
        }}
        onMouseDown={isDrawer ? (e) => handleMouseDown(e, tool, color, width) : undefined}
        onMouseMove={isDrawer ? (e) => handleMouseMove(e, tool, color, width) : undefined}
        onMouseUp={isDrawer ? () => handleMouseUp(tool, color, width) : undefined}
        onMouseLeave={isDrawer ? () => handleMouseUp(tool, color, width) : undefined}
      />
    </div>
  );
}