export interface ChatMessage {
  messageId: string;
  playerId: string;
  nickname: string;
  message: string;
  type: ChatMessageType;
  isCorrect?: boolean;
  senderIsCorrect?: boolean;  // 발신자가 정답을 맞춘 상태인지 여부
  timestamp: number;
}

export type ChatMessageType = 'NORMAL' | 'CORRECT' | 'SYSTEM';