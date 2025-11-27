export const CANVAS_CONFIG = {
  WIDTH: 800,
  HEIGHT: 600,
  THROTTLE_MS: 8,         // 8ms마다 전송 (125fps, 더 부드러운 곡선)
  BATCH_SIZE: 5,          // 5개 포인트씩 전송 (곡선 품질 향상)
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