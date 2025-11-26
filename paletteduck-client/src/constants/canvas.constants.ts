export const CANVAS_CONFIG = {
  WIDTH: 800,
  HEIGHT: 600,
  THROTTLE_MS: 16,        // 16ms마다 전송 (60fps, 모니터 주사율과 동기화)
  BATCH_SIZE: 3,          // 3개 포인트씩 전송 (최대한 빠른 전송)
  BACKGROUND_COLOR: '#FFFFFF',
} as const;

export const COLORS = [
  '#000000', 
  '#FFFFFF', 
  '#FF0000', 
  '#00FF00', 
  '#0000FF',
  '#FFFF00', 
  '#FF00FF', 
  '#00FFFF', 
  '#FFA500', 
  '#800080'
] as const;

export const LINE_WIDTHS = [
  { value: 4, label: 1 },
  { value: 6, label: 2 },
  { value: 8, label: 3 },
] as const;

export const TOOL_TYPES = {
  PEN: 'pen' as const,
  ERASER: 'eraser' as const,
};