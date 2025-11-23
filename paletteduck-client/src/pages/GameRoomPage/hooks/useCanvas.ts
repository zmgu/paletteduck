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
      t: tool === 'pen' ? 0 : 1,
      c: color,
      w: width,
      p: flatPoints,
      s: isNewStrokeRef.current,
    });

    isNewStrokeRef.current = false;
    pointsBufferRef.current = [];
  };

  const handleMouseDown = (e: React.MouseEvent<HTMLCanvasElement>, tool: Tool, color: string, width: number) => {
    if (!isDrawer || !ctx) return;
    
    setIsDrawing(true);
    const { x, y } = getCanvasCoords(e);

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