import { useState, type FormEvent } from 'react';
import { GAME_CONSTANTS } from '../../../constants/gameConstants';
import type { ChatMessage } from '../../../types/game.types';

interface ChatBoxProps {
  messages: ChatMessage[];
  onSendMessage: (message: string) => void;
}

export default function ChatBox({ messages, onSendMessage }: ChatBoxProps) {
  const [input, setInput] = useState('');

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!input.trim()) return;

    onSendMessage(input);
    setInput('');
  };

  return (
    <div>
      <div style={{
        height: '280px', 
        overflowY: 'auto', 
        border: '1px solid #eee', 
        padding: '10px', 
        marginBottom: '10px', 
        backgroundColor: '#fafafa' 
      }}>
        {messages.map((msg, idx) => (
          <div key={idx} style={{ 
            marginBottom: '5px',
            color: msg.type === 'SYSTEM' ? 'green' : msg.type === 'CORRECT' ? 'gray' : 'black'
          }}>
            {msg.type === 'SYSTEM' ? (
              msg.message
            ) : (
              <><strong>{msg.nickname}:</strong> {msg.message}</>
            )}
          </div>
        ))}
      </div>
      <form onSubmit={handleSubmit} style={{ display: 'flex', gap: '10px' }}>
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder={`메시지 입력 (최대 ${GAME_CONSTANTS.MAX_CHAT_LENGTH}자)`}
          maxLength={GAME_CONSTANTS.MAX_CHAT_LENGTH}
          style={{ flex: 1, padding: '10px' }}
        />
        <button type="submit" style={{ padding: '10px 20px' }}>전송</button>
      </form>
    </div>
  );
}