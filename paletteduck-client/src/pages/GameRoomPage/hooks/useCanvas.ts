import { useRef, useEffect, useState, useCallback } from 'react';
import type { DrawingPoint, Tool } from '../../../types/drawing.types';
import { CANVAS_CONFIG } from '../../../constants/canvas.constants';

interface UseCanvasProps {
  isDrawer: boolean;
  onDrawing?: (data: any) => void;
}

export const useCanvas = ({ isDrawer, onDrawing }: UseCanvasProps) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [ctx, setCtx] = useState<CanvasRenderingContext2D | null>(null);
  const [isDrawing, setIsDrawing] = useState(false);
  
  const pointsBufferRef = useRef<DrawingPoint[]>([]);
  const lastSendTimeRef = useRef<number>(0);
  const isNewStrokeRef = useRef(false);

  // Canvas 초기화
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const context = canvas.getContext('2d', { willReadFrequently: true });
    if (!context) return;

    context.fillStyle = CANVAS_CONFIG.BACKGROUND_COLOR;
    context.fillRect(0, 0, CANVAS_CONFIG.WIDTH, CANVAS_CONFIG.HEIGHT);
    context.lineCap = 'round';
    context.lineJoin = 'round';
    
    setCtx(context);
  }, []);

  const getCanvasCoords = (e: React.MouseEvent<HTMLCanvasElement>): DrawingPoint => {
    const canvas = canvasRef.current;
    if (!canvas) return { x: 0, y: 0 };
    
    const rect = canvas.getBoundingClientRect();
    return {
      x: Math.round(e.clientX - rect.left),
      y: Math.round(e.clientY - rect.top),
    };
  };

  const sendPoints = (tool: Tool, color: string, width: number) => {
    if (!onDrawing || pointsBufferRef.current.length === 0) return;

    const flatPoints = pointsBufferRef.current.flatMap(pt => [pt.x, pt.y]);

    onDrawing({
      t: tool === 'pen' ? 0 : tool === 'eraser' ? 1 : 2,
      c: color,
      w: width,
      p: flatPoints,
      s: isNewStrokeRef.current,
    });

    isNewStrokeRef.current = false;
    pointsBufferRef.current = [];
  };

  // Flood fill 알고리즘
  const floodFill = (startX: number, startY: number, fillColor: string) => {
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

    // 스택 기반 flood fill (재귀 대신 스택 사용)
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

  const handleMouseDown = (e: React.MouseEvent<HTMLCanvasElement>, tool: Tool, color: string, width: number) => {
    if (!isDrawer || !ctx) return;

    // 팝오버 닫기 이벤트 발생
    window.dispatchEvent(new Event('canvas-drawing-start'));

    const { x, y } = getCanvasCoords(e);

    // 채우기 도구인 경우
    if (tool === 'fill') {
      floodFill(x, y, color);

      // 채우기 액션 전송
      if (onDrawing) {
        onDrawing({
          t: 2,
          c: color,
          w: 0,
          p: [x, y],
          s: true,
        });
      }
      return;
    }

    // 펜/지우개 도구인 경우
    setIsDrawing(true);

    ctx.strokeStyle = tool === 'pen' ? color : CANVAS_CONFIG.BACKGROUND_COLOR;
    ctx.lineWidth = width;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
    ctx.beginPath();
    ctx.moveTo(x, y);

    isNewStrokeRef.current = true;
    pointsBufferRef.current = [{ x, y }];
    lastSendTimeRef.current = Date.now();

    sendPoints(tool, color, width);
  };

  const handleMouseMove = (e: React.MouseEvent<HTMLCanvasElement>, tool: Tool, color: string, width: number) => {
    if (!isDrawer || !isDrawing || !ctx) return;
    
    const { x, y } = getCanvasCoords(e);

    ctx.lineTo(x, y);
    ctx.stroke();

    pointsBufferRef.current.push({ x, y });

    const now = Date.now();
    if (now - lastSendTimeRef.current >= CANVAS_CONFIG.THROTTLE_MS || 
        pointsBufferRef.current.length >= CANVAS_CONFIG.BATCH_SIZE) {
      sendPoints(tool, color, width);
      lastSendTimeRef.current = now;
    }
  };

  const handleMouseUp = (tool: Tool, color: string, width: number) => {
    if (!isDrawing) return;
    
    setIsDrawing(false);

    if (pointsBufferRef.current.length > 0) {
      sendPoints(tool, color, width);
    }

    isNewStrokeRef.current = false;
  };

  const clearCanvas = useCallback(() => {
    if (!ctx) return;

    // 캔버스 지우기
    ctx.fillStyle = CANVAS_CONFIG.BACKGROUND_COLOR;
    ctx.fillRect(0, 0, CANVAS_CONFIG.WIDTH, CANVAS_CONFIG.HEIGHT);

    // ctx 설정 완전히 초기화
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
    ctx.strokeStyle = '#000000';
    ctx.lineWidth = 4;

    // 상태 초기화
    setIsDrawing(false);
    pointsBufferRef.current = [];
    isNewStrokeRef.current = false;
  }, [ctx]);

  return {
    canvasRef,
    ctx,
    isDrawing,
    handleMouseDown,
    handleMouseMove,
    handleMouseUp,
    clearCanvas,
  };
};