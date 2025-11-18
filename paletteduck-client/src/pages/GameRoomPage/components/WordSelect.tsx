import React from 'react';
import type { TurnInfo } from '../../../types/game.types';

interface WordSelectProps {
  turnInfo: TurnInfo;
  onSelectWord: (word: string) => void;
}

export default function WordSelect({ turnInfo, onSelectWord }: WordSelectProps) {
  return (
    <div style={{ 
      marginTop: '20px', 
      padding: '30px', 
      border: '3px solid #2196f3',
      borderRadius: '12px',
      backgroundColor: '#e3f2fd'
    }}>
      <h3 style={{ marginTop: 0, fontSize: '24px', textAlign: 'center' }}>
        단어를 선택하세요
      </h3>
      <div style={{ display: 'flex', gap: '15px', marginTop: '20px' }}>
        {turnInfo.wordChoices.map((word) => (
          <button
            key={word}
            onClick={() => onSelectWord(word)}
            style={{
              flex: 1,
              padding: '30px 20px',
              fontSize: '24px',
              fontWeight: 'bold',
              backgroundColor: '#2196f3',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              cursor: 'pointer',
              transition: 'all 0.2s',
            }}
            onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#1976d2'}
            onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#2196f3'}
          >
            {word}
          </button>
        ))}
      </div>
    </div>
  );
}