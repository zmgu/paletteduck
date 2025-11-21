import React from 'react';
import { useNavigate } from 'react-router-dom';
import type { GameState } from '../../../types/game.types';
import { wsClient } from '../../../utils/wsClient';
import { WS_DESTINATIONS } from '../../../constants/wsDestinations';
import { getPlayerInfo } from '../../../utils/apiClient';

interface GameEndProps {
  gameState: GameState;
  roomId: string;
  isHost: boolean;
}

// ✅ isHost 추가
export default function GameEnd({ gameState, roomId, isHost }: GameEndProps) {
  const navigate = useNavigate();
  const playerInfo = getPlayerInfo();
  
  // ✅ 디버깅 로그
  console.log('[GameEnd] isHost:', isHost);
  console.log('[GameEnd] playerInfo:', playerInfo);
  
  const sortedPlayers = [...gameState.players].sort((a, b) => 
    (b.score || 0) - (a.score || 0)
  );

  const winner = sortedPlayers[0];


  // ✅ 다시하기
  const handleRestart = () => {
    if (!playerInfo || !roomId) return;
    
    console.log('[GameEnd] Requesting restart...');
    
    wsClient.send(WS_DESTINATIONS.ROOM_RESTART(roomId), {
      playerId: playerInfo.playerId,
    });
    
    // 대기실로 이동
    navigate(`/room/${roomId}/lobby`);
  };

  // ✅ 방 나가기
  const handleLeave = () => {
    if (!playerInfo || !roomId) return;
    
    console.log('[GameEnd] Leaving room...');
    
    wsClient.send(WS_DESTINATIONS.ROOM_LEAVE(roomId), {
      playerId: playerInfo.playerId,
    });
    
    // 로비로 이동
    navigate('/');
  };

  return (
    <div style={{
      padding: '40px',
      textAlign: 'center',
      backgroundColor: '#fff',
      borderRadius: '8px',
      border: '2px solid #ccc',
      marginTop: '20px',
    }}>
      <h1 style={{ fontSize: '36px', marginBottom: '20px', color: '#333' }}>
        🎉 게임 종료!
      </h1>

      {/* 우승자 */}
      {winner && (
        <div style={{
          padding: '30px',
          backgroundColor: '#fff3cd',
          border: '3px solid #ffc107',
          borderRadius: '12px',
          marginBottom: '30px',
        }}>
          <div style={{ fontSize: '20px', color: '#856404', marginBottom: '10px' }}>
            🏆 우승자
          </div>
          <div style={{ fontSize: '32px', fontWeight: 'bold', color: '#333', marginBottom: '10px' }}>
            {winner.playerName}
          </div>
          <div style={{ fontSize: '28px', color: '#ffc107', fontWeight: 'bold' }}>
            {winner.score}점
          </div>
        </div>
      )}

      {/* 최종 순위 */}
      <div style={{ marginTop: '30px' }}>
        <h2 style={{ fontSize: '24px', marginBottom: '20px' }}>최종 순위</h2>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '15px', maxWidth: '500px', margin: '0 auto' }}>
          {sortedPlayers.map((player, index) => (
            <div
              key={player.playerId}
              style={{
                display: 'flex',
                alignItems: 'center',
                padding: '15px 20px',
                backgroundColor: index === 0 ? '#fff3cd' : index === 1 ? '#e8f5e9' : index === 2 ? '#ffe0b2' : '#f5f5f5',
                border: `2px solid ${index === 0 ? '#ffc107' : index === 1 ? '#4caf50' : index === 2 ? '#ff9800' : '#ddd'}`,
                borderRadius: '8px',
              }}
            >
              <div style={{
                width: '40px',
                height: '40px',
                borderRadius: '50%',
                backgroundColor: index === 0 ? '#ffd700' : index === 1 ? '#c0c0c0' : index === 2 ? '#cd7f32' : '#e0e0e0',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '18px',
                fontWeight: 'bold',
                color: index <= 2 ? '#fff' : '#666',
                marginRight: '15px',
              }}>
                {index + 1}
              </div>

              <div style={{ flex: 1, fontSize: '18px', fontWeight: 'bold', textAlign: 'left' }}>
                {player.playerName}
              </div>

              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#333' }}>
                {player.score || 0}점
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 통계 */}
      <div style={{
        marginTop: '40px',
        padding: '20px',
        backgroundColor: '#f8f9fa',
        borderRadius: '8px',
      }}>
        <div style={{ fontSize: '16px', color: '#666' }}>
          총 {gameState.totalRounds || 0}라운드 · {gameState.currentTurn?.turnNumber || 0}턴 진행
        </div>
      </div>

      {/* ✅ 버튼 */}
      <div style={{
        marginTop: '40px',
        display: 'flex',
        gap: '20px',
        justifyContent: 'center',
      }}>
        {/* 다시하기 (방장만) */}
        {isHost && (
          <button
            onClick={handleRestart}
            style={{
              padding: '15px 40px',
              fontSize: '18px',
              fontWeight: 'bold',
              backgroundColor: '#28a745',
              color: '#fff',
              border: 'none',
              borderRadius: '8px',
              cursor: 'pointer',
              transition: 'background-color 0.2s',
            }}
            onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#218838'}
            onMouseOut={(e) => e.currentTarget.style.backgroundColor = '#28a745'}
          >
            🔄 다시하기
          </button>
        )}

        {/* 방 나가기 */}
        <button
          onClick={handleLeave}
          style={{
            padding: '15px 40px',
            fontSize: '18px',
            fontWeight: 'bold',
            backgroundColor: '#dc3545',
            color: '#fff',
            border: 'none',
            borderRadius: '8px',
            cursor: 'pointer',
            transition: 'background-color 0.2s',
          }}
          onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#c82333'}
          onMouseOut={(e) => e.currentTarget.style.backgroundColor = '#dc3545'}
        >
          🚪 방 나가기
        </button>
      </div>

      {/* 방장 안내 */}
      {!isHost && (
        <div style={{
          marginTop: '20px',
          fontSize: '14px',
          color: '#666',
        }}>
          방장만 게임을 다시 시작할 수 있습니다
        </div>
      )}
    </div>
  );
}