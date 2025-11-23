// 색상 상수
export const COLORS = {
  // 기본 색상
  PRIMARY: '#2196f3',
  SUCCESS: '#4caf50',
  WARNING: '#ff9800',
  DANGER: '#ff5722',
  INFO: '#00bcd4',

  // 배경 색상
  BG_WHITE: '#ffffff',
  BG_LIGHT: '#f8f9fa',
  BG_WARNING_LIGHT: '#fff3cd',
  BG_SUCCESS_LIGHT: '#d4edda',

  // 테두리 색상
  BORDER_LIGHT: '#dee2e6',
  BORDER_WARNING: '#ffc107',
  BORDER_SUCCESS: '#28a745',

  // 텍스트 색상
  TEXT_DARK: '#333',
  TEXT_MUTED: '#666',
  TEXT_LIGHT: '#999',
} as const;

// 폰트 크기
export const FONT_SIZES = {
  XS: '10px',
  SM: '12px',
  MD: '14px',
  LG: '16px',
  XL: '18px',
  XXL: '24px',
  XXXL: '28px',
  HUGE: '48px',
  GIANT: '64px',
} as const;

// 간격
export const SPACING = {
  XS: '4px',
  SM: '8px',
  MD: '12px',
  LG: '16px',
  XL: '20px',
  XXL: '24px',
} as const;

// 버튼 스타일
export const BUTTON_STYLES = {
  padding: `${SPACING.SM} ${SPACING.LG}`,
  borderRadius: '4px',
  cursor: 'pointer',
  border: 'none',
  fontSize: FONT_SIZES.MD,
  transition: 'all 0.2s',
} as const;

// 공통 컨테이너 스타일
export const CONTAINER_STYLES = {
  borderRadius: '8px',
  padding: SPACING.LG,
  boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
} as const;
