import React, { useState, useRef, useEffect } from 'react';
import type { ChatMessage } from '../../../types/chat.types';

interface ChatBoxProps {
  messages: ChatMessage[];
  onSendMessage: (message: string) => void;
  disabled?: boolean;
  currentPlayerId: string;
  isCorrect: boolean;  // ✅ 현재 사용자가 정답 맞췄는지
}

export default function ChatBox({ 
  messages, 
  onSendMessage, 
  disabled,
  currentPlayerId,
  isCorrect  // ✅ 추가
}: ChatBoxProps) {
  const [inputValue, setInputValue] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);

      console.log('[ChatBox] Rendering...');
  console.log('[ChatBox] Total messages:', messages.length);
  console.log('[ChatBox] isCorrect:', isCorrect);
  console.log('[ChatBox] disabled:', disabled);
  console.log('[ChatBox] Messages:', messages);

  // 자동 스크롤
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!inputValue.trim() || disabled) return;
    
    onSendMessage(inputValue.trim());
    setInputValue('');
  };

  // ✅ 메시지 필터링: 
  // - 정답 맞춘 사람의 일반 채팅은 정답 맞춘 사람들만 볼 수 있음
  // - 시스템 메시지와 정답 메시지는 모두 볼 수 있음
  const visibleMessages = messages.filter(msg => {
    // 시스템 메시지, 정답 메시지는 모두 표시
    if (msg.type === 'SYSTEM' || msg.type === 'CORRECT') return true;
    
    // 일반 채팅: 발신자가 정답 맞춘 사람이면
    if (msg.isCorrect) {
      // 나도 정답 맞췄으면 보이고, 아니면 숨김
      return isCorrect;
    }
    
    // 정답 못 맞춘 사람의 채팅은 모두 표시
    return true;
  });

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      height: '400px',
      border: '2px solid #ccc',
      borderRadius: '8px',
      backgroundColor: '#fff',
    }}>
      {/* 채팅 메시지 영역 */}
      <div style={{
        flex: 1,
        overflowY: 'auto',
        padding: '10px',
        display: 'flex',
        flexDirection: 'column',
        gap: '8px',
      }}>
        {visibleMessages.map((msg) => (
          <div 
            key={msg.messageId}
            style={{
              padding: '8px 12px',
              borderRadius: '4px',
              backgroundColor: msg.type === 'SYSTEM'
                ? '#fff3cd' 
                : msg.type === 'CORRECT'
                  ? '#d4edda' 
                  : '#f8f9fa',
              border: msg.type === 'SYSTEM'
                ? '1px solid #ffc107'
                : msg.type === 'CORRECT'
                  ? '1px solid #28a745'
                  : '1px solid #dee2e6',
            }}
          >
            {msg.type === 'NORMAL' && (
              <div style={{ 
                fontSize: '12px', 
                fontWeight: 'bold',
                color: msg.playerId === currentPlayerId ? '#007bff' : '#666',
                marginBottom: '4px'
              }}>
                {msg.nickname}
              </div>
            )}
            <div style={{ 
              fontSize: '14px',
              color: msg.type === 'SYSTEM' ? '#856404' : '#000',
              fontWeight: msg.type !== 'NORMAL' ? 'bold' : 'normal',
            }}>
              {msg.message}
            </div>
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>

      {/* 입력 영역 */}
      <form 
        onSubmit={handleSubmit}
        style={{
          padding: '10px',
          borderTop: '1px solid #ccc',
          display: 'flex',
          gap: '8px',
        }}
      >
        <input
          type="text"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          placeholder={disabled ? "채팅을 입력할 수 없습니다" : isCorrect ? "다른 정답자들과 채팅..." : "정답을 입력하세요..."}
          disabled={disabled}
          style={{
            flex: 1,
            padding: '8px 12px',
            border: '1px solid #ccc',
            borderRadius: '4px',
            fontSize: '14px',
          }}
        />
        <button
          type="submit"
          disabled={disabled || !inputValue.trim()}
          style={{
            padding: '8px 20px',
            backgroundColor: disabled ? '#ccc' : '#007bff',
            color: '#fff',
            border: 'none',
            borderRadius: '4px',
            cursor: disabled ? 'not-allowed' : 'pointer',
            fontSize: '14px',
            fontWeight: 'bold',
          }}
        >
          전송
        </button>
      </form>
    </div>
  );
}