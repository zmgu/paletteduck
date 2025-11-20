export interface ChatMessage {
  messageId: string;
  playerId: string;
  nickname: string;
  message: string;
  type: ChatMessageType;
  isCorrect?: boolean;
  timestamp: number;
}

export type ChatMessageType = 'NORMAL' | 'CORRECT' | 'SYSTEM';