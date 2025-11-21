import React, { useEffect, useState, useRef } from 'react';
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
  
  const lastPointRef = useRef<{ x: number; y: number } | null>(null);
  const lastClearSignalRef = useRef<number>(0);  // ✅ 추가: 마지막 처리한 clearSignal 추적

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

    ctx.beginPath();

    if (s) {
      ctx.moveTo(p[0], p[1]);
      lastPointRef.current = { x: p[0], y: p[1] };
    } else {
      if (lastPointRef.current) {
        ctx.moveTo(lastPointRef.current.x, lastPointRef.current.y);
      } else {
        ctx.moveTo(p[0], p[1]);
      }
    }

    const startIndex = s ? 2 : 0;
    for (let i = startIndex; i < p.length; i += 2) {
      ctx.lineTo(p[i], p[i + 1]);
    }
    
    ctx.stroke();
    
    lastPointRef.current = { 
      x: p[p.length - 2], 
      y: p[p.length - 1] 
    };
  }, [drawingData, ctx, isDrawer]);

  // ✅ Clear 신호 - 중복 처리 방지
  useEffect(() => {
    console.log('[Canvas] clearSignal changed:', clearSignal, 'last processed:', lastClearSignalRef.current);
    
    // clearSignal이 증가했을 때만 처리
    if (clearSignal && clearSignal > 0 && clearSignal > lastClearSignalRef.current) {
      console.log('[Canvas] Processing clear signal:', clearSignal);
      
      clearCanvas();
      lastPointRef.current = null;
      
      // ✅ 처리한 신호 기록
      lastClearSignalRef.current = clearSignal;
      
      console.log('[Canvas] Clear completed for signal:', clearSignal);
    }
  }, [clearSignal, clearCanvas]);

  const handleClear = () => {
    console.log('[Canvas] Clear button clicked');
    clearCanvas();
    lastPointRef.current = null;
    
    if (isDrawer && onClearRequest) {
      console.log('[Canvas] Sending clear request to server');
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