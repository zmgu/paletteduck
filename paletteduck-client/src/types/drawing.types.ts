export interface DrawingPoint {
  x: number;
  y: number;
}

export type Tool = 'pen' | 'eraser';

// 압축된 형식의 그리기 데이터 (WebSocket 전송용)
export interface DrawingData {
  t: 0 | 1;              // tool (0=pen, 1=eraser)
  c: string;             // color
  w: number;             // width
  p: number[];           // points [x1,y1,x2,y2,...]
  s: boolean;            // isStart
  playerId?: string;
}

// 일반 형식의 그리기 데이터
export interface FullDrawData {
  playerId: string;
  tool: Tool;
  color: string;
  width: number;
  points: DrawingPoint[];
}

export interface BoundingBox {
  minX: number;
  minY: number;
  maxX: number;
  maxY: number;
}

export interface CanvasToolState {
  tool: Tool;
  color: string;
  width: number;
}