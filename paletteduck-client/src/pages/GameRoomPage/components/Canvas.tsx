import React, { useRef, useEffect, useState } from 'react';
import { ReactSketchCanvas, type ReactSketchCanvasRef } from 'react-sketch-canvas';
import type { DrawData } from '../../../types/game.types';

interface CanvasProps {
  isDrawer: boolean;
  onPathComplete?: (data: DrawData) => void;
  drawData?: DrawData | null;
  clearSignal?: number;
  onClearRequest?: () => void;
}

const COLORS = [
  '#000000', '#FFFFFF', '#FF0000', '#00FF00', '#0000FF',
  '#FFFF00', '#FF00FF', '#00FFFF', '#FFA500', '#800080'
];

export default function Canvas({ isDrawer, onPathComplete, drawData, clearSignal, onClearRequest }: CanvasProps) {
  const canvasRef = useRef<ReactSketchCanvasRef>(null);
  const [tool, setTool] = useState<'pen' | 'eraser'>('pen');
  const [color, setColor] = useState('#000000');
  const [width, setWidth] = useState(2);
  const [allPaths, setAllPaths] = useState<any[]>([]);  // 모든 경로 저장

  // 다른 사람의 그림 수신
  useEffect(() => {
    if (!drawData || isDrawer) return;

    const newPath = {
      drawMode: drawData.tool === 'pen',
      paths: drawData.points.map(p => ({ x: p.x, y: p.y })),
      strokeColor: drawData.color,
      strokeWidth: drawData.width,
    };

    setAllPaths(prev => [...prev, newPath]);
  }, [drawData, isDrawer]);

  // 경로 변경 시 캔버스 업데이트
    useEffect(() => {
    if (!isDrawer) {
        if (allPaths.length > 0) {
        canvasRef.current?.loadPaths(allPaths);
        } else {
        canvasRef.current?.clearCanvas();  // 빈 배열일 때 초기화
        }
    }
    }, [allPaths, isDrawer]);

  // Clear 신호 수신
    useEffect(() => {
    if (clearSignal && clearSignal > 0) {
        canvasRef.current?.clearCanvas();
        setAllPaths([]);  // 경로 히스토리 초기화
        
        // 참가자 화면도 명시적으로 초기화
        if (!isDrawer) {
        setTimeout(() => {
            canvasRef.current?.clearCanvas();
        }, 0);
        }
    }
    }, [clearSignal, isDrawer]);

  const handleStrokeEnd = async () => {
    if (!isDrawer || !onPathComplete) return;

    const paths = await canvasRef.current?.exportPaths();
    if (!paths || paths.length === 0) return;

    const lastPath = paths[paths.length - 1];
    const points = lastPath.paths.map((p: any) => ({ x: p.x, y: p.y }));

    onPathComplete({
      playerId: '',
      tool: lastPath.drawMode ? 'pen' : 'eraser',
      color: lastPath.strokeColor,
      width: lastPath.strokeWidth,
      points,
    });
  };

  const handleClear = () => {
    canvasRef.current?.clearCanvas();
    if (isDrawer && onClearRequest) {
      onClearRequest();
    }
  };

  return (
    <div style={{ border: '2px solid #ccc', borderRadius: '8px', overflow: 'hidden' }}>
      {/* 도구 모음 */}
      {isDrawer && (
        <div style={{ 
          padding: '10px', 
          backgroundColor: '#f5f5f5', 
          display: 'flex', 
          gap: '10px',
          alignItems: 'center',
          flexWrap: 'wrap'
        }}>
          {/* 펜/지우개 */}
          <div style={{ display: 'flex', gap: '5px' }}>
            <button
              onClick={() => setTool('pen')}
              style={{
                padding: '8px 16px',
                backgroundColor: tool === 'pen' ? '#2196f3' : '#fff',
                color: tool === 'pen' ? '#fff' : '#000',
                border: '1px solid #ccc',
                borderRadius: '4px',
                cursor: 'pointer',
              }}
            >
              🖊️ 펜
            </button>
            <button
              onClick={() => setTool('eraser')}
              style={{
                padding: '8px 16px',
                backgroundColor: tool === 'eraser' ? '#2196f3' : '#fff',
                color: tool === 'eraser' ? '#fff' : '#000',
                border: '1px solid #ccc',
                borderRadius: '4px',
                cursor: 'pointer',
              }}
            >
              🧹 지우개
            </button>
          </div>

          {/* 굵기 */}
          <div style={{ display: 'flex', gap: '5px', alignItems: 'center' }}>
            <span>굵기:</span>
            {[1, 2, 3].map(w => (
              <button
                key={w}
                onClick={() => setWidth(w * 2)}
                style={{
                  width: '30px',
                  height: '30px',
                  backgroundColor: width === w * 2 ? '#2196f3' : '#fff',
                  color: width === w * 2 ? '#fff' : '#000',
                  border: '1px solid #ccc',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontSize: '12px',
                }}
              >
                {w}
              </button>
            ))}
          </div>

          {/* 색상 */}
          <div style={{ display: 'flex', gap: '5px' }}>
            {COLORS.map(c => (
              <button
                key={c}
                onClick={() => setColor(c)}
                style={{
                  width: '30px',
                  height: '30px',
                  backgroundColor: c,
                  border: color === c ? '3px solid #2196f3' : '1px solid #ccc',
                  borderRadius: '4px',
                  cursor: 'pointer',
                }}
              />
            ))}
          </div>

          {/* Clear */}
          <div style={{ display: 'flex', gap: '5px', marginLeft: 'auto' }}>
            <button
              onClick={handleClear}
              style={{
                padding: '8px 16px',
                backgroundColor: '#f44336',
                color: '#fff',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
              }}
            >
              🗑️ 전체삭제
            </button>
          </div>
        </div>
      )}

      {/* 캔버스 */}
      <div style={{ position: 'relative' }}>
        <ReactSketchCanvas
          ref={canvasRef}
          width="800px"
          height="600px"
          strokeWidth={width}
          strokeColor={tool === 'pen' ? color : '#FFFFFF'}
          canvasColor="#FFFFFF"
          style={{ border: 'none' }}
          onStroke={isDrawer ? handleStrokeEnd : undefined}  // 출제자만 onStroke
          allowOnlyPointerType="all"
          withTimestamp={false}
        />
        {/* 참가자용 오버레이 - 마우스 이벤트 차단 */}
        {!isDrawer && (
          <div style={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            pointerEvents: 'all',  // 모든 마우스 이벤트 차단
            cursor: 'default',
          }} />
        )}
      </div>
    </div>
  );
}