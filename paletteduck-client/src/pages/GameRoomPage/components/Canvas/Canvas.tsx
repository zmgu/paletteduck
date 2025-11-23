import { useEffect, useState, useRef } from 'react';
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
  
  // ✅ 이전 포인트 추적
  const lastPointRef = useRef<{ x: number; y: number } | null>(null);

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
    
    // 스타일 설정
    ctx.strokeStyle = penColor;
    ctx.lineWidth = w;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';

    // 새로운 경로 시작
    ctx.beginPath();

    if (s) {
      // ✅ 새로운 선 시작 (마우스 다운)
      ctx.moveTo(p[0], p[1]);
      lastPointRef.current = { x: p[0], y: p[1] };
    } else {
      // ✅ 이전 선에서 이어 그리기 (마우스 무브)
      if (lastPointRef.current) {
        ctx.moveTo(lastPointRef.current.x, lastPointRef.current.y);
      } else {
        // 이전 포인트가 없으면 첫 포인트에서 시작
        ctx.moveTo(p[0], p[1]);
      }
    }

    // 나머지 포인트 연결
    const startIndex = s ? 2 : 0;
    for (let i = startIndex; i < p.length; i += 2) {
      ctx.lineTo(p[i], p[i + 1]);
    }
    
    ctx.stroke();
    
    // ✅ 마지막 포인트 저장
    lastPointRef.current = { 
      x: p[p.length - 2], 
      y: p[p.length - 1] 
    };
  }, [drawingData, ctx, isDrawer]);

  // Clear 신호
  useEffect(() => {
    if (clearSignal && clearSignal > 0) {
      console.log('[Canvas] Received clear signal:', clearSignal);
      clearCanvas();
      lastPointRef.current = null;  // ✅ 이전 포인트 초기화
    }
  }, [clearSignal, clearCanvas]);

  const handleClear = () => {
    console.log('[Canvas] Clear button clicked');
    clearCanvas();
    lastPointRef.current = null;  // ✅ 이전 포인트 초기화
    
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