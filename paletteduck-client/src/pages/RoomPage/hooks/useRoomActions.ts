import { wsClient } from '../../../utils/wsClient';
import { WS_DESTINATIONS } from '../../../constants/wsDestinations';
import type { PlayerRole, GameSettings } from '../../../types/game.types';

export const useRoomActions = (roomId: string, currentPlayerId: string) => {
  
  // 준비 완료 토글
  const toggleReady = () => {
    wsClient.send(WS_DESTINATIONS.ROOM_READY(roomId), currentPlayerId);
  };

  // 역할 변경
  const changeRole = (newRole: PlayerRole) => {
    wsClient.send(WS_DESTINATIONS.ROOM_ROLE(roomId), {
      playerId: currentPlayerId,
      newRole
    });
  };

  // 게임 설정 변경
  const updateSettings = (newSettings: Partial<GameSettings>, currentSettings: GameSettings) => {
    const updatedSettings = { ...currentSettings, ...newSettings };
    
    wsClient.send(WS_DESTINATIONS.ROOM_SETTINGS(roomId), {
      playerId: currentPlayerId,
      settings: updatedSettings
    });
  };

  // 게임 시작
  const startGame = () => {
    wsClient.send(WS_DESTINATIONS.ROOM_START(roomId), currentPlayerId);
  };

  // 채팅 전송
  const sendChat = (message: string, nickname: string) => {
    wsClient.send(WS_DESTINATIONS.ROOM_CHAT(roomId), {
      playerId: currentPlayerId,
      nickname,
      message: message.trim(),
      type: 'NORMAL'
    });
  };

  // 초대 코드 복사
  const copyInviteCode = (inviteCode: string) => {
    const inviteUrl = `${window.location.origin}/room/${inviteCode}`;
    navigator.clipboard.writeText(inviteUrl);
    alert('초대 코드가 복사되었습니다!');
  };

  return {
    toggleReady,
    changeRole,
    updateSettings,
    startGame,
    sendChat,
    copyInviteCode,
  };
};