import React, { useState, useRef, useEffect, useMemo } from 'react';
import type { ChatMessage } from '../../../types/chat.types';

interface ChatBoxProps {
  messages: ChatMessage[];
  onSendMessage: (message: string) => void;
  disabled?: boolean;
  currentPlayerId: string;
  isCorrect: boolean;
  isDrawer: boolean;  // 현재 사용자가 출제자인지 여부
}

export default function ChatBox({
  messages,
  onSendMessage,
  disabled,
  currentPlayerId,
  isCorrect,
  isDrawer
}: ChatBoxProps) {
  const [inputValue, setInputValue] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const messagesContainerRef = useRef<HTMLDivElement>(null);

  // 자동 스크롤 (채팅 박스 내부에서만)
  useEffect(() => {
    if (messagesContainerRef.current) {
      messagesContainerRef.current.scrollTop = messagesContainerRef.current.scrollHeight;
    }
  }, [messages]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!inputValue.trim() || disabled) return;
    
    onSendMessage(inputValue.trim());
    setInputValue('');
  };

  // useMemo로 최적화
  const visibleMessages = useMemo(() => {
    const filtered = messages.filter(msg => {
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

      // NORMAL 메시지 필터링
      if (msg.type === 'NORMAL') {
        // 정답 맞춘 사람이 보낸 메시지
        if (msg.senderIsCorrect) {
          // 정답 맞춘 사람과 출제자만 볼 수 있음
          return isCorrect || isDrawer;
        }
        // 정답 못 맞춘 사람이 보낸 메시지는 모두 볼 수 있음
        return true;
      }

      return true;
    });

    return filtered;
  }, [messages, currentPlayerId, isCorrect, isDrawer]);

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      height: '100%',
      border: '2px solid #ccc',
      borderRadius: '8px',
      backgroundColor: '#fff',
    }}>
      {/* 채팅 메시지 영역 */}
      <div
        ref={messagesContainerRef}
        style={{
          flex: 1,
          overflowY: 'auto',
          padding: '10px',
          display: 'flex',
          flexDirection: 'column',
          gap: '8px',
        }}
      >
        {visibleMessages.length === 0 && (
          <div style={{ color: '#999', textAlign: 'center', marginTop: '20px' }}>
            채팅이 없습니다
          </div>
        )}
        {visibleMessages.map((msg) => {
          // 메시지 타입에 따른 스타일 결정
          let backgroundColor = '#f8f9fa';
          let borderColor = '#dee2e6';

          if (msg.type === 'SYSTEM') {
            backgroundColor = '#fff3cd';
            borderColor = '#ffc107';
          } else if (msg.type === 'CORRECT') {
            backgroundColor = '#d4edda';
            borderColor = '#28a745';
          } else if (msg.type === 'NORMAL' && msg.senderIsCorrect) {
            // 정답 맞춘 사람의 채팅
            backgroundColor = '#e7f3ff';
            borderColor = '#0066cc';
          }

          return (
            <div
              key={msg.messageId}
              style={{
                padding: '8px 12px',
                borderRadius: '4px',
                backgroundColor,
                border: `1px solid ${borderColor}`,
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
                  {msg.senderIsCorrect && (
                    <span style={{
                      marginLeft: '6px',
                      fontSize: '11px',
                      color: '#0066cc',
                      fontWeight: 'normal'
                    }}>
                      (정답자)
                    </span>
                  )}
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
          );
        })}
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