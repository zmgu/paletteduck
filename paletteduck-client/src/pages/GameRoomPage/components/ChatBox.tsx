import React, { useState, useRef, useEffect, useMemo } from 'react';
import type { ChatMessage } from '../../../types/chat.types';

interface ChatBoxProps {
  messages: ChatMessage[];
  onSendMessage: (message: string) => void;
  disabled?: boolean;
  currentPlayerId: string;
  isCorrect: boolean;
}

export default function ChatBox({ 
  messages, 
  onSendMessage, 
  disabled,
  currentPlayerId,
  isCorrect
}: ChatBoxProps) {
  const [inputValue, setInputValue] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const scrollContainerRef = useRef<HTMLDivElement>(null);  // ✅ 추가

  // ✅ 자동 스크롤 수정
  useEffect(() => {
    // scrollIntoView 대신 컨테이너의 scrollTop 조작
    if (scrollContainerRef.current) {
      scrollContainerRef.current.scrollTop = scrollContainerRef.current.scrollHeight;
    }
  }, [messages]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!inputValue.trim() || disabled) return;
    
    onSendMessage(inputValue.trim());
    setInputValue('');
  };

  const visibleMessages = useMemo(() => {
    const filtered = messages.filter(msg => {
      if (msg.type === 'SYSTEM') {
        return true;
      }
      
      if (msg.type === 'CORRECT') {
        if (msg.message.includes('🎉 정답입니다!')) {
          return msg.playerId === currentPlayerId;
        }
        return true;
      }
      
      if (msg.isCorrect) {
        return isCorrect;
      }
      
      return true;
    });
    
    return filtered;
  }, [messages, currentPlayerId, isCorrect]);

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
      <div 
        ref={scrollContainerRef}  // ✅ ref 추가
        style={{
          flex: 1,
          overflowY: 'auto',
          padding: '10px',
          display: 'flex',
          flexDirection: 'column',
          gap: '8px',
          position: 'relative',  // ✅ 추가
        }}
      >
        {visibleMessages.length === 0 && (
          <div style={{ color: '#999', textAlign: 'center', marginTop: '20px' }}>
            채팅이 없습니다
          </div>
        )}
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
        <div ref={messagesEndRef} />  {/* 마커는 유지하되 scrollIntoView 안씀 */}
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