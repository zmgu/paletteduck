// src/types/game.types.ts
export interface PlayerJoinResponse {
  token: string;
  playerId: string;
  nickname: string;
}

export interface RoomPlayer {
  playerId: string;
  nickname: string;
  isHost: boolean;
  isReady: boolean;
}

export interface RoomInfo {
  roomId: string;
  inviteCode: string;
  players: RoomPlayer[];
}

export interface RoomCreateResponse {
  roomId: string;
  inviteCode: string;
}