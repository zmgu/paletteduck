import { useEffect, useRef } from 'react';
import { wsClient } from '../utils/wsClient';

interface UseWebSocketSubscriptionOptions<T> {
  topic: string;
  onMessage: (data: T) => void;
  enabled?: boolean;
  delay?: number;
}

/**
 * WebSocket 구독을 위한 공통 Hook
 * 자동으로 구독 해제 및 정리 작업을 처리합니다.
 *
 * @example
 * useWebSocketSubscription({
 *   topic: WS_TOPICS.ROOM_CHAT(roomId),
 *   onMessage: (data: ChatMessage) => setMessages(prev => [...prev, data]),
 *   enabled: !!roomId,
 *   delay: 100,
 * });
 */
export function useWebSocketSubscription<T = any>({
  topic,
  onMessage,
  enabled = true,
  delay = 0,
}: UseWebSocketSubscriptionOptions<T>) {
  const unsubscribeRef = useRef<(() => void) | null>(null);

  useEffect(() => {
    if (!enabled || !topic) return;

    const timer = setTimeout(() => {
      unsubscribeRef.current = wsClient.subscribe(topic, onMessage);
    }, delay);

    return () => {
      clearTimeout(timer);
      if (unsubscribeRef.current) {
        unsubscribeRef.current();
        unsubscribeRef.current = null;
      }
    };
  }, [topic, enabled, delay]); // onMessage는 의도적으로 제외 (무한 재구독 방지)
}
