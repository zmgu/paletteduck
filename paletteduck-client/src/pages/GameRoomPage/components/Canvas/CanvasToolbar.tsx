
import type { Tool } from '../../../../types/drawing.types';
import { COLORS, LINE_WIDTHS } from '../../../../constants/canvas.constants';

interface CanvasToolbarProps {
  tool: Tool;
  color: string;
  width: number;
  onToolChange: (tool: Tool) => void;
  onColorChange: (color: string) => void;
  onWidthChange: (width: number) => void;
  onClear: () => void;
}

export default function CanvasToolbar({
  tool,
  color,
  width,
  onToolChange,
  onColorChange,
  onWidthChange,
  onClear,
}: CanvasToolbarProps) {
  return (
    <div style={{
      padding: '6px',
      backgroundColor: 'transparent',
      display: 'flex',
      gap: '6px',
      alignItems: 'center',
      flexWrap: 'wrap'
    }}>
      {/* 도구 선택 */}
      <div style={{ display: 'flex', gap: '4px' }}>
        <button
          onClick={() => onToolChange('pen')}
          style={{
            padding: '10px 12px',
            fontSize: '14px',
            backgroundColor: tool === 'pen' ? '#2196f3' : '#fff',
            color: tool === 'pen' ? '#fff' : '#000',
            border: '1px solid #ccc',
            borderRadius: '4px',
            cursor: 'pointer',
          }}
        >
          🖊️
        </button>
        <button
          onClick={() => onToolChange('eraser')}
          style={{
            padding: '10px 12px',
            fontSize: '14px',
            backgroundColor: tool === 'eraser' ? '#2196f3' : '#fff',
            color: tool === 'eraser' ? '#fff' : '#000',
            border: '1px solid #ccc',
            borderRadius: '4px',
            cursor: 'pointer',
          }}
        >
          🧹
        </button>
      </div>

      {/* 굵기 선택 */}
      <div style={{ display: 'flex', gap: '4px', alignItems: 'center' }}>
        {LINE_WIDTHS.map(({ value, label }) => (
          <button
            key={value}
            onClick={() => onWidthChange(value)}
            style={{
              width: '33px',
              height: '33px',
              backgroundColor: width === value ? '#2196f3' : '#fff',
              color: width === value ? '#fff' : '#000',
              border: '1px solid #ccc',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '11px',
            }}
          >
            {label}
          </button>
        ))}
      </div>

      {/* 색상 선택 */}
      <div style={{ display: 'flex', gap: '4px' }}>
        {COLORS.map(c => (
          <button
            key={c}
            onClick={() => onColorChange(c)}
            style={{
              width: '33px',
              height: '33px',
              backgroundColor: c,
              border: color === c ? '3px solid #2196f3' : '1px solid #ccc',
              borderRadius: '4px',
              cursor: 'pointer',
            }}
          />
        ))}
      </div>

      {/* 전체 삭제 */}
      <div style={{ display: 'flex', gap: '4px', marginLeft: 'auto' }}>
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
          }}
        >
          🗑️
        </button>
      </div>
    </div>
  );
}