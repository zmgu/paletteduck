import { useEffect, useState, useRef, forwardRef, useImperativeHandle } from 'react';
import { useCanvas } from '../../hooks/useCanvas';
import CanvasToolbar from './CanvasToolbar';
import type { Tool, DrawingData } from '../../../../types/drawing.types';
import { CANVAS_CONFIG } from '../../../../constants/canvas.constants';

interface CanvasProps {
  isDrawer: boolean;
  onDrawing?: (data: Omit<DrawingData, 'playerId'>) => void;
  drawingData?: DrawingData | null;
  initialDrawingEvents?: DrawingData[];
  clearSignal?: number;
  onClearRequest?: () => void;
  turnNumber?: number;  // 턴 번호 변경 시 자동 초기화
  isSpectatorMidJoin?: boolean;  // 도중 참가 관전자 여부
  phase?: string;  // 게임 페이즈
  tool?: Tool;  // 외부에서 제어하는 도구
  color?: string;  // 외부에서 제어하는 색상
  width?: number;  // 외부에서 제어하는 굵기
  onToolChange?: (tool: Tool) => void;  // 도구 변경 콜백
  onColorChange?: (color: string) => void;  // 색상 변경 콜백
  onWidthChange?: (width: number) => void;  // 굵기 변경 콜백
  hideToolbar?: boolean;  // 툴바를 숨길지 여부
}

export interface CanvasHandle {
  captureImage: () => string;
}

const Canvas = forwardRef<CanvasHandle, CanvasProps>(({
  isDrawer,
  onDrawing,
  drawingData,
  initialDrawingEvents,
  clearSignal,
  onClearRequest,
  turnNumber,
  isSpectatorMidJoin,
  phase,
  tool: externalTool,
  color: externalColor,
  width: externalWidth,
  onToolChange,
  onColorChange,
  onWidthChange,
  hideToolbar
}: CanvasProps, ref) => {
  const [internalTool, setInternalTool] = useState<Tool>('pen');
  const [internalColor, setInternalColor] = useState('#000000');
  const [internalWidth, setInternalWidth] = useState(8);

  // 외부 prop이 제공되면 그것을 사용, 아니면 내부 상태 사용
  const tool = externalTool !== undefined ? externalTool : internalTool;
  const color = externalColor !== undefined ? externalColor : internalColor;
  const width = externalWidth !== undefined ? externalWidth : internalWidth;

  const setTool = (newTool: Tool) => {
    if (onToolChange) {
      onToolChange(newTool);
    } else {
      setInternalTool(newTool);
    }
  };

  const setColor = (newColor: string) => {
    if (onColorChange) {
      onColorChange(newColor);
    } else {
      setInternalColor(newColor);
    }
  };

  const setWidth = (newWidth: number) => {
    if (onWidthChange) {
      onWidthChange(newWidth);
    } else {
      setInternalWidth(newWidth);
    }
  };

  const lastPointRef = useRef<{ x: number; y: number } | null>(null);
  const lastProcessedRef = useRef<string | null>(null);

  const {
    canvasRef,
    ctx,
    handleMouseDown,
    handleMouseMove,
    handleMouseUp,
    clearCanvas,
  } = useCanvas({ isDrawer, onDrawing });

  // Flood fill 함수 (다른 플레이어의 채우기 이벤트 처리용)
  const performFloodFill = (startX: number, startY: number, fillColor: string) => {
    if (!ctx || !canvasRef.current) return;

    const imageData = ctx.getImageData(0, 0, CANVAS_CONFIG.WIDTH, CANVAS_CONFIG.HEIGHT);
    const pixels = imageData.data;

    // RGB 문자열을 [r, g, b] 배열로 변환
    const hexToRgb = (hex: string): [number, number, number] => {
      const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
      return result ? [
        parseInt(result[1], 16),
        parseInt(result[2], 16),
        parseInt(result[3], 16)
      ] : [0, 0, 0];
    };

    const fillRgb = hexToRgb(fillColor);

    // 시작점의 색상 가져오기
    const startIndex = (startY * CANVAS_CONFIG.WIDTH + startX) * 4;
    const targetR = pixels[startIndex];
    const targetG = pixels[startIndex + 1];
    const targetB = pixels[startIndex + 2];

    // 이미 같은 색이면 리턴
    if (targetR === fillRgb[0] && targetG === fillRgb[1] && targetB === fillRgb[2]) {
      return;
    }

    // 스택 기반 flood fill
    const stack: [number, number][] = [[startX, startY]];
    const visited = new Set<number>();

    while (stack.length > 0) {
      const [x, y] = stack.pop()!;

      if (x < 0 || x >= CANVAS_CONFIG.WIDTH || y < 0 || y >= CANVAS_CONFIG.HEIGHT) continue;

      const index = (y * CANVAS_CONFIG.WIDTH + x) * 4;

      if (visited.has(index)) continue;
      visited.add(index);

      const r = pixels[index];
      const g = pixels[index + 1];
      const b = pixels[index + 2];

      // 타겟 색상과 다르면 스킵
      if (r !== targetR || g !== targetG || b !== targetB) continue;

      // 색상 채우기
      pixels[index] = fillRgb[0];
      pixels[index + 1] = fillRgb[1];
      pixels[index + 2] = fillRgb[2];
      pixels[index + 3] = 255;

      // 인접 픽셀 추가
      stack.push([x + 1, y]);
      stack.push([x - 1, y]);
      stack.push([x, y + 1]);
      stack.push([x, y - 1]);
    }

    ctx.putImageData(imageData, 0, 0);
  };

  // 다른 사람의 그림 수신 (실시간)
  useEffect(() => {
    if (!drawingData || !ctx || isDrawer || isSpectatorMidJoin) return;

    const { t, c, w, p, s } = drawingData;

    if (!p || p.length < 2) return;

    // 중복 실행 방지: 데이터 고유 키 생성 (points + isStart)
    const dataKey = `${s ? 'S' : 'C'}_${p.join(',')}_${t}_${c}_${w}`;
    if (lastProcessedRef.current === dataKey) return;
    lastProcessedRef.current = dataKey;

    // 채우기 도구인 경우
    if (t === 2) {
      const [x, y] = p;
      performFloodFill(x, y, c);
      return;
    }

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
  }, [drawingData, ctx, isDrawer, turnNumber]);

  // 턴 번호 변경 시 캔버스 자동 초기화
  useEffect(() => {
    if (turnNumber !== undefined) {
      clearCanvas();
      lastPointRef.current = null;
      lastProcessedRef.current = null;
    }
  }, [turnNumber, clearCanvas]);

  // 초기 그림 이벤트 적용 (도중 참가자를 위해)
  useEffect(() => {
    if (!initialDrawingEvents || initialDrawingEvents.length === 0 || !ctx || isDrawer || isSpectatorMidJoin) {
      return;
    }

    let localLastPoint: { x: number; y: number } | null = null;

    initialDrawingEvents.forEach((event) => {
      const { t, c, w, p, s } = event;

      if (!p || p.length < 2) return;

      const penColor = t === 0 ? c : CANVAS_CONFIG.BACKGROUND_COLOR;

      ctx.strokeStyle = penColor;
      ctx.lineWidth = w;
      ctx.lineCap = 'round';
      ctx.lineJoin = 'round';

      ctx.beginPath();

      let startIndex = 0;

      if (s) {
        // 새로운 선 시작
        ctx.moveTo(p[0], p[1]);
        localLastPoint = { x: p[0], y: p[1] };
        startIndex = 2;
      } else {
        // 이전 선에서 이어 그리기
        if (localLastPoint) {
          ctx.moveTo(localLastPoint.x, localLastPoint.y);
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

      // 마지막 포인트 저장
      localLastPoint = {
        x: p[p.length - 2],
        y: p[p.length - 1]
      };
    });

    // 전역 lastPointRef 업데이트
    if (localLastPoint) {
      lastPointRef.current = localLastPoint;
    }
  }, [initialDrawingEvents, ctx, isDrawer, turnNumber]);

  // Clear 신호
  useEffect(() => {
    if (clearSignal && clearSignal > 0) {
      clearCanvas();
      lastPointRef.current = null;
      lastProcessedRef.current = null;
    }
  }, [clearSignal, clearCanvas]);

  const handleClear = () => {
    clearCanvas();
    lastPointRef.current = null;

    if (isDrawer && onClearRequest) {
      onClearRequest();
    }
  };

  useImperativeHandle(ref, () => ({
    captureImage: () => {
      if (!canvasRef.current) return '';
      return canvasRef.current.toDataURL('image/png');
    }
  }));

  // 커서 스타일 결정
  const getCursorStyle = () => {
    // 드로잉 턴이 아니거나 출제자가 아니면 기본 커서
    if (!isDrawer || phase !== 'DRAWING') return 'default';

    if (tool === 'pen') {
      // 펜 모양 커서 (SVG data URI)
      const penCursor = `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='32' height='32' viewBox='0 0 24 24'%3E%3Cpath fill='%23444' d='M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z'/%3E%3C/svg%3E") 0 32, auto`;
      return penCursor;
    } else if (tool === 'eraser') {
      // 지우개 모양 커서 (SVG data URI)
      const eraserCursor = `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='32' height='32' viewBox='0 0 24 24'%3E%3Cpath fill='%23444' d='M16.24 3.56l4.95 4.94c.78.79.78 2.05 0 2.84L12 20.53a4.008 4.008 0 0 1-5.66 0L2.81 17c-.78-.79-.78-2.05 0-2.84l10.6-10.6c.79-.78 2.05-.78 2.83 0M4.22 15.58l3.54 3.53c.78.79 2.04.79 2.83 0l3.53-3.53l-4.95-4.95l-4.95 4.95Z'/%3E%3C/svg%3E") 16 16, auto`;
      return eraserCursor;
    } else {
      // 채우기 도구 커서 (SVG data URI - 물감통 아이콘)
      const fillCursor = `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='32' height='32' viewBox='0 0 24 24'%3E%3Cpath fill='%23444' d='M18 14c0-4-6-10.8-6-10.8s-1.33 1.51-2.73 3.52l8.59 8.59c.09-.42.14-.86.14-1.31zm-5 8h-1l-4.1-4.11C7.39 18.48 7 19.22 7 20a2 2 0 0 0 2 2c.84 0 1.55-.52 1.84-1.25L13 22m8.5-9.87L21 12l-6.18-6.18c-1.93 2.52-4.82 7.01-4.82 9.18c0 1.76.85 3.31 2.16 4.29L21.5 12.13Z'/%3E%3C/svg%3E") 0 32, auto`;
      return fillCursor;
    }
  };

  return (
    <div style={{ border: 'none', overflow: 'hidden', position: 'relative' }}>
      <canvas
        ref={canvasRef}
        width={CANVAS_CONFIG.WIDTH}
        height={CANVAS_CONFIG.HEIGHT}
        style={{
          display: 'block',
          cursor: getCursorStyle(),
          touchAction: 'none',
        }}
        onMouseDown={isDrawer ? (e) => handleMouseDown(e, tool, color, width) : undefined}
        onMouseMove={isDrawer ? (e) => handleMouseMove(e, tool, color, width) : undefined}
        onMouseUp={isDrawer ? () => handleMouseUp(tool, color, width) : undefined}
        onMouseLeave={isDrawer ? () => handleMouseUp(tool, color, width) : undefined}
      />

      {isDrawer && phase === 'DRAWING' && !hideToolbar && (
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

      {/* 도중 참가 관전자 안내 오버레이 */}
      {isSpectatorMidJoin && (
        <div style={{
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          gap: '20px',
          padding: '40px',
          textAlign: 'center',
          pointerEvents: 'none',
        }}>
          <div style={{
            fontSize: '48px',
          }}>
            👀
          </div>
          <div style={{
            fontSize: '20px',
            fontWeight: 'bold',
            color: '#ff9800',
            lineHeight: '1.6',
          }}>
            입장한 턴의 그림은 보이지 않습니다
          </div>
          <div style={{
            fontSize: '16px',
            color: '#666',
            lineHeight: '1.5',
          }}>
            다음 턴부터 그림을 볼 수 있습니다
          </div>
        </div>
      )}
    </div>
  );
});

Canvas.displayName = 'Canvas';

export default Canvas;