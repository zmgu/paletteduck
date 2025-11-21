export const WS_DESTINATIONS = {
  // 룸 관련
  ROOM_JOIN: (roomId: string) => `/app/room/${roomId}/join`,
  ROOM_REGISTER: (roomId: string) => `/app/room/${roomId}/register`,
  ROOM_UPDATE: (roomId: string) => `/app/room/${roomId}/update`,
  ROOM_LEAVE: (roomId: string) => `/app/room/${roomId}/leave`,
  ROOM_READY: (roomId: string) => `/app/room/${roomId}/ready`,
  ROOM_ROLE: (roomId: string) => `/app/room/${roomId}/role`,
  ROOM_SETTINGS: (roomId: string) => `/app/room/${roomId}/settings`,
  ROOM_START: (roomId: string) => `/app/room/${roomId}/start`,
  ROOM_CHAT: (roomId: string) => `/app/room/${roomId}/chat`,
  ROOM_RESTART: (roomId: string) => `/app/room/${roomId}/restart`,
  
  // 게임 관련 (✅ 기존 경로 유지)
  GAME_WORD_SELECT: (roomId: string) => `/app/room/${roomId}/game/word/select`,
  GAME_DRAWING: (roomId: string) => `/app/room/${roomId}/game/drawing`,
  GAME_CLEAR: (roomId: string) => `/app/room/${roomId}/game/clear`,
  GAME_CHAT: (roomId: string) => `/app/room/${roomId}/game/chat`,
} as const;

// WebSocket Topics (구독)
export const WS_TOPICS = {
  ROOM: (roomId: string) => `/topic/room/${roomId}`,
  ROOM_CHAT: (roomId: string) => `/topic/room/${roomId}/chat`,
  ROOM_START: (roomId: string) => `/topic/room/${roomId}/start`,
  GAME_START: (roomId: string) => `/topic/room/${roomId}/game/start`,
  GAME_STATE: (roomId: string) => `/topic/room/${roomId}/game/state`,
  GAME_DRAWING: (roomId: string) => `/topic/room/${roomId}/game/drawing`,
  GAME_CLEAR: (roomId: string) => `/topic/room/${roomId}/game/clear`,
  
  // 추가된 것들
  ROOM_PLAYERS: (roomId: string) => `/topic/room/${roomId}/players`,
  ROOM_STATE: (roomId: string) => `/topic/room/${roomId}/state`,
  ROOM_DRAWING: (roomId: string) => `/topic/room/${roomId}/drawing`,
  ROOM_CANVAS_CLEAR: (roomId: string) => `/topic/room/${roomId}/canvas/clear`,
} as const;