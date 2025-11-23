import { useMemo } from 'react';
import type { ChatMessage } from '../types/chat.types';

interface UseMessageFilterOptions {
  messages: ChatMessage[];
  currentPlayerId: string;
  isCorrect: boolean;
}

/**
 * 채팅 메시지 필터링 로직을 처리하는 Hook
 *
 * 필터링 규칙:
 * - SYSTEM 메시지: 항상 표시
 * - CORRECT 메시지 중 "🎉 정답입니다!": 본인에게만 표시
 * - 정답 맞춘 사람의 일반 채팅: 정답 맞춘 사람끼리만 표시
 *
 * @example
 * const visibleMessages = useMessageFilter({
 *   messages,
 *   currentPlayerId,
 *   isCorrect,
 * });
 */
export function useMessageFilter({
  messages,
  currentPlayerId,
  isCorrect,
}: UseMessageFilterOptions): ChatMessage[] {
  return useMemo(() => {
    return messages.filter(msg => {
      // SYSTEM 메시지는 항상 표시
      if (msg.type === 'SYSTEM') {
        return true;
      }

      // CORRECT 메시지
      if (msg.type === 'CORRECT') {
        // "🎉 정답입니다!"는 본인에게만
        if (msg.message.includes('🎉 정답입니다!')) {
          return msg.playerId === currentPlayerId;
        }
        return true;
      }

      // 일반 채팅: 발신자가 정답 맞춘 사람이면
      if (msg.isCorrect) {
        return isCorrect;
      }

      return true;
    });
  }, [messages, currentPlayerId, isCorrect]);
}
