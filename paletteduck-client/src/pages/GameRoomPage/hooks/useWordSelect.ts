import { useMemo } from 'react';
import { wsClient } from '../../../utils/wsClient';
import { WS_DESTINATIONS } from '../../../constants/wsDestinations';
import { getPlayerInfo } from '../../../utils/apiClient';

export const useWordSelect = (roomId: string) => {
  const playerInfo = useMemo(() => getPlayerInfo(), []);

  const selectWord = (word: string) => {
    if (!playerInfo || !roomId) return;

    wsClient.send(WS_DESTINATIONS.GAME_WORD_SELECT(roomId), {
      playerId: playerInfo.playerId,
      word,
    });
  };

  return { selectWord };
};