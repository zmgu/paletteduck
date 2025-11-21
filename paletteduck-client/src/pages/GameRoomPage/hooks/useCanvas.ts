import { useRef, useEffect, useState, useCallback } from 'react';
import type { Tool, DrawingData } from '../../../types/drawing.types';
import { CANVAS_CONFIG } from '../../../constants/canvas.constants';

interface UseCanvasProps {
  isDrawer: boolean;
  onDrawing?: (data: Omit<DrawingData, 'playerId'>) => void;
}

export const useCanvas = ({ isDrawer, onDrawing }: UseCanvasProps) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [ctx, setCtx] = useState<CanvasRenderingContext2D | null>(null);
  const [isDrawing, setIsDrawing] = useState(false);
  const [currentPath, setCurrentPath] = useState<number[]>([]);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const context = canvas.getContext('2d');
    if (!context) return;

    context.fillStyle = CANVAS_CONFIG.BACKGROUND_COLOR;
    context.fillRect(0, 0, canvas.width, canvas.height);
    
    setCtx(context);
  }, []);

  const handleMouseDown = (e: React.MouseEvent, tool: Tool, color: string, width: number) => {
    if (!ctx || !isDrawer) return;

    const rect = canvasRef.current!.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    setIsDrawing(true);
    setCurrentPath([x, y]);

    const penColor = tool === 'pen' ? color : CANVAS_CONFIG.BACKGROUND_COLOR;
    
    ctx.strokeStyle = penColor;
    ctx.lineWidth = width;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
    ctx.beginPath();
    ctx.moveTo(x, y);

    if (onDrawing) {
      onDrawing({
        t: tool === 'pen' ? 0 : 1,
        c: color,
        w: width,
        p: [x, y],
        s: true,
      });
    }
  };

  const handleMouseMove = (e: React.MouseEvent, tool: Tool, color: string, width: number) => {
    if (!isDrawing || !ctx || !isDrawer) return;

    const rect = canvasRef.current!.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    const newPath = [...currentPath, x, y];
    setCurrentPath(newPath);

    ctx.lineTo(x, y);
    ctx.stroke();

    if (onDrawing && newPath.length >= 4) {
      onDrawing({
        t: tool === 'pen' ? 0 : 1,
        c: color,
        w: width,
        p: newPath,
        s: false,
      });
      setCurrentPath([x, y]);
    }
  };

  const handleMouseUp = (tool: Tool, color: string, width: number) => {
    if (!isDrawing) return;
    setIsDrawing(false);
    setCurrentPath([]);

    if (ctx) {
      ctx.closePath();
    }
  };

  // ✅ useCallback으로 메모이제이션
  const clearCanvas = useCallback(() => {
    if (!ctx || !canvasRef.current) {
      console.log('[useCanvas] clearCanvas blocked - ctx:', !!ctx, 'canvas:', !!canvasRef.current);
      return;
    }
    
    console.log('[useCanvas] Clearing canvas...');
    
    // 배경 채우기
    ctx.fillStyle = CANVAS_CONFIG.BACKGROUND_COLOR;
    ctx.fillRect(0, 0, canvasRef.current.width, canvasRef.current.height);
    
    // 경로 초기화
    ctx.beginPath();
    
    // 기본 설정 재적용
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
    
    console.log('[useCanvas] Canvas cleared and reset');
  }, [ctx]);  // ✅ ctx만 의존

  return {
    canvasRef,
    ctx,
    handleMouseDown,
    handleMouseMove,
    handleMouseUp,
    clearCanvas,
  };
};