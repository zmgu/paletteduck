
import { useState, useRef, useEffect } from 'react';
import type { Tool } from '../../../../types/drawing.types';
import { COLORS } from '../../../../constants/canvas.constants';

interface CanvasToolbarProps {
  tool: Tool;
  color: string;
  width: number;
  onToolChange: (tool: Tool) => void;
  onColorChange: (color: string) => void;
  onWidthChange: (width: number) => void;
  onClear: () => void;
  onPopoverOpen?: () => void;
}

export default function CanvasToolbar({
  tool,
  color,
  width,
  onToolChange,
  onColorChange,
  onWidthChange,
  onClear,
  onPopoverOpen,
}: CanvasToolbarProps) {
  const [activePopover, setActivePopover] = useState<'pen' | 'eraser' | null>(null);
  const penButtonRef = useRef<HTMLButtonElement>(null);
  const eraserButtonRef = useRef<HTMLButtonElement>(null);
  const popoverRef = useRef<HTMLDivElement>(null);

  // 외부 클릭 시 팝오버 닫기
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        popoverRef.current &&
        !popoverRef.current.contains(event.target as Node) &&
        !penButtonRef.current?.contains(event.target as Node) &&
        !eraserButtonRef.current?.contains(event.target as Node)
      ) {
        setActivePopover(null);
      }
    };

    if (activePopover) {
      document.addEventListener('mousedown', handleClickOutside);
      return () => document.removeEventListener('mousedown', handleClickOutside);
    }
  }, [activePopover]);

  const handlePenClick = () => {
    if (tool !== 'pen') {
      onToolChange('pen');
    }
    setActivePopover(activePopover === 'pen' ? null : 'pen');
    onPopoverOpen?.();
  };

  const handleEraserClick = () => {
    if (tool !== 'eraser') {
      onToolChange('eraser');
    }
    setActivePopover(activePopover === 'eraser' ? null : 'eraser');
    onPopoverOpen?.();
  };

  const handleWidthChange = (newWidth: number) => {
    onWidthChange(newWidth);
  };

  // 팝오버 닫기 함수 (외부에서 호출 가능)
  useEffect(() => {
    // Canvas에서 그리기 시작하면 팝오버를 닫도록 전역 이벤트 리스너 추가
    const handleDrawingStart = () => {
      setActivePopover(null);
    };

    window.addEventListener('canvas-drawing-start', handleDrawingStart);
    return () => window.removeEventListener('canvas-drawing-start', handleDrawingStart);
  }, []);

  return (
    <div style={{
      padding: '2px',
      backgroundColor: 'transparent',
      display: 'grid',
      gridTemplateColumns: '1fr auto 1fr',
      alignItems: 'center',
      position: 'relative',
      width: '100%'
    }}>
      {/* 왼쪽: 도구 선택 */}
      <div style={{ display: 'flex', gap: '4px', position: 'relative', justifyContent: 'flex-start' }}>
        <button
          ref={penButtonRef}
          onClick={handlePenClick}
          style={{
            padding: '10px 12px',
            fontSize: '14px',
            backgroundColor: tool === 'pen' ? '#2196f3' : '#fff',
            color: tool === 'pen' ? '#fff' : '#000',
            border: '1px solid #ccc',
            borderRadius: '4px',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <span className="material-symbols-outlined" style={{
            fontSize: '24px',
            fontVariationSettings: '"FILL" 1, "wght" 700, "GRAD" 0, "opsz" 48'
          }}>stylus</span>
        </button>

        {/* 펜 굵기 팝오버 */}
        {activePopover === 'pen' && (
          <div
            ref={popoverRef}
            style={{
              position: 'absolute',
              bottom: '100%',
              left: '0',
              marginBottom: '8px',
              backgroundColor: '#fff',
              border: '1px solid #ccc',
              borderRadius: '8px',
              padding: '12px',
              boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
              zIndex: 1000,
              display: 'flex',
              flexDirection: 'column',
              gap: '8px',
              minWidth: '100px'
            }}
          >
            <input
              type="range"
              min="2"
              max="20"
              value={width}
              onChange={(e) => handleWidthChange(Number(e.target.value))}
              style={{
                width: '100%',
                height: '16px',
                cursor: 'pointer',
                accentColor: '#2196f3'
              }}
            />
            {/* 말풍선 꼬리 */}
            <div style={{
              position: 'absolute',
              bottom: '-6px',
              left: '20px',
              width: '12px',
              height: '12px',
              backgroundColor: '#fff',
              border: '1px solid #ccc',
              borderTop: 'none',
              borderLeft: 'none',
              transform: 'rotate(45deg)',
            }} />
          </div>
        )}

        <button
          ref={eraserButtonRef}
          onClick={handleEraserClick}
          style={{
            padding: '10px 12px',
            fontSize: '14px',
            backgroundColor: tool === 'eraser' ? '#2196f3' : '#fff',
            color: tool === 'eraser' ? '#fff' : '#000',
            border: '1px solid #ccc',
            borderRadius: '4px',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <span className="material-symbols-outlined" style={{
            fontSize: '24px',
            fontVariationSettings: '"FILL" 1, "wght" 700, "GRAD" 0, "opsz" 48'
          }}>highlighter_size_5</span>
        </button>

        <button
          onClick={() => onToolChange('fill')}
          style={{
            padding: '10px 12px',
            fontSize: '14px',
            backgroundColor: tool === 'fill' ? '#2196f3' : '#fff',
            color: tool === 'fill' ? '#fff' : '#000',
            border: '1px solid #ccc',
            borderRadius: '4px',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <span className="material-symbols-outlined" style={{
            fontSize: '24px',
            fontVariationSettings: '"FILL" 1, "wght" 700, "GRAD" 0, "opsz" 48'
          }}>colors</span>
        </button>

        {/* 지우개 굵기 팝오버 */}
        {activePopover === 'eraser' && (
          <div
            ref={popoverRef}
            style={{
              position: 'absolute',
              bottom: '100%',
              left: '50px',
              marginBottom: '8px',
              backgroundColor: '#fff',
              border: '1px solid #ccc',
              borderRadius: '8px',
              padding: '12px',
              boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
              zIndex: 1000,
              display: 'flex',
              flexDirection: 'column',
              gap: '8px',
              minWidth: '100px'
            }}
          >
            <input
              type="range"
              min="2"
              max="30"
              value={width}
              onChange={(e) => handleWidthChange(Number(e.target.value))}
              style={{
                width: '100%',
                height: '16px',
                cursor: 'pointer',
                accentColor: '#2196f3'
              }}
            />
            {/* 말풍선 꼬리 */}
            <div style={{
              position: 'absolute',
              bottom: '-6px',
              left: '20px',
              width: '12px',
              height: '12px',
              backgroundColor: '#fff',
              border: '1px solid #ccc',
              borderTop: 'none',
              borderLeft: 'none',
              transform: 'rotate(45deg)',
            }} />
          </div>
        )}
      </div>

      {/* 가운데: 색상 선택 (2행, 2행 마지막에 RGB 버튼) */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '3px', justifyContent: 'center' }}>
        {/* 첫 번째 행 - 10개 색상 */}
        <div style={{ display: 'flex', gap: '3px' }}>
          {COLORS.slice(0, 10).map(c => (
            <button
              key={c}
              onClick={() => onColorChange(c)}
              style={{
                width: '32px',
                height: '26px',
                minWidth: '32px',
                padding: '0',
                backgroundColor: c,
                border: color === c ? '2px solid #2196f3' : '2px solid transparent',
                borderRadius: '3px',
                cursor: 'pointer',
                boxSizing: 'border-box',
              }}
            />
          ))}
        </div>

        {/* 두 번째 행 - 9개 색상 + RGB 버튼 */}
        <div style={{ display: 'flex', gap: '3px' }}>
          {COLORS.slice(10, 19).map(c => (
            <button
              key={c}
              onClick={() => onColorChange(c)}
              style={{
                width: '32px',
                height: '26px',
                minWidth: '32px',
                padding: '0',
                backgroundColor: c,
                border: color === c ? '2px solid #2196f3' : '2px solid transparent',
                borderRadius: '3px',
                cursor: 'pointer',
                boxSizing: 'border-box',
              }}
            />
          ))}

          {/* RGB 직접 선택 버튼 */}
          <label style={{
            width: '32px',
            height: '26px',
            minWidth: '32px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            border: '2px solid #ccc',
            borderRadius: '3px',
            cursor: 'pointer',
            backgroundColor: '#fff',
            position: 'relative',
            overflow: 'hidden',
            boxSizing: 'border-box',
          }}>
            <input
              type="color"
              value={color}
              onChange={(e) => onColorChange(e.target.value)}
              style={{
                position: 'absolute',
                width: '100%',
                height: '100%',
                opacity: 0,
                cursor: 'pointer',
              }}
            />
            <span className="material-symbols-outlined" style={{
              fontSize: '18px',
              color: '#666',
              pointerEvents: 'none',
              fontVariationSettings: '"FILL" 1, "wght" 400, "GRAD" 0, "opsz" 48'
            }}>palette</span>
          </label>
        </div>
      </div>

      {/* 오른쪽 끝: 전체 삭제 */}
      <div style={{ display: 'flex', gap: '4px', justifyContent: 'flex-end' }}>
        <button
          onClick={onClear}
          style={{
            padding: '10px 12px',
            fontSize: '14px',
            backgroundColor: '#f44336',
            color: '#fff',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <span className="material-symbols-outlined" style={{
            fontSize: '24px',
            fontVariationSettings: '"FILL" 1, "wght" 400, "GRAD" 0, "opsz" 48'
          }}>delete</span>
        </button>
      </div>
    </div>
  );
}
