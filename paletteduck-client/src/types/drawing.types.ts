export interface DrawingPoint {
  x: number;
  y: number;
}

export type Tool = 'pen' | 'eraser';

export interface DrawingData {
  t: 0 | 1;              // tool (0=pen, 1=eraser)
  c: string;             // color
  w: number;             // width
  p: number[];           // points [x1,y1,x2,y2,...]
  s: boolean;            // isStart
  playerId?: string;
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