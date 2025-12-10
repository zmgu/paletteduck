import { GAME_CONSTANTS } from '../../../constants/gameConstants';
import type { GameSettings } from '../../../types/game.types';

interface GameSettingsProps {
  settings: GameSettings;
  isHost: boolean;
  currentPlayerCount: number;
  onSettingsChange: (newSettings: Partial<GameSettings>) => void;
}

export default function GameSettings({ settings, isHost, currentPlayerCount, onSettingsChange }: GameSettingsProps) {
  const minPlayers = Math.max(GAME_CONSTANTS.MIN_PLAYERS, currentPlayerCount);

  return (
    <div style={{ flex: 1, border: '1px solid #ccc', padding: '15px', borderRadius: '8px' }}>
      <div style={{ marginTop: '15px', display: 'flex', flexDirection: 'column', gap: '15px' }}>

        {/* 참가자 수 */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <label style={{ flex: 1 }}>참가자 수</label>
          <select
            value={settings.maxPlayers}
            onChange={(e) => onSettingsChange({ maxPlayers: parseInt(e.target.value) })}
            disabled={!isHost}
            style={{
              flex: 1,
              padding: '8px',
              borderRadius: '4px',
              cursor: isHost ? 'pointer' : 'not-allowed',
              opacity: isHost ? 1 : 0.7
            }}
          >
            {Array.from({ length: GAME_CONSTANTS.MAX_PLAYERS - minPlayers + 1 }, (_, i) => minPlayers + i).map(num => (
              <option key={num} value={num}>{num}명</option>
            ))}
          </select>
        </div>

        {/* 라운드 */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <label style={{ flex: 1 }}>라운드</label>
          <select
            value={settings.rounds}
            onChange={(e) => onSettingsChange({ rounds: parseInt(e.target.value) })}
            disabled={!isHost}
            style={{
              flex: 1,
              padding: '8px',
              borderRadius: '4px',
              cursor: isHost ? 'pointer' : 'not-allowed',
              opacity: isHost ? 1 : 0.7
            }}
          >
            {Array.from({ length: GAME_CONSTANTS.MAX_ROUNDS - GAME_CONSTANTS.MIN_ROUNDS + 1 }, (_, i) => GAME_CONSTANTS.MIN_ROUNDS + i).map(num => (
              <option key={num} value={num}>{num}라운드</option>
            ))}
          </select>
        </div>

        {/* 단어 선택지 */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <label style={{ flex: 1 }}>단어 선택지</label>
          <select
            value={settings.wordChoices}
            onChange={(e) => onSettingsChange({ wordChoices: parseInt(e.target.value) })}
            disabled={!isHost}
            style={{
              flex: 1,
              padding: '8px',
              borderRadius: '4px',
              cursor: isHost ? 'pointer' : 'not-allowed',
              opacity: isHost ? 1 : 0.7
            }}
          >
            {Array.from({ length: GAME_CONSTANTS.MAX_WORD_CHOICES - GAME_CONSTANTS.MIN_WORD_CHOICES + 1 }, (_, i) => GAME_CONSTANTS.MIN_WORD_CHOICES + i).map(num => (
              <option key={num} value={num}>{num}개</option>
            ))}
          </select>
        </div>

        {/* 그리기 시간 */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <label style={{ flex: 1 }}>그리기 시간</label>
          <select
            value={settings.drawTime}
            onChange={(e) => onSettingsChange({ drawTime: parseInt(e.target.value) })}
            disabled={!isHost}
            style={{
              flex: 1,
              padding: '8px',
              borderRadius: '4px',
              cursor: isHost ? 'pointer' : 'not-allowed',
              opacity: isHost ? 1 : 0.7
            }}
          >
            {GAME_CONSTANTS.DRAW_TIME_OPTIONS.map(time => (
              <option key={time} value={time}>{time}초</option>
            ))}
          </select>
        </div>
      </div>
    </div>
  );
}