// src/types/game.types.ts
export interface PlayerJoinResponse {
  token: string;
  playerId: string;
  nickname: string;
}

export type PlayerRole = 'PLAYER' | 'SPECTATOR';

export type RoomStatus = 'WAITING' | 'PLAYING' | 'FINISHED';

export type GameMode = 'NORMAL' | 'CUSTOM';

export type ChatType = 'NORMAL' | 'CORRECT' | 'SYSTEM';

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

export interface RoomCreateResponse {
  roomId: string;
  inviteCode: string;
}

export interface ChatMessage {
  playerId: string;
  nickname: string;
  message: string;
  type: ChatType;
  timestamp: number;
}