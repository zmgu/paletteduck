import type { TurnInfo, Player } from '../../../types/game.types';
import { CANVAS_CONFIG } from '../../../constants/canvas.constants';

interface TurnResultProps {
  turnInfo: TurnInfo;
  players: Player[];
  canvasImageUrl: string;
}

export default function TurnResult({ turnInfo, players, canvasImageUrl }: TurnResultProps) {
  // 이번 턴에서 점수를 획득한 플레이어만 필터링 및 점수순 정렬
  const scoredPlayers = players
    .filter(p => (turnInfo.turnScores?.[p.playerId] || 0) > 0)
    .map(p => ({
      ...p,
      turnScore: turnInfo.turnScores?.[p.playerId] || 0
    }))
    .sort((a, b) => b.turnScore - a.turnScore);

  // 추천/비추천 통계
  const drawer = players.find(p => p.playerId === turnInfo.drawerId);
  const drawerTurnScore = turnInfo.turnScores?.[turnInfo.drawerId] || 0;

  return (
    <div style={{
      padding: '20px',
      maxWidth: '1200px',
      margin: '0 auto'
    }}>
      <h2 style={{
        textAlign: 'center',
        fontSize: '32px',
        marginBottom: '20px',
        color: '#2e7d32'
      }}>
        턴 결과
      </h2>

      <div style={{
        textAlign: 'center',
        fontSize: '24px',
        fontWeight: 'bold',
        marginBottom: '20px',
        color: '#666'
      }}>
        정답: {turnInfo.word}
      </div>

      <div style={{ display: 'flex', gap: '30px' }}>
        {/* 왼쪽: 그림 */}
        <div style={{ flex: 1 }}>
          <h3>출제된 그림</h3>
          <div style={{ marginTop: '10px', display: 'flex', justifyContent: 'center' }}>
            {canvasImageUrl ? (
              <img
                src={canvasImageUrl}
                alt="턴 결과 그림"
                style={{
                  border: '2px solid #ccc',
                  borderRadius: '8px',
                  width: CANVAS_CONFIG.WIDTH,
                  height: CANVAS_CONFIG.HEIGHT,
                }}
              />
            ) : (
              <div
                style={{
                  border: '2px solid #ccc',
                  borderRadius: '8px',
                  width: CANVAS_CONFIG.WIDTH,
                  height: CANVAS_CONFIG.HEIGHT,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  backgroundColor: '#f5f5f5',
                  color: '#999',
                }}
              >
                그림을 불러올 수 없습니다
              </div>
            )}
          </div>

          {/* 출제자 정보 */}
          {drawer && (
            <div style={{
              marginTop: '20px',
              padding: '20px',
              backgroundColor: '#e3f2fd',
              borderRadius: '8px',
              border: '2px solid #2196f3'
            }}>
              <div style={{ fontSize: '18px', fontWeight: 'bold', marginBottom: '10px' }}>
                🎨 출제자: {turnInfo.drawerNickname}
              </div>
              <div style={{ fontSize: '16px', color: '#666' }}>
                획득 점수: <strong style={{ color: '#2196f3' }}>+{drawerTurnScore}</strong>점
              </div>
              <div style={{ fontSize: '16px', color: '#666', marginTop: '5px' }}>
                👍 추천: {drawer.totalLikes || 0} / 👎 비추천: {drawer.totalDislikes || 0}
              </div>
            </div>
          )}
        </div>

        {/* 오른쪽: 랭킹 */}
        <div style={{ flex: 1 }}>
          <h3>이번 턴 득점 랭킹</h3>
          <div style={{ marginTop: '10px' }}>
            {scoredPlayers.length > 0 ? (
              scoredPlayers.map((player, index) => (
                <div
                  key={player.playerId}
                  style={{
                    padding: '15px 20px',
                    marginBottom: '10px',
                    backgroundColor: index === 0 ? '#fff3cd' : index === 1 ? '#d4edda' : '#f8f9fa',
                    border: index === 0 ? '2px solid #ffc107' : index === 1 ? '2px solid #28a745' : '1px solid #dee2e6',
                    borderRadius: '8px',
                    display: 'flex',
                    alignItems: 'center'
                  }}
                >
                  <span style={{
                    fontSize: '24px',
                    fontWeight: 'bold',
                    marginRight: '15px',
                    width: '50px',
                    textAlign: 'center'
                  }}>
                    {index === 0 ? '🥇' : index === 1 ? '🥈' : index === 2 ? '🥉' : `${index + 1}위`}
                  </span>
                  <div style={{ flex: 1 }}>
                    <div style={{
                      fontSize: '18px',
                      fontWeight: 'bold',
                      marginBottom: '5px'
                    }}>
                      {player.nickname}
                      {player.playerId === turnInfo.drawerId && ' 🎨'}
                    </div>
                    <div style={{ fontSize: '14px', color: '#666' }}>
                      획득: <strong style={{ color: '#4caf50' }}>+{player.turnScore}</strong>점
                      {' | '}
                      총점: {player.score}점
                    </div>
                  </div>
                  <div style={{ fontSize: '14px', color: '#666', textAlign: 'right' }}>
                    👍 {player.totalLikes || 0} / 👎 {player.totalDislikes || 0}
                  </div>
                </div>
              ))
            ) : (
              <div style={{
                padding: '40px',
                textAlign: 'center',
                color: '#999',
                fontSize: '16px'
              }}>
                이번 턴에 점수를 획득한 플레이어가 없습니다
              </div>
            )}
          </div>

          <div style={{
            marginTop: '20px',
            padding: '15px',
            backgroundColor: '#f0f0f0',
            borderRadius: '8px',
            textAlign: 'center',
            fontSize: '16px',
            color: '#666'
          }}>
            다음 턴이 곧 시작됩니다...
          </div>
        </div>
      </div>
    </div>
  );
}