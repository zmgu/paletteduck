export type PlayerRole = 'PLAYER' | 'SPECTATOR';
export type RoomStatus = 'WAITING' | 'PLAYING' | 'FINISHED';
export type GameMode = 'NORMAL' | 'CUSTOM';
export type ChatType = 'NORMAL' | 'CORRECT' | 'SYSTEM';
export type GamePhase = 'COUNTDOWN' | 'WORD_SELECT' | 'DRAWING' | 'ROUND_END' | 'GAME_END';

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
  mode: GameMode;
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
}

export interface DrawPoint {
  x: number;
  y: number;
}

export interface DrawData {
  playerId: string;
  tool: 'pen' | 'eraser';
  color: string;
  width: number;
  points: DrawPoint[];
}