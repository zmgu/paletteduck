export const WS_DESTINATIONS = {
  ROOM_REGISTER: (roomId: string) => `/app/room/${roomId}/register`,
  ROOM_UPDATE: (roomId: string) => `/app/room/${roomId}/update`,
  ROOM_READY: (roomId: string) => `/app/room/${roomId}/ready`,
  ROOM_ROLE: (roomId: string) => `/app/room/${roomId}/role`,
  ROOM_SETTINGS: (roomId: string) => `/app/room/${roomId}/settings`,
  ROOM_START: (roomId: string) => `/app/room/${roomId}/start`,
  ROOM_CHAT: (roomId: string) => `/app/room/${roomId}/chat`,
  ROOM_RETURN_TO_WAITING: (roomId: string) => `/app/room/${roomId}/return-to-waiting`,
  GAME_WORD_SELECT: (roomId: string) => `/app/room/${roomId}/game/word/select`,
  GAME_DRAWING: (roomId: string) => `/app/room/${roomId}/game/drawing`,
  GAME_CLEAR: (roomId: string) => `/app/room/${roomId}/game/clear`,
  GAME_CHAT: (roomId: string) => `/app/room/${roomId}/game/chat`,
} as const;

export const WS_TOPICS = {
  ROOM: (roomId: string) => `/topic/room/${roomId}`,
  ROOM_CHAT: (roomId: string) => `/topic/room/${roomId}/chat`,
  ROOM_START: (roomId: string) => `/topic/room/${roomId}/start`,
  GAME_START: (roomId: string) => `/topic/room/${roomId}/game/start`,
  GAME_STATE: (roomId: string) => `/topic/room/${roomId}/game/state`,
  GAME_DRAWING: (roomId: string) => `/topic/room/${roomId}/game/drawing`,
  GAME_CLEAR: (roomId: string) => `/topic/room/${roomId}/game/clear`,
} as const;