import { useEffect, useState, useRef, forwardRef, useImperativeHandle } from 'react';
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
  turnNumber?: number;  // 턴 번호 변경 시 자동 초기화
}

export interface CanvasHandle {
  captureImage: () => string;
}

const Canvas = forwardRef<CanvasHandle, CanvasProps>(({
  isDrawer,
  onDrawing,
  drawingData,
  clearSignal,
  onClearRequest,
  turnNumber
}: CanvasProps, ref) => {
  const [tool, setTool] = useState<Tool>('pen');
  const [color, setColor] = useState('#000000');
  const [width, setWidth] = useState(4);
  
  // ✅ 이전 포인트 추적
  const lastPointRef = useRef<{ x: number; y: number } | null>(null);
  // ✅ 중복 실행 방지를 위한 마지막 처리 데이터 추적
  const lastProcessedRef = useRef<string | null>(null);

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

    // 중복 실행 방지: 데이터 고유 키 생성 (points + isStart)
    const dataKey = `${s ? 'S' : 'C'}_${p.join(',')}_${t}_${c}_${w}`;
    if (lastProcessedRef.current === dataKey) return;
    lastProcessedRef.current = dataKey;

    const penColor = t === 0 ? c : CANVAS_CONFIG.BACKGROUND_COLOR;

    // 스타일 설정
    ctx.strokeStyle = penColor;
    ctx.lineWidth = w;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';

    // 새로운 경로 시작
    ctx.beginPath();

    // 시작 인덱스 결정
    let startIndex = 0;

    if (s) {
      // 새로운 선 시작 (마우스 다운)
      ctx.moveTo(p[0], p[1]);
      lastPointRef.current = { x: p[0], y: p[1] };
      startIndex = 2;
    } else {
      // 이전 선에서 이어 그리기 (마우스 무브)
      if (lastPointRef.current) {
        ctx.moveTo(lastPointRef.current.x, lastPointRef.current.y);
        startIndex = 0;
      } else {
        ctx.moveTo(p[0], p[1]);
        startIndex = 2;
      }
    }

    // 포인트 연결
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
      clearCanvas();
      lastPointRef.current = null;
      lastProcessedRef.current = null;
    }
  }, [clearSignal, clearCanvas]);

  // 턴 번호 변경 시 캔버스 자동 초기화
  useEffect(() => {
    if (turnNumber !== undefined) {
      clearCanvas();
      lastPointRef.current = null;
      lastProcessedRef.current = null;
    }
  }, [turnNumber, clearCanvas]);

  const handleClear = () => {
    clearCanvas();
    lastPointRef.current = null;

    if (isDrawer && onClearRequest) {
      onClearRequest();
    }
  };

  // 외부에서 캔버스 이미지를 캡처할 수 있도록 ref 노출
  useImperativeHandle(ref, () => ({
    captureImage: () => {
      if (!canvasRef.current) return '';
      return canvasRef.current.toDataURL('image/png');
    }
  }));

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
});

Canvas.displayName = 'Canvas';

export default Canvas;