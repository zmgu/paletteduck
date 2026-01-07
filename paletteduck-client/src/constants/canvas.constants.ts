export const CANVAS_CONFIG = {
  WIDTH: 800,
  HEIGHT: 600,
  THROTTLE_MS: 8,         // 8ms마다 전송 (125fps, 더 부드러운 곡선)
  BATCH_SIZE: 5,          // 5개 포인트씩 전송 (곡선 품질 향상)
  BACKGROUND_COLOR: '#FFFFFF',
} as const;

export const COLORS = [
  // 첫 번째 행 - 밝은 색상 (무지개 순서)
  '#FF0000', // 빨강
  '#FF69B4', // 핫핑크
  '#FF1493', // 진분홍
  '#FFA500', // 오렌지
  '#FFFF00', // 노랑
  '#32CD32', // 라임
  '#00FF00', // 초록
  '#00FFFF', // 시안
  '#0000FF', // 파랑
  '#FF00FF', // 마젠타
  // 두 번째 행 - 어두운/중간 톤 (9개 + RGB 버튼 자리)
  '#A0522D', // 시에나
  '#8B4513', // 갈색
  '#FFB6C1', // 연분홍
  '#FFDAB9', // 피치
  '#F0E68C', // 카키
  '#008080', // 청록
  '#4169E1', // 로얄블루
  '#800080', // 보라
  '#9370DB', // 미디엄퍼플
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