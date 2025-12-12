import type { TurnInfo, Player } from '../../../types/game.types';
import { CANVAS_CONFIG } from '../../../constants/canvas.constants';

interface TurnResultProps {
  turnInfo: TurnInfo;
  players: Player[];
  canvasImageUrl: string;
  isSpectatorMidJoin?: boolean;  // 도중 참가 관전자 여부
}

export default function TurnResult({ turnInfo, players, canvasImageUrl, isSpectatorMidJoin }: TurnResultProps) {
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

  // 턴 종료 사유 메시지
  const getEndReasonMessage = () => {
    switch (turnInfo.turnEndReason) {
      case 'TIME_OUT':
        return { icon: '⏰', text: '시간 종료', color: '#ff9800' };
      case 'ALL_CORRECT':
        return { icon: '✅', text: '모든 참가자 정답!', color: '#4caf50' };
      case 'DRAWER_LEFT':
        return { icon: '👋', text: '출제자 퇴장', color: '#f44336' };
      default:
        return null;
    }
  };

  const endReasonInfo = getEndReasonMessage();

  return (
    <div style={{
      width: '100%',
      maxWidth: '1200px',
      maxHeight: '100%',
      display: 'flex',
      flexDirection: 'column'
    }}>
      <div style={{
        textAlign: 'center',
        padding: '15px',
        backgroundColor: '#f0f0f0',
        borderRadius: '8px',
        fontSize: '16px',
        color: '#666',
        marginBottom: '20px',
        flexShrink: 0
      }}>
        다음 턴이 곧 시작됩니다...
      </div>

      <div style={{ display: 'flex', gap: '30px', flex: 1, minHeight: 0, overflow: 'hidden' }}>
        {/* 왼쪽: 그림 */}
        <div style={{ flex: 1 }}>
          <h3>출제된 그림</h3>
          <div style={{ marginTop: '10px', display: 'flex', justifyContent: 'center', position: 'relative' }}>
            {isSpectatorMidJoin ? (
              <div
                style={{
                  border: '2px solid #ccc',
                  borderRadius: '8px',
                  width: CANVAS_CONFIG.WIDTH * 0.5,
                  height: CANVAS_CONFIG.HEIGHT * 0.5,
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '10px',
                  backgroundColor: 'rgba(255, 255, 255, 0.95)',
                  padding: '20px',
                  textAlign: 'center',
                }}
              >
                <div style={{ fontSize: '24px' }}>👀</div>
                <div style={{
                  fontSize: '14px',
                  fontWeight: 'bold',
                  color: '#ff9800',
                  lineHeight: '1.6',
                }}>
                  다음 턴부터 그림을 볼 수 있습니다
                </div>
              </div>
            ) : canvasImageUrl ? (
              <img
                src={canvasImageUrl}
                alt="턴 결과 그림"
                style={{
                  border: '2px solid #ccc',
                  borderRadius: '8px',
                  width: CANVAS_CONFIG.WIDTH * 0.5,
                  height: CANVAS_CONFIG.HEIGHT * 0.5,
                }}
              />
            ) : (
              <div
                style={{
                  border: '2px solid #ccc',
                  borderRadius: '8px',
                  width: CANVAS_CONFIG.WIDTH * 0.5,
                  height: CANVAS_CONFIG.HEIGHT * 0.5,
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
              marginTop: '15px',
              padding: '12px',
              backgroundColor: '#e3f2fd',
              borderRadius: '8px',
              border: '2px solid #2196f3'
            }}>
              <div style={{ fontSize: '14px', fontWeight: 'bold', marginBottom: '6px' }}>
                🎨 출제자: {turnInfo.drawerNickname}
              </div>
              <div style={{ fontSize: '12px', color: '#666' }}>
                획득 점수: <strong style={{ color: '#2196f3' }}>+{drawerTurnScore}</strong>점
              </div>
              <div style={{ fontSize: '12px', color: '#666', marginTop: '4px' }}>
                👍 추천: {drawer.totalLikes || 0} / 👎 비추천: {drawer.totalDislikes || 0}
              </div>
            </div>
          )}
        </div>

        {/* 오른쪽: 랭킹 */}
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', maxHeight: '100%' }}>
          <h3 style={{ marginBottom: '10px', flexShrink: 0 }}>이번 턴 득점 랭킹</h3>
          <div style={{
            flex: 1,
            overflowY: 'auto',
            overflowX: 'hidden',
            paddingRight: '4px'
          }}>
            {scoredPlayers.length > 0 ? (
              scoredPlayers.map((player, index) => (
                <div
                  key={player.playerId}
                  style={{
                    padding: '10px 15px',
                    marginBottom: '6px',
                    backgroundColor: index === 0 ? '#fff3cd' : index === 1 ? '#d4edda' : '#f8f9fa',
                    border: index === 0 ? '2px solid #ffc107' : index === 1 ? '2px solid #28a745' : '1px solid #dee2e6',
                    borderRadius: '8px',
                    display: 'flex',
                    alignItems: 'center'
                  }}
                >
                  <span style={{
                    fontSize: '18px',
                    fontWeight: 'bold',
                    marginRight: '10px',
                    width: '40px',
                    textAlign: 'center'
                  }}>
                    {index === 0 ? '🥇' : index === 1 ? '🥈' : index === 2 ? '🥉' : `${index + 1}위`}
                  </span>
                  <div style={{ flex: 1 }}>
                    <div style={{
                      fontSize: '14px',
                      fontWeight: 'bold',
                      marginBottom: '3px'
                    }}>
                      {player.nickname}
                      {player.playerId === turnInfo.drawerId && ' 🎨'}
                    </div>
                    <div style={{ fontSize: '12px', color: '#666' }}>
                      획득: <strong style={{ color: '#4caf50' }}>+{player.turnScore}</strong>점
                    </div>
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
        </div>
      </div>
    </div>
  );
}