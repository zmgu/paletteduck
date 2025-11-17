import { GAME_CONSTANTS } from '../../../constants/gameConstants';
import type { GameSettings } from '../../../types/game.types';

interface GameSettingsProps {
  settings: GameSettings;
  isHost: boolean;
  onSettingsChange: (newSettings: Partial<GameSettings>) => void;
}

export default function GameSettings({ settings, isHost, onSettingsChange }: GameSettingsProps) {
  return (
    <div style={{ flex: 1, border: '1px solid #ccc', padding: '15px', borderRadius: '8px' }}>
      <h2>게임 설정 {isHost && <span style={{ fontSize: '14px', color: '#666' }}>(방장)</span>}</h2>
      <div style={{ marginTop: '15px', display: 'flex', flexDirection: 'column', gap: '15px' }}>
        
        {/* 참가자 수 */}
        <div>
          <label style={{ display: 'block', marginBottom: '5px' }}>
            참가자 수: {settings.maxPlayers}명
          </label>
          {isHost && (
            <input
              type="range"
              min={GAME_CONSTANTS.MIN_PLAYERS}
              max={GAME_CONSTANTS.MAX_PLAYERS}
              value={settings.maxPlayers}
              onChange={(e) => onSettingsChange({ maxPlayers: parseInt(e.target.value) })}
              style={{ width: '100%' }}
            />
          )}
        </div>

        {/* 라운드 */}
        <div>
          <label style={{ display: 'block', marginBottom: '5px' }}>
            라운드: {settings.rounds}라운드
          </label>
          {isHost && (
            <input
              type="range"
              min={settings.mode === 'CUSTOM' ? GAME_CONSTANTS.MIN_ROUNDS_CUSTOM : GAME_CONSTANTS.MIN_ROUNDS}
              max={settings.mode === 'CUSTOM' ? GAME_CONSTANTS.MAX_ROUNDS_CUSTOM : GAME_CONSTANTS.MAX_ROUNDS}
              value={settings.rounds}
              onChange={(e) => onSettingsChange({ rounds: parseInt(e.target.value) })}
              style={{ width: '100%' }}
            />
          )}
        </div>

        {/* 단어 선택지 */}
        <div>
          <label style={{ display: 'block', marginBottom: '5px' }}>
            단어 선택지: {settings.wordChoices}개
          </label>
          {isHost && settings.mode === 'NORMAL' ? (
            <input
              type="range"
              min={GAME_CONSTANTS.MIN_WORD_CHOICES}
              max={GAME_CONSTANTS.MAX_WORD_CHOICES}
              value={settings.wordChoices}
              onChange={(e) => onSettingsChange({ wordChoices: parseInt(e.target.value) })}
              style={{ width: '100%' }}
            />
          ) : settings.mode === 'CUSTOM' ? (
            <span> (고정)</span>
          ) : null}
        </div>

        {/* 그리기 시간 */}
        <div>
          <label style={{ display: 'block', marginBottom: '5px' }}>
            그리기 시간: {settings.drawTime}초
          </label>
          {isHost && (
            <select
              value={settings.drawTime}
              onChange={(e) => onSettingsChange({ drawTime: parseInt(e.target.value) })}
              style={{ width: '100%', padding: '8px' }}
            >
              {GAME_CONSTANTS.DRAW_TIME_OPTIONS.map(time => (
                <option key={time} value={time}>{time}초</option>
              ))}
            </select>
          )}
        </div>

        {/* 모드 */}
        <div>
          <label style={{ display: 'block', marginBottom: '5px' }}>
            모드: {settings.mode === 'NORMAL' ? '일반' : '커스텀'}
          </label>
          {isHost && (
            <select
              value={settings.mode}
              onChange={(e) => {
                const mode = e.target.value as 'NORMAL' | 'CUSTOM';
                onSettingsChange({ 
                  mode,
                  rounds: mode === 'CUSTOM' ? GAME_CONSTANTS.MIN_ROUNDS_CUSTOM : settings.rounds,
                  wordChoices: mode === 'CUSTOM' ? GAME_CONSTANTS.WORD_CHOICES_CUSTOM : settings.wordChoices
                });
              }}
              style={{ width: '100%', padding: '8px' }}
            >
              <option value="NORMAL">일반 모드</option>
              <option value="CUSTOM">커스텀 모드</option>
            </select>
          )}
        </div>
      </div>
    </div>
  );
}