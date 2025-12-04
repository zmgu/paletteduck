export type PlayerRole = 'PLAYER' | 'SPECTATOR';
export type RoomStatus = 'WAITING' | 'PLAYING' | 'FINISHED';
export type ChatType = 'NORMAL' | 'CORRECT' | 'SYSTEM';
export type GamePhase = 'COUNTDOWN' | 'WORD_SELECT' | 'DRAWING' | 'TURN_RESULT' | 'ROUND_END' | 'GAME_END';
export type VoteType = 'LIKE' | 'DISLIKE' | 'NONE';
export type TurnEndReason = 'TIME_OUT' | 'ALL_CORRECT' | 'DRAWER_LEFT';

// API Response types
export interface PlayerJoinResponse {
  token: string;
  playerId: string;
  nickname: string;
}

export interface RoomCreateResponse {
  roomId: string;
  inviteCode: string;
}

export interface RoomListResponse {
  roomId: string;
  inviteCode: string;
  status: RoomStatus;
  currentPlayers: number;
  maxPlayers: number;
  hostNickname: string;
  currentRound: number | null;
  totalRounds: number | null;
  createdAt: number;
}

export interface RoomPlayer {
  playerId: string;
  nickname: string;
  host: boolean;
  ready: boolean;
  role: PlayerRole;
  score: number;
  totalLikes: number;
  totalDislikes: number;
}

export interface GameSettings {
  maxPlayers: number;
  rounds: number;
  wordChoices: number;
  drawTime: number;
  maxSpectators: number;
}

export interface RoomInfo {
  roomId: string;
  inviteCode: string;
  players: RoomPlayer[];
  settings: GameSettings;
  status: RoomStatus;
}

export interface ChatMessage {
  playerId: string;
  nickname: string;
  message: string;
  type: ChatType;
  timestamp: number;
}

export interface TurnInfo {
  turnNumber: number;
  drawerId: string;
  drawerNickname: string;
  word: string | null;
  wordChoices: string[];
  timeLeft: number;
  correctPlayerIds: string[];
  hintLevel: number;
  currentHint: string | null;
  hintArray: string[] | null;
  revealedChosungPositions: number[];
  revealedLetterPositions: number[];
  votes: Record<string, VoteType>;  // voterId -> VoteType
  turnScores: Record<string, number>;  // playerId -> score earned in this turn
  turnEndReason?: TurnEndReason;  // 턴 종료 사유
  drawingEvents?: any[];  // 그림 이벤트 저장 (도중 참가자를 위해)
}

export interface Player {
  playerId: string;
  nickname: string;
  score: number;
  isCorrect: boolean;
  totalLikes: number;
  totalDislikes: number;
}

export interface GameState {
  roomId: string;
  currentRound: number;
  totalRounds: number;
  phase: GamePhase;
  currentTurn: TurnInfo | null;
  turnOrder: string[];
  phaseStartTime: number;
  drawTime: number;
  players: Player[];
}

// Drawing types are now in drawing.types.ts
// Use DrawingData for WebSocket transmission (compressed format)
// DrawPoint is defined in drawing.types.ts