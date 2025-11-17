export const WS_DESTINATIONS = {
  ROOM_REGISTER: (roomId: string) => `/app/room/${roomId}/register`,
  ROOM_UPDATE: (roomId: string) => `/app/room/${roomId}/update`,
  ROOM_READY: (roomId: string) => `/app/room/${roomId}/ready`,
  ROOM_ROLE: (roomId: string) => `/app/room/${roomId}/role`,
  ROOM_SETTINGS: (roomId: string) => `/app/room/${roomId}/settings`,
  ROOM_START: (roomId: string) => `/app/room/${roomId}/start`,
  ROOM_CHAT: (roomId: string) => `/app/room/${roomId}/chat`,
} as const;

export const WS_TOPICS = {
  ROOM: (roomId: string) => `/topic/room/${roomId}`,
  ROOM_CHAT: (roomId: string) => `/topic/room/${roomId}/chat`,
  ROOM_START: (roomId: string) => `/topic/room/${roomId}/start`,
} as const;