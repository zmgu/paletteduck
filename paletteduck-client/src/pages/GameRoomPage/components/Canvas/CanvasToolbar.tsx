
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
      padding: '10px', 
      backgroundColor: '#f5f5f5', 
      display: 'flex', 
      gap: '10px',
      alignItems: 'center',
      flexWrap: 'wrap'
    }}>
      {/* 도구 선택 */}
      <div style={{ display: 'flex', gap: '5px' }}>
        <button
          onClick={() => onToolChange('pen')}
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
          onClick={() => onToolChange('eraser')}
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

      {/* 굵기 선택 */}
      <div style={{ display: 'flex', gap: '5px', alignItems: 'center' }}>
        <span>굵기:</span>
        {LINE_WIDTHS.map(({ value, label }) => (
          <button
            key={value}
            onClick={() => onWidthChange(value)}
            style={{
              width: '30px',
              height: '30px',
              backgroundColor: width === value ? '#2196f3' : '#fff',
              color: width === value ? '#fff' : '#000',
              border: '1px solid #ccc',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '12px',
            }}
          >
            {label}
          </button>
        ))}
      </div>

      {/* 색상 선택 */}
      <div style={{ display: 'flex', gap: '5px' }}>
        {COLORS.map(c => (
          <button
            key={c}
            onClick={() => onColorChange(c)}
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

      {/* 전체 삭제 */}
      <div style={{ display: 'flex', gap: '5px', marginLeft: 'auto' }}>
        <button
          onClick={onClear}
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
  );
}