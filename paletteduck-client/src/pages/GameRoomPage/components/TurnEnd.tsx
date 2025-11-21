import React from 'react';
import type { TurnInfo } from '../../../types/game.types';

interface TurnEndProps {
  turnInfo: TurnInfo;
}

export default function TurnEnd({ turnInfo }: TurnEndProps) {
  return (
    <div style={{
      padding: '40px',
      textAlign: 'center',
      backgroundColor: '#fff',
      borderRadius: '8px',
      border: '2px solid #ccc',
      marginTop: '20px',
    }}>
      {/* ✅ 턴 번호 추가 */}
      <div style={{
        fontSize: '16px',
        color: '#666',
        marginBottom: '10px',
      }}>
        턴 {turnInfo.turnNumber}
      </div>
      
      <h2 style={{ fontSize: '28px', marginBottom: '20px', color: '#333' }}>
        턴 종료!
      </h2>
      
      <div style={{
        fontSize: '20px',
        marginBottom: '30px',
        color: '#666',
      }}>
        정답은 <strong style={{ fontSize: '32px', color: '#2196f3' }}>"{turnInfo.word}"</strong> 였습니다!
      </div>
      
      <div style={{
        padding: '15px',
        backgroundColor: '#f5f5f5',
        borderRadius: '6px',
        fontSize: '16px',
        color: '#666',
      }}>
        잠시 후 다음 턴이 시작됩니다...
      </div>
    </div>
  );
}