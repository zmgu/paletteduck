import { useState, useRef, useMemo, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import GameHeader from './GameRoomPage/components/GameHeader';
import WordSelect from './GameRoomPage/components/WordSelect';
import DrawingArea from './GameRoomPage/components/DrawingArea';
import Canvas from './GameRoomPage/components/Canvas';
import CanvasToolbar from './GameRoomPage/components/Canvas/CanvasToolbar';
import ChatBox from './GameRoomPage/components/ChatBox';
import TurnResult from './GameRoomPage/components/TurnResult';
import SpectatorList from './RoomPage/components/SpectatorList';
import PlayerList from './RoomPage/components/PlayerList';
import type { GameState, RoomInfo, ChatMessage, GamePhase } from '../types/game.types';
import type { CanvasHandle } from './GameRoomPage/components/Canvas/Canvas';
import type { Tool } from '../types/drawing.types';
import logo from '../assets/logo.png';

// 플레이어 Mock 데이터
const mockPlayers = [
  {
    playerId: 'player1',
    nickname: '나',
    score: 150,
    isCorrect: false,
    totalLikes: 3,
    totalDislikes: 0,
  },
  {
    playerId: 'player2',
    nickname: '그림쟁이',
    score: 200,
    isCorrect: false,
    totalLikes: 5,
    totalDislikes: 1,
  },
  {
    playerId: 'player3',
    nickname: '정답맞춤',
    score: 180,
    isCorrect: true,
    totalLikes: 2,
    totalDislikes: 0,
  },
  {
    playerId: 'player4',
    nickname: '플레이어4',
    score: 120,
    isCorrect: false,
    totalLikes: 1,
    totalDislikes: 2,
  },
  {
    playerId: 'player5',
    nickname: '아티스트',
    score: 170,
    isCorrect: false,
    totalLikes: 4,
    totalDislikes: 0,
  },
  {
    playerId: 'player6',
    nickname: '천재화가',
    score: 190,
    isCorrect: true,
    totalLikes: 6,
    totalDislikes: 1,
  },
  {
    playerId: 'player7',
    nickname: '미술가',
    score: 160,
    isCorrect: false,
    totalLikes: 3,
    totalDislikes: 0,
  },
  {
    playerId: 'player8',
    nickname: '그림왕',
    score: 210,
    isCorrect: true,
    totalLikes: 7,
    totalDislikes: 0,
  },
  {
    playerId: 'player9',
    nickname: '붓장인',
    score: 140,
    isCorrect: false,
    totalLikes: 2,
    totalDislikes: 0,
  },
  {
    playerId: 'player10',
    nickname: '스케치마스터',
    score: 175,
    isCorrect: true,
    totalLikes: 5,
    totalDislikes: 1,
  },
  {
    playerId: 'player11',
    nickname: '색칠왕',
    score: 165,
    isCorrect: false,
    totalLikes: 4,
    totalDislikes: 0,
  },
  {
    playerId: 'player12',
    nickname: '드로잉고수',
    score: 195,
    isCorrect: true,
    totalLikes: 6,
    totalDislikes: 0,
  },
  {
    playerId: 'player13',
    nickname: '펜촉달인',
    score: 130,
    isCorrect: false,
    totalLikes: 2,
    totalDislikes: 1,
  },
  {
    playerId: 'player14',
    nickname: '크레용신짱',
    score: 185,
    isCorrect: true,
    totalLikes: 5,
    totalDislikes: 0,
  },
  {
    playerId: 'player15',
    nickname: '물감왕자',
    score: 155,
    isCorrect: false,
    totalLikes: 3,
    totalDislikes: 1,
  },
  {
    playerId: 'player16',
    nickname: '낙서의신',
    score: 200,
    isCorrect: true,
    totalLikes: 7,
    totalDislikes: 1,
  },
  {
    playerId: 'player17',
    nickname: '그림귀재',
    score: 145,
    isCorrect: false,
    totalLikes: 3,
    totalDislikes: 0,
  },
  {
    playerId: 'player18',
    nickname: '아트킹',
    score: 205,
    isCorrect: true,
    totalLikes: 8,
    totalDislikes: 0,
  },
];

const mockRoomInfo: RoomInfo = {
  roomId: 'preview',
  inviteCode: 'ABC123',
  status: 'PLAYING',
  settings: {
    maxPlayers: 18,
    rounds: 3,
    wordChoices: 3,
    drawTime: 80,
    maxSpectators: 10,
  },
  players: [
    {
      playerId: 'player1',
      nickname: '나',
      host: true,
      ready: true,
      role: 'PLAYER',
      score: 150,
      totalLikes: 3,
      totalDislikes: 0,
    },
    {
      playerId: 'player2',
      nickname: '그림쟁이',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 200,
      totalLikes: 5,
      totalDislikes: 1,
    },
    {
      playerId: 'player3',
      nickname: '정답맞춤',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 180,
      totalLikes: 2,
      totalDislikes: 0,
    },
    {
      playerId: 'player4',
      nickname: '플레이어4',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 120,
      totalLikes: 1,
      totalDislikes: 2,
    },
    {
      playerId: 'player5',
      nickname: '아티스트',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 170,
      totalLikes: 4,
      totalDislikes: 0,
    },
    {
      playerId: 'player6',
      nickname: '천재화가',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 190,
      totalLikes: 6,
      totalDislikes: 1,
    },
    {
      playerId: 'player7',
      nickname: '미술가',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 160,
      totalLikes: 3,
      totalDislikes: 0,
    },
    {
      playerId: 'player8',
      nickname: '그림왕',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 210,
      totalLikes: 7,
      totalDislikes: 0,
    },
    {
      playerId: 'player9',
      nickname: '붓장인',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 140,
      totalLikes: 2,
      totalDislikes: 0,
    },
    {
      playerId: 'player10',
      nickname: '스케치마스터',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 175,
      totalLikes: 5,
      totalDislikes: 1,
    },
    {
      playerId: 'player11',
      nickname: '색칠왕',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 165,
      totalLikes: 4,
      totalDislikes: 0,
    },
    {
      playerId: 'player12',
      nickname: '드로잉고수',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 195,
      totalLikes: 6,
      totalDislikes: 0,
    },
    {
      playerId: 'player13',
      nickname: '펜촉달인',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 130,
      totalLikes: 2,
      totalDislikes: 1,
    },
    {
      playerId: 'player14',
      nickname: '크레용신짱',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 185,
      totalLikes: 5,
      totalDislikes: 0,
    },
    {
      playerId: 'player15',
      nickname: '물감왕자',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 155,
      totalLikes: 3,
      totalDislikes: 1,
    },
    {
      playerId: 'player16',
      nickname: '낙서의신',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 200,
      totalLikes: 7,
      totalDislikes: 1,
    },
    {
      playerId: 'player17',
      nickname: '그림귀재',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 145,
      totalLikes: 3,
      totalDislikes: 0,
    },
    {
      playerId: 'player18',
      nickname: '아트킹',
      host: false,
      ready: true,
      role: 'PLAYER',
      score: 205,
      totalLikes: 8,
      totalDislikes: 0,
    },
    {
      playerId: 'spectator1',
      nickname: '관전자1',
      host: false,
      ready: false,
      role: 'SPECTATOR',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'spectator2',
      nickname: '관전자2',
      host: false,
      ready: false,
      role: 'SPECTATOR',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'spectator3',
      nickname: '관전자3',
      host: false,
      ready: false,
      role: 'SPECTATOR',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'spectator4',
      nickname: '구경꾼',
      host: false,
      ready: false,
      role: 'SPECTATOR',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'spectator5',
      nickname: '방구석관전러',
      host: false,
      ready: false,
      role: 'SPECTATOR',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'spectator6',
      nickname: '지켜보는자',
      host: false,
      ready: false,
      role: 'SPECTATOR',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'spectator7',
      nickname: '몰래보기',
      host: false,
      ready: false,
      role: 'SPECTATOR',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'spectator8',
      nickname: '관람객A',
      host: false,
      ready: false,
      role: 'SPECTATOR',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'spectator9',
      nickname: '눈팅러',
      host: false,
      ready: false,
      role: 'SPECTATOR',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
    {
      playerId: 'spectator10',
      nickname: '조용한관전자',
      host: false,
      ready: false,
      role: 'SPECTATOR',
      score: 0,
      totalLikes: 0,
      totalDislikes: 0,
    },
  ],
};

const mockMessages: ChatMessage[] = [
  {
    playerId: 'system',
    nickname: '시스템',
    message: '게임이 시작되었습니다!',
    type: 'SYSTEM',
    timestamp: Date.now() - 60000,
  },
  {
    playerId: 'player1',
    nickname: '나',
    message: '바나나?',
    type: 'NORMAL',
    timestamp: Date.now() - 58000,
  },
  {
    playerId: 'player4',
    nickname: '플레이어4',
    message: '오렌지',
    type: 'NORMAL',
    timestamp: Date.now() - 56000,
  },
  {
    playerId: 'player5',
    nickname: '아티스트',
    message: '포도인가요?',
    type: 'NORMAL',
    timestamp: Date.now() - 54000,
  },
  {
    playerId: 'player2',
    nickname: '그림쟁이',
    message: '딸기!',
    type: 'NORMAL',
    timestamp: Date.now() - 52000,
  },
  {
    playerId: 'player6',
    nickname: '천재화가',
    message: '멜론?',
    type: 'NORMAL',
    timestamp: Date.now() - 50000,
  },
  {
    playerId: 'player1',
    nickname: '나',
    message: '수박인가',
    type: 'NORMAL',
    timestamp: Date.now() - 48000,
  },
  {
    playerId: 'player4',
    nickname: '플레이어4',
    message: '키위',
    type: 'NORMAL',
    timestamp: Date.now() - 46000,
  },
  {
    playerId: 'player5',
    nickname: '아티스트',
    message: '망고',
    type: 'NORMAL',
    timestamp: Date.now() - 44000,
  },
  {
    playerId: 'player3',
    nickname: '정답맞춤',
    message: '복숭아?',
    type: 'NORMAL',
    timestamp: Date.now() - 42000,
  },
  {
    playerId: 'player6',
    nickname: '천재화가',
    message: '자두',
    type: 'NORMAL',
    timestamp: Date.now() - 40000,
  },
  {
    playerId: 'player2',
    nickname: '그림쟁이',
    message: '레몬',
    type: 'NORMAL',
    timestamp: Date.now() - 38000,
  },
  {
    playerId: 'player1',
    nickname: '나',
    message: '귤',
    type: 'NORMAL',
    timestamp: Date.now() - 36000,
  },
  {
    playerId: 'player4',
    nickname: '플레이어4',
    message: '배',
    type: 'NORMAL',
    timestamp: Date.now() - 34000,
  },
  {
    playerId: 'player5',
    nickname: '아티스트',
    message: '감',
    type: 'NORMAL',
    timestamp: Date.now() - 32000,
  },
  {
    playerId: 'player6',
    nickname: '천재화가',
    message: '파인애플',
    type: 'NORMAL',
    timestamp: Date.now() - 30000,
  },
  {
    playerId: 'player3',
    nickname: '정답맞춤',
    message: '체리?',
    type: 'NORMAL',
    timestamp: Date.now() - 28000,
  },
  {
    playerId: 'player2',
    nickname: '그림쟁이',
    message: '블루베리',
    type: 'NORMAL',
    timestamp: Date.now() - 26000,
  },
  {
    playerId: 'player1',
    nickname: '나',
    message: '라즈베리',
    type: 'NORMAL',
    timestamp: Date.now() - 24000,
  },
  {
    playerId: 'player4',
    nickname: '플레이어4',
    message: '석류',
    type: 'NORMAL',
    timestamp: Date.now() - 22000,
  },
  {
    playerId: 'player5',
    nickname: '아티스트',
    message: '무화과',
    type: 'NORMAL',
    timestamp: Date.now() - 20000,
  },
  {
    playerId: 'player6',
    nickname: '천재화가',
    message: '용과',
    type: 'NORMAL',
    timestamp: Date.now() - 18000,
  },
  {
    playerId: 'player2',
    nickname: '그림쟁이',
    message: '리치',
    type: 'NORMAL',
    timestamp: Date.now() - 16000,
  },
  {
    playerId: 'player3',
    nickname: '정답맞춤',
    message: '사과!',
    type: 'CORRECT',
    timestamp: Date.now() - 15000,
  },
  {
    playerId: 'system',
    nickname: '시스템',
    message: '정답맞춤 님이 정답을 맞췄습니다!',
    type: 'SYSTEM',
    timestamp: Date.now() - 14000,
  },
  {
    playerId: 'player1',
    nickname: '나',
    message: '아 사과였구나',
    type: 'NORMAL',
    timestamp: Date.now() - 12000,
  },
  {
    playerId: 'player4',
    nickname: '플레이어4',
    message: '축하해요!',
    type: 'NORMAL',
    timestamp: Date.now() - 10000,
  },
  {
    playerId: 'player5',
    nickname: '아티스트',
    message: '다음엔 내가!',
    type: 'NORMAL',
    timestamp: Date.now() - 8000,
  },
  {
    playerId: 'player6',
    nickname: '천재화가',
    message: 'ㅋㅋㅋ 잘했어요',
    type: 'NORMAL',
    timestamp: Date.now() - 6000,
  },
  {
    playerId: 'player2',
    nickname: '그림쟁이',
    message: '다음 라운드 기대됩니다',
    type: 'NORMAL',
    timestamp: Date.now() - 4000,
  },
  {
    playerId: 'player7',
    nickname: '미술가',
    message: '재밌네요 ㅎㅎ',
    type: 'NORMAL',
    timestamp: Date.now() - 3800,
  },
  {
    playerId: 'player8',
    nickname: '그림왕',
    message: '다들 실력이 좋으시네요',
    type: 'NORMAL',
    timestamp: Date.now() - 3600,
  },
  {
    playerId: 'player1',
    nickname: '나',
    message: '이번엔 맞춰야지',
    type: 'NORMAL',
    timestamp: Date.now() - 3400,
  },
  {
    playerId: 'player3',
    nickname: '정답맞춤',
    message: '힌트 잘 봐야 해요',
    type: 'NORMAL',
    timestamp: Date.now() - 3200,
  },
  {
    playerId: 'player5',
    nickname: '아티스트',
    message: '집중하자!',
    type: 'NORMAL',
    timestamp: Date.now() - 3000,
  },
  {
    playerId: 'player4',
    nickname: '플레이어4',
    message: '화이팅',
    type: 'NORMAL',
    timestamp: Date.now() - 2800,
  },
  {
    playerId: 'player6',
    nickname: '천재화가',
    message: '모두 파이팅!',
    type: 'NORMAL',
    timestamp: Date.now() - 2600,
  },
  {
    playerId: 'player7',
    nickname: '미술가',
    message: '다음 문제 뭐지',
    type: 'NORMAL',
    timestamp: Date.now() - 2400,
  },
  {
    playerId: 'player8',
    nickname: '그림왕',
    message: '기대됩니다!',
    type: 'NORMAL',
    timestamp: Date.now() - 2200,
  },
  {
    playerId: 'player2',
    nickname: '그림쟁이',
    message: 'ㄱㄱ',
    type: 'NORMAL',
    timestamp: Date.now() - 2000,
  },
];

// 각 페이즈별 Mock 데이터 생성 함수
const createMockGameState = (phase: GamePhase): GameState => {
  const baseState = {
    roomId: 'preview',
    currentRound: 2,
    totalRounds: 3,
    phase,
    phaseStartTime: Date.now(),
    drawTime: 80,
    turnOrder: ['player1', 'player2', 'player3', 'player4'],
    players: mockPlayers,
  };

  switch (phase) {
    case 'COUNTDOWN':
      return {
        ...baseState,
        currentTurn: {
          turnNumber: 5,
          drawerId: 'player2',
          drawerNickname: '그림쟁이',
          word: null,
          wordChoices: [],
          timeLeft: 3,
          correctPlayerIds: [],
          hintLevel: 0,
          currentHint: null,
          hintArray: null,
          revealedChosungPositions: [],
          revealedLetterPositions: [],
          votes: {},
          turnScores: {},
        },
      };

    case 'WORD_SELECT':
      return {
        ...baseState,
        currentTurn: {
          turnNumber: 5,
          drawerId: 'player2',
          drawerNickname: '그림쟁이',
          word: null,
          wordChoices: ['사과', '바나나', '포도'],
          timeLeft: 10,
          correctPlayerIds: [],
          hintLevel: 0,
          currentHint: null,
          hintArray: null,
          revealedChosungPositions: [],
          revealedLetterPositions: [],
          votes: {},
          turnScores: {},
        },
      };

    case 'DRAWING':
      return {
        ...baseState,
        currentTurn: {
          turnNumber: 5,
          drawerId: 'player2',
          drawerNickname: '그림쟁이',
          word: '사과',
          wordChoices: [],
          timeLeft: 45,
          correctPlayerIds: ['player3'],
          hintLevel: 1,
          currentHint: '사_',
          hintArray: ['사', '_'],
          revealedChosungPositions: [0],
          revealedLetterPositions: [],
          votes: {
            player1: 'NONE',
            player3: 'LIKE',
            player4: 'NONE',
          },
          turnScores: {},
        },
      };

    case 'TURN_RESULT':
      return {
        ...baseState,
        currentTurn: {
          turnNumber: 5,
          drawerId: 'player2',
          drawerNickname: '그림쟁이',
          word: '사과',
          wordChoices: [],
          timeLeft: 0,
          correctPlayerIds: ['player3', 'player1'],
          hintLevel: 2,
          currentHint: '사과',
          hintArray: ['사', '과'],
          revealedChosungPositions: [0, 1],
          revealedLetterPositions: [0, 1],
          votes: {
            player1: 'LIKE',
            player3: 'LIKE',
            player4: 'DISLIKE',
          },
          turnScores: {
            player1: 80,
            player2: 50,
            player3: 100,
            player4: 60,
            player5: 90,
            player6: 95,
            player7: 70,
            player8: 110,
            player9: 55,
            player10: 85,
            player11: 75,
            player12: 98,
            player13: 45,
            player14: 88,
            player15: 65,
            player16: 100,
            player17: 58,
            player18: 105,
          },
          turnEndReason: 'TIME_OUT',
        },
      };

    case 'ROUND_END':
      return {
        ...baseState,
        currentTurn: {
          turnNumber: 5,
          drawerId: 'player2',
          drawerNickname: '그림쟁이',
          word: '사과',
          wordChoices: [],
          timeLeft: 0,
          correctPlayerIds: ['player3', 'player1'],
          hintLevel: 2,
          currentHint: '사과',
          hintArray: ['사', '과'],
          revealedChosungPositions: [0, 1],
          revealedLetterPositions: [0, 1],
          votes: {},
          turnScores: {},
        },
      };

    case 'GAME_END':
      return {
        ...baseState,
        currentRound: 3,
        currentTurn: null,
      };

    default:
      return baseState as GameState;
  }
};

export default function GameRoomPreview() {
  const navigate = useNavigate();
  const [currentPhase, setCurrentPhase] = useState<GamePhase>('DRAWING');
  const [gameState, setGameState] = useState<GameState>(createMockGameState('DRAWING'));
  const [messages, setMessages] = useState<ChatMessage[]>(mockMessages);
  const [timeLeft, setTimeLeft] = useState(45);
  const [currentVote, setCurrentVote] = useState<'LIKE' | 'DISLIKE' | 'NONE'>('NONE');
  const [canvasImageUrl] = useState<string>('');
  const [previewRole, setPreviewRole] = useState<'drawer' | 'guesser'>('drawer');
  const [tool, setTool] = useState<Tool>('pen');
  const [color, setColor] = useState('#000000');
  const [brushWidth, setBrushWidth] = useState(8);
  const [clearSignal, setClearSignal] = useState(0);
  const [isPlayerHovered, setIsPlayerHovered] = useState(false);
  const [isSpectatorHovered, setIsSpectatorHovered] = useState(false);
  const [isPlayerAtTop, setIsPlayerAtTop] = useState(true);
  const [isPlayerAtBottom, setIsPlayerAtBottom] = useState(false);
  const [isSpectatorAtTop, setIsSpectatorAtTop] = useState(true);
  const [isSpectatorAtBottom, setIsSpectatorAtBottom] = useState(false);
  const canvasRef = useRef<CanvasHandle>(null);
  const playerListRef = useRef<HTMLUListElement>(null);
  const spectatorListRef = useRef<HTMLDivElement>(null);

  const playerInfo = useMemo(() => ({
    playerId: 'player1',
    nickname: '나',
    token: 'mock-token',
  }), []);

  const handlePhaseChange = (phase: GamePhase) => {
    setCurrentPhase(phase);
    setGameState(createMockGameState(phase));

    // 페이즈별 시간 초기값 설정
    switch (phase) {
      case 'COUNTDOWN':
        setTimeLeft(3);
        break;
      case 'WORD_SELECT':
        setTimeLeft(10);
        break;
      case 'DRAWING':
        setTimeLeft(45);
        break;
      default:
        setTimeLeft(0);
    }
  };

  // 스크롤 위치 감지
  useEffect(() => {
    const checkPlayerScroll = () => {
      if (playerListRef.current) {
        const { scrollTop, scrollHeight, clientHeight } = playerListRef.current;
        setIsPlayerAtTop(scrollTop <= 1);
        setIsPlayerAtBottom(scrollTop + clientHeight >= scrollHeight - 1);
      }
    };

    const checkSpectatorScroll = () => {
      if (spectatorListRef.current) {
        const { scrollTop, scrollHeight, clientHeight } = spectatorListRef.current;
        setIsSpectatorAtTop(scrollTop <= 1);
        setIsSpectatorAtBottom(scrollTop + clientHeight >= scrollHeight - 1);
      }
    };

    checkPlayerScroll();
    checkSpectatorScroll();

    const playerList = playerListRef.current;
    const spectatorList = spectatorListRef.current;

    if (playerList) {
      playerList.addEventListener('scroll', checkPlayerScroll);
    }
    if (spectatorList) {
      spectatorList.addEventListener('scroll', checkSpectatorScroll);
    }

    return () => {
      if (playerList) {
        playerList.removeEventListener('scroll', checkPlayerScroll);
      }
      if (spectatorList) {
        spectatorList.removeEventListener('scroll', checkSpectatorScroll);
      }
    };
  }, []);

  // 프리뷰 모드에서는 previewRole로 결정
  const isDrawer = previewRole === 'drawer';
  const currentPlayer = gameState.players?.find(p => p.playerId === playerInfo?.playerId);
  const isCorrect = currentPlayer?.isCorrect || false;

  const handleSendMessage = (message: string) => {
    const newMessage: ChatMessage = {
      playerId: playerInfo.playerId,
      nickname: playerInfo.nickname,
      message,
      type: 'NORMAL',
      timestamp: Date.now(),
    };
    setMessages([...messages, newMessage]);
  };

  const handleVote = (voteType: 'LIKE' | 'DISLIKE' | 'NONE') => {
    setCurrentVote(voteType);
    console.log('Vote:', voteType);
  };

  const handleClearCanvas = () => {
    setClearSignal(prev => prev + 1);
    console.log('Clear canvas');
  };

  const handlePlayerListScroll = (direction: 'up' | 'down') => {
    if (playerListRef.current) {
      const scrollAmount = 80;
      const currentScroll = playerListRef.current.scrollTop;
      const newScroll = direction === 'down'
        ? currentScroll + scrollAmount
        : Math.max(0, currentScroll - scrollAmount);

      playerListRef.current.scrollTo({
        top: newScroll,
        behavior: 'smooth'
      });
    }
  };

  const handleSpectatorListScroll = (direction: 'up' | 'down') => {
    if (spectatorListRef.current) {
      const scrollAmount = 80;
      const currentScroll = spectatorListRef.current.scrollTop;
      const newScroll = direction === 'down'
        ? currentScroll + scrollAmount
        : Math.max(0, currentScroll - scrollAmount);

      spectatorListRef.current.scrollTo({
        top: newScroll,
        behavior: 'smooth'
      });
    }
  };

  // 순위 계산 (GAME_END용)
  const sortedPlayers = [...(gameState.players || [])].sort((a, b) => b.score - a.score);
  const bestArtist = gameState.players?.reduce((best, player) => {
    const playerLikes = player.totalLikes || 0;
    const bestLikes = best?.totalLikes || 0;
    return playerLikes > bestLikes ? player : best;
  }, gameState.players?.[0]);

  return (
    <div style={{ margin: '0 auto' }}>
      {/* 통합 레이아웃을 사용하지 않는 페이즈에서만 GameHeader 표시 */}
      {gameState.phase !== 'GAME_END' && gameState.phase !== 'DRAWING' && gameState.phase !== 'COUNTDOWN' && gameState.phase !== 'WORD_SELECT' && gameState.phase !== 'TURN_RESULT' && gameState.phase !== 'ROUND_END' && (
        <GameHeader gameState={gameState} timeLeft={timeLeft} isDrawer={isDrawer} />
      )}

      {((gameState.phase === 'WORD_SELECT' || gameState.phase === 'COUNTDOWN' || gameState.phase === 'DRAWING' || gameState.phase === 'TURN_RESULT' || gameState.phase === 'ROUND_END') && gameState.currentTurn) || gameState.phase === 'GAME_END' ? (
        <div style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'flex-start',
          width: '1310px',
          margin: '10px auto 0'
        }}>
          {/* 헤더 영역 - 모든 페이즈에서 동일한 높이 유지 */}
          <div style={{
            width: '100%',
            height: '70px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            backgroundColor: 'transparent',
            borderRadius: '8px 8px 0 0',
            position: 'relative',
            flexShrink: 0
          }}>
            {/* 왼쪽: 로고 */}
            <div style={{
              position: 'absolute',
              left: '20px',
              top: '50%',
              transform: 'translateY(-50%)',
            }}>
              <img
                src={logo}
                alt="PaletteDuck Logo"
                style={{
                  height: '50px',
                  width: 'auto'
                }}
              />
            </div>

            {gameState.currentTurn && (
              <>
                {/* 오른쪽: 역할 전환 버튼 */}
                <div style={{
                  position: 'absolute',
                  right: '20px',
                  top: '50%',
                  transform: 'translateY(-50%)',
                  display: 'flex',
                  gap: '8px'
                }}>
                  <button
                    onClick={() => setPreviewRole('drawer')}
                    style={{
                      padding: '8px 16px',
                      fontSize: '14px',
                      fontWeight: 'bold',
                      backgroundColor: previewRole === 'drawer' ? 'rgba(0, 0, 0, 0.3)' : 'rgba(255, 255, 255, 0.3)',
                      color: '#fff',
                      border: '2px solid rgba(255, 255, 255, 0.5)',
                      borderRadius: '4px',
                      cursor: 'pointer',
                      transition: 'all 0.2s'
                    }}
                    onMouseEnter={(e) => {
                      if (previewRole !== 'drawer') {
                        e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.4)';
                      }
                    }}
                    onMouseLeave={(e) => {
                      if (previewRole !== 'drawer') {
                        e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.3)';
                      }
                    }}
                  >
                    🎨 출제자
                  </button>
                  <button
                    onClick={() => setPreviewRole('guesser')}
                    style={{
                      padding: '8px 16px',
                      fontSize: '14px',
                      fontWeight: 'bold',
                      backgroundColor: previewRole === 'guesser' ? 'rgba(0, 0, 0, 0.3)' : 'rgba(255, 255, 255, 0.3)',
                      color: '#fff',
                      border: '2px solid rgba(255, 255, 255, 0.5)',
                      borderRadius: '4px',
                      cursor: 'pointer',
                      transition: 'all 0.2s'
                    }}
                    onMouseEnter={(e) => {
                      if (previewRole !== 'guesser') {
                        e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.4)';
                      }
                    }}
                    onMouseLeave={(e) => {
                      if (previewRole !== 'guesser') {
                        e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.3)';
                      }
                    }}
                  >
                    🎯 참가자
                  </button>
                </div>
              </>
            )}
          </div>

          {/* 서브 헤더 영역 - 모든 페이즈에서 동일한 높이 유지 */}
          <div style={{
            width: '100%',
            height: '50px',
            display: 'grid',
            gridTemplateColumns: '200px 810px 300px',
            alignItems: 'center',
            backgroundColor: '#8CA9FF',
            borderBottom: 'none',
            boxSizing: 'border-box',
            flexShrink: 0
          }}>
            {gameState.phase === 'GAME_END' ? (
              <>
                {/* 왼쪽: 빈 공간 */}
                <div></div>

                {/* 중앙: 게임 종료 문구 */}
                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}>
                  <div style={{
                    fontSize: '24px',
                    fontWeight: 'bold',
                    color: '#fff'
                  }}>
                    🎉 게임 종료! 🎉
                  </div>
                </div>

                {/* 오른쪽: 빈 공간 */}
                <div></div>
              </>
            ) : gameState.currentTurn && (
              <>
                {/* 왼쪽: 라운드 (플레이어 영역 대응) */}
                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '18px',
                  fontWeight: 'bold',
                  color: '#fff',
                  padding: '0 10px'
                }}>
                  ROUND {gameState.currentRound}/{gameState.totalRounds}
                </div>

                {/* 중앙: 힌트 + 힌트 버튼 (캔버스 영역 대응) */}
                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  position: 'relative'
                }}>
                  <div style={{
                    fontSize: '22px',
                    fontWeight: 'bold',
                    color: '#333',
                    letterSpacing: '8px',
                    backgroundColor: 'rgb(208, 225, 249)',
                    padding: '2px 20px',
                    borderRadius: '6px',
                    border: '2px solid #4a6bb3',
                    textShadow: 'none'
                  }}>
                    {gameState.currentTurn.currentHint || '???'}
                  </div>

                  {/* 힌트 버튼 (캔버스 오른쪽 끝) */}
                  {isDrawer && (
                    <div style={{
                      position: 'absolute',
                      right: '10px',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '8px'
                    }}>
                      <button
                        onClick={() => console.log('Chosung hint')}
                        disabled={gameState.currentTurn.hintLevel < 2}
                        style={{
                          padding: '8px 16px',
                          fontSize: '14px',
                          fontWeight: 'bold',
                          backgroundColor: gameState.currentTurn.hintLevel >= 2 ? '#D4A574' : '#999',
                          color: '#fff',
                          border: 'none',
                          borderRadius: '4px',
                          cursor: gameState.currentTurn.hintLevel >= 2 ? 'pointer' : 'not-allowed',
                          opacity: gameState.currentTurn.hintLevel >= 2 ? 1 : 0.6,
                          boxShadow: gameState.currentTurn.hintLevel >= 2 ? '0 2px 4px rgba(0, 0, 0, 0.2)' : 'none'
                        }}
                      >
                        💡 초성
                      </button>
                      <button
                        onClick={() => console.log('Letter hint')}
                        disabled={gameState.currentTurn.hintLevel >= 2}
                        style={{
                          padding: '8px 16px',
                          fontSize: '14px',
                          fontWeight: 'bold',
                          backgroundColor: gameState.currentTurn.hintLevel >= 2 ? '#B8885A' : '#999',
                          color: '#fff',
                          border: 'none',
                          borderRadius: '4px',
                          cursor: gameState.currentTurn.hintLevel >= 2 ? 'pointer' : 'not-allowed',
                          opacity: gameState.currentTurn.hintLevel >= 2 ? 1 : 0.6,
                          boxShadow: gameState.currentTurn.hintLevel >= 2 ? '0 2px 4px rgba(0, 0, 0, 0.2)' : 'none'
                        }}
                      >
                        🔥 글자
                      </button>
                    </div>
                  )}
                </div>

                {/* 오른쪽: 빈 공간 (채팅 영역 대응) */}
                <div></div>
              </>
            )}
          </div>

          {/* 메인 레이아웃 */}
          {gameState.phase === 'GAME_END' ? (
            /* 게임 종료 화면 - 전체 레이아웃 */
            <div style={{
              width: '100%',
              height: '660px',
              backgroundColor: '#E8E5E0',
              borderRadius: '0 0 8px 8px',
              flexShrink: 0,
              padding: '20px',
              boxSizing: 'border-box',
              display: 'flex',
              flexDirection: 'column'
            }}>
              <div style={{
                maxWidth: '1100px',
                width: '100%',
                margin: '0 auto',
                backgroundColor: '#fff',
                padding: '20px',
                borderRadius: '8px',
                boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                display: 'flex',
                flexDirection: 'column',
                height: '100%',
                overflow: 'hidden'
              }}>
                <div style={{ display: 'flex', gap: '20px', flex: 1, minHeight: 0 }}>
                  {/* 왼쪽: 베스트 아티스트 그림 */}
                  <div style={{
                    flex: '0 0 480px',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '12px'
                  }}>
                    <h3 style={{ margin: 0, textAlign: 'center', fontSize: '18px' }}>🎨 베스트 아티스트</h3>

                    {/* 그림 영역 - 캔버스 비율 (810:660 = 27:22) 유지 */}
                    <div style={{
                      width: '100%',
                      height: '391px',
                      backgroundColor: '#f5f5f5',
                      border: '3px solid #8FB8B8',
                      borderRadius: '8px',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      position: 'relative',
                      overflow: 'hidden'
                    }}>
                      {/* TODO: 여기에 베스트 아티스트의 그림이 표시됩니다 */}
                      <div style={{
                        textAlign: 'center',
                        color: '#999',
                        fontSize: '16px'
                      }}>
                        베스트 아티스트의 그림
                      </div>
                    </div>

                    {/* 베스트 아티스트 정보 */}
                    {bestArtist && (bestArtist.totalLikes || 0) > 0 && (
                      <div style={{
                        textAlign: 'center',
                        padding: '12px',
                        backgroundColor: '#E8F4F4',
                        borderRadius: '8px',
                        border: '2px solid #8FB8B8',
                      }}>
                        <div style={{ fontSize: '16px', fontWeight: 'bold', color: '#6B9999' }}>
                          {bestArtist.nickname}
                        </div>
                        <div style={{ fontSize: '13px', color: '#666', marginTop: '4px' }}>
                          👍 추천 {bestArtist.totalLikes}개
                        </div>
                      </div>
                    )}
                  </div>

                  {/* 오른쪽: 최종 순위 */}
                  <div style={{ flex: 1, minWidth: 0, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
                    <h3 style={{ marginTop: 0, marginBottom: '15px', fontSize: '18px' }}>최종 순위</h3>
                    <div style={{
                      flex: 1,
                      overflowY: 'auto',
                      overflowX: 'hidden',
                      paddingRight: '8px'
                    }}>
                      {sortedPlayers.map((player, index) => {
                        const isBestArtist = bestArtist?.playerId === player.playerId && (bestArtist?.totalLikes || 0) > 0;
                        return (
                          <div
                            key={player.playerId}
                            style={{
                              display: 'flex',
                              alignItems: 'center',
                              padding: '12px 15px',
                              marginBottom: '8px',
                              backgroundColor: index === 0 ? '#fff3cd' : '#f8f9fa',
                              border: index === 0 ? '2px solid #ffc107' : '1px solid #dee2e6',
                              borderRadius: '8px',
                              position: 'relative',
                            }}
                          >
                            {isBestArtist && (
                              <div style={{
                                position: 'absolute',
                                top: '-8px',
                                right: '-8px',
                                fontSize: '28px',
                                transform: 'rotate(15deg)',
                              }}>
                                👑
                              </div>
                            )}

                            <span style={{
                              fontSize: '20px',
                              fontWeight: 'bold',
                              marginRight: '15px',
                              width: '35px',
                              textAlign: 'center',
                              flexShrink: 0
                            }}>
                              {index === 0 ? '🥇' : index === 1 ? '🥈' : index === 2 ? '🥉' : `${index + 1}위`}
                            </span>

                            <div style={{ flex: 1, minWidth: 0 }}>
                              <div style={{
                                fontSize: '16px',
                                fontWeight: player.playerId === playerInfo?.playerId ? 'bold' : 'normal',
                                color: player.playerId === playerInfo?.playerId ? '#007bff' : '#000',
                                overflow: 'hidden',
                                textOverflow: 'ellipsis',
                                whiteSpace: 'nowrap'
                              }}>
                                {player.nickname}
                                {player.playerId === playerInfo?.playerId && ' (나)'}
                              </div>
                              <div style={{ fontSize: '13px', color: '#666', marginTop: '3px' }}>
                                👍 {player.totalLikes || 0}
                                {(player.totalDislikes || 0) > 0 && ` • 👎 ${player.totalDislikes}`}
                              </div>
                            </div>

                            <span style={{ fontSize: '18px', fontWeight: 'bold', marginLeft: '10px', flexShrink: 0 }}>
                              {player.score}점
                            </span>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                </div>

                <div style={{ textAlign: 'center', marginTop: '15px', display: 'flex', gap: '12px', justifyContent: 'center', flexShrink: 0 }}>
                  <button
                    onClick={() => console.log('대기방으로')}
                    style={{
                      padding: '10px 35px',
                      fontSize: '15px',
                      backgroundColor: '#28a745',
                      color: 'white',
                      border: 'none',
                      borderRadius: '4px',
                      cursor: 'pointer',
                      fontWeight: 'bold',
                    }}
                  >
                    대기방으로 돌아가기
                  </button>
                  <button
                    onClick={() => navigate('/')}
                    style={{
                      padding: '10px 35px',
                      fontSize: '15px',
                      backgroundColor: '#6c757d',
                      color: 'white',
                      border: 'none',
                      borderRadius: '4px',
                      cursor: 'pointer',
                    }}
                  >
                    메인으로
                  </button>
                </div>
              </div>
            </div>
          ) : (
            <>
            {/* 메인 레이아웃 - 그리드 레이아웃 */}
            <div style={{
              width: '100%',
              height: '606px',
              display: 'grid',
              gridTemplateColumns: '200px 810px 300px',
              gridTemplateRows: '1fr',
              gap: '0',
              backgroundColor: 'transparent',
              flexShrink: 0,
              overflow: 'hidden'
            }}>
              {/* 왼쪽: 플레이어 목록 */}
              <div
              style={{
                display: 'flex',
                flexDirection: 'column',
                backgroundColor: 'transparent',
                borderRight: 'none',
                position: 'relative',
                height: '600px',
                overflow: 'hidden',
                marginTop: '3px'
              }}
              onMouseEnter={() => setIsPlayerHovered(true)}
              onMouseLeave={() => setIsPlayerHovered(false)}
            >
              {!isPlayerAtTop && (
                <button
                  onClick={() => handlePlayerListScroll('up')}
                  style={{
                  position: 'absolute',
                  top: '0',
                  left: '0',
                  right: '0',
                  padding: '1px',
                  backgroundColor: isPlayerHovered ? 'rgba(0, 0, 0, 0.7)' : 'rgba(0, 0, 0, 0.4)',
                  color: '#fff',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  zIndex: 10,
                  fontSize: '14px',
                  fontWeight: 'bold',
                  transition: 'opacity 0.2s, background-color 0.2s',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  opacity: isPlayerHovered ? 0.3 : 0.1
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.backgroundColor = 'rgba(0, 0, 0, 0.85)';
                  e.currentTarget.style.opacity = '0.5';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.backgroundColor = isPlayerHovered ? 'rgba(0, 0, 0, 0.7)' : 'rgba(0, 0, 0, 0.4)';
                  e.currentTarget.style.opacity = isPlayerHovered ? '0.3' : '0.1';
                }}
              >
                  <span style={{ fontSize: '20px' }}>▲</span>
                </button>
              )}
              <PlayerList
                ref={playerListRef}
                players={mockRoomInfo.players.filter(p => p.role === 'PLAYER')}
                currentPlayerId={playerInfo?.playerId || ''}
                maxPlayers={mockRoomInfo.settings.maxPlayers}
              />
              {!isPlayerAtBottom && (
                <button
                  onClick={() => handlePlayerListScroll('down')}
                  style={{
                  position: 'absolute',
                  bottom: '0',
                  left: '0',
                  right: '0',
                  padding: '1px',
                  backgroundColor: isPlayerHovered ? 'rgba(0, 0, 0, 0.7)' : 'rgba(0, 0, 0, 0.4)',
                  color: '#fff',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  zIndex: 10,
                  fontSize: '14px',
                  fontWeight: 'bold',
                  transition: 'opacity 0.2s, background-color 0.2s',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  opacity: isPlayerHovered ? 0.3 : 0.1
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.backgroundColor = 'rgba(0, 0, 0, 0.85)';
                  e.currentTarget.style.opacity = '0.5';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.backgroundColor = isPlayerHovered ? 'rgba(0, 0, 0, 0.7)' : 'rgba(0, 0, 0, 0.4)';
                  e.currentTarget.style.opacity = isPlayerHovered ? '0.3' : '0.1';
                }}
              >
                  <span style={{ fontSize: '20px' }}>▼</span>
                </button>
              )}
            </div>

            {/* 중앙: 캔버스 영역 */}
            <div style={{
              display: 'flex',
              flexDirection: 'column',
              backgroundColor: 'transparent',
              borderRight: 'none',
              position: 'relative',
              overflow: 'hidden'
            }}>
              <div style={{
                flex: 1,
                display: 'flex',
                alignItems: 'flex-start',
                justifyContent: 'center',
                padding: '3px'
              }}>
                {/* Canvas 전용 컨테이너 */}
                <div style={{
                  position: 'relative',
                  width: 'fit-content',
                  height: '100%'
                }}>
                  {/* 제시어/출제자 오버레이 */}
                  {gameState.phase === 'DRAWING' && gameState.currentTurn && (
                    <div style={{
                      position: 'absolute',
                      top: '10px',
                      left: '12px',
                    backgroundColor: 'rgba(91, 132, 216, 0.95)',
                    color: '#fff',
                    padding: '7px 14px',
                    borderRadius: '5px',
                    fontSize: '16px',
                    fontWeight: 'bold',
                    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.3)',
                    zIndex: 10,
                    border: '2px solid #4a6bb3',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '6px'
                  }}>
                    {isDrawer ? (
                      `제시어: ${gameState.currentTurn.word}`
                    ) : (
                      <>
                        <span style={{ fontSize: '16px' }}>🎨</span>
                        <span>{gameState.currentTurn.drawerNickname}</span>
                      </>
                    )}
                  </div>
                )}

                {/* 시간 오버레이 */}
                {gameState.phase === 'DRAWING' && (
                  <div style={{
                    position: 'absolute',
                    top: '10px',
                    right: '12px',
                    backgroundColor: 'rgba(0, 0, 0, 0.7)',
                    color: timeLeft <= 10 ? '#ff5252' : '#fff',
                    padding: '7px 14px',
                    borderRadius: '5px',
                    fontSize: '22px',
                    fontWeight: 'bold',
                    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.3)',
                    zIndex: 10
                  }}>
                    {timeLeft}
                  </div>
                )}

                <Canvas
                  ref={canvasRef}
                  isDrawer={isDrawer}
                  onDrawing={isDrawer ? () => {} : undefined}
                  drawingData={null}
                  initialDrawingEvents={[]}
                  clearSignal={clearSignal}
                  onClearRequest={isDrawer ? () => console.log('Clear canvas') : undefined}
                  turnNumber={gameState.currentTurn?.turnNumber || 0}
                  isSpectatorMidJoin={false}
                  phase={gameState.phase}
                  tool={tool}
                  color={color}
                  width={brushWidth}
                  onToolChange={setTool}
                  onColorChange={setColor}
                  onWidthChange={setBrushWidth}
                  hideToolbar={true}
                />

                {/* 카운트다운 오버레이 */}
                {gameState.phase === 'COUNTDOWN' && (
                  <div style={{
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    width: '800px',
                    height: '600px',
                    backgroundColor: 'rgba(0, 0, 0, 0.5)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    zIndex: 100,
                    pointerEvents: 'none'
                  }}>
                    <div style={{
                      fontSize: '120px',
                      fontWeight: 'bold',
                      color: '#fff',
                      textShadow: '0 4px 8px rgba(0, 0, 0, 0.5)',
                      animation: 'pulse 1s infinite'
                    }}>
                      {timeLeft}
                    </div>
                  </div>
                )}

                {/* 단어 선택 오버레이 */}
                {gameState.phase === 'WORD_SELECT' && (
                  <div style={{
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    width: '800px',
                    height: '600px',
                    backgroundColor: 'rgba(0, 0, 0, 0.5)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    zIndex: 100,
                    padding: '20px',
                    boxSizing: 'border-box',
                    pointerEvents: 'none'
                  }}>
                    {isDrawer ? (
                      <WordSelect
                        turnInfo={gameState.currentTurn}
                        onSelectWord={(word) => console.log('Selected word:', word)}
                        roomId="preview"
                      />
                    ) : (
                      <div style={{
                        backgroundColor: '#fff',
                        padding: '40px 60px',
                        borderRadius: '12px',
                        textAlign: 'center',
                        boxShadow: '0 4px 20px rgba(0, 0, 0, 0.3)'
                      }}>
                        <h2 style={{ margin: 0, fontSize: '24px', color: '#333' }}>
                          출제자가 단어를 선택하고 있습니다...
                        </h2>
                        <p style={{ marginTop: '20px', color: '#666', fontSize: '16px' }}>
                          잠시만 기다려주세요.
                        </p>
                      </div>
                    )}
                  </div>
                )}

                {/* 턴 결과 오버레이 */}
                {gameState.phase === 'TURN_RESULT' && (
                  <div style={{
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    width: '800px',
                    height: '600px',
                    backgroundColor: 'rgba(0, 0, 0, 0.7)',
                    display: 'flex',
                    alignItems: 'flex-start',
                    justifyContent: 'center',
                    zIndex: 100,
                    padding: '20px',
                    boxSizing: 'border-box',
                    overflowY: 'auto',
                    pointerEvents: 'auto'
                  }}>
                    <TurnResult
                      turnInfo={gameState.currentTurn}
                      players={gameState.players}
                      canvasImageUrl={canvasImageUrl}
                      isSpectatorMidJoin={false}
                    />
                  </div>
                )}

                {/* 라운드 종료 오버레이 */}
                {gameState.phase === 'ROUND_END' && (
                  <div style={{
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    width: '800px',
                    height: '600px',
                    backgroundColor: 'rgba(0, 0, 0, 0.5)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    zIndex: 100,
                    pointerEvents: 'auto'
                  }}>
                    <div style={{
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      gap: '20px'
                    }}>
                      <div style={{
                        fontSize: '120px',
                        fontWeight: 'bold',
                        color: '#fff',
                        textShadow: '0 4px 8px rgba(0, 0, 0, 0.5)',
                        animation: 'pulse 1s infinite'
                      }}>
                        {timeLeft}
                      </div>
                      <div style={{
                        fontSize: '32px',
                        fontWeight: 'bold',
                        color: '#fff',
                        textShadow: '0 2px 4px rgba(0, 0, 0, 0.5)'
                      }}>
                        다음 라운드가 시작됩니다...
                      </div>
                    </div>
                  </div>
                )}
                </div>
                {/* Canvas 전용 컨테이너 끝 */}
              </div>
            </div>

            {/* 우측: 관전자 목록 + 채팅창 */}
            <div style={{
              display: 'flex',
              flexDirection: 'column',
              gap: '0',
              height: '609px',
              maxHeight: '609px',
              boxSizing: 'border-box',
              overflow: 'hidden',
              padding: '0'
            }}>
              {/* 관전자 목록 */}
              <div
                style={{
                  flex: '1',
                  display: 'flex',
                  flexDirection: 'column',
                  backgroundColor: 'transparent',
                  borderBottom: 'none',
                  padding: '8px 0 2px 0',
                  minHeight: 0,
                  position: 'relative'
                }}
                onMouseEnter={() => setIsSpectatorHovered(true)}
                onMouseLeave={() => setIsSpectatorHovered(false)}
              >
                {!isSpectatorAtTop && (
                  <button
                    onClick={() => handleSpectatorListScroll('up')}
                    style={{
                    position: 'absolute',
                    top: '0',
                    left: '0',
                    right: '0',
                    padding: '1px',
                    backgroundColor: isSpectatorHovered ? 'rgba(0, 0, 0, 0.7)' : 'rgba(0, 0, 0, 0.4)',
                    color: '#fff',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    zIndex: 10,
                    fontSize: '14px',
                    fontWeight: 'bold',
                    transition: 'opacity 0.2s, background-color 0.2s',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    opacity: isSpectatorHovered ? 0.3 : 0.1
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = 'rgba(0, 0, 0, 0.85)';
                    e.currentTarget.style.opacity = '0.5';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = isSpectatorHovered ? 'rgba(0, 0, 0, 0.7)' : 'rgba(0, 0, 0, 0.4)';
                    e.currentTarget.style.opacity = isSpectatorHovered ? '0.3' : '0.1';
                  }}
                >
                    <span style={{ fontSize: '20px' }}>▲</span>
                  </button>
                )}
                <SpectatorList
                  ref={spectatorListRef}
                  spectators={mockRoomInfo.players.filter(p => p.role === 'SPECTATOR')}
                  currentPlayerId={playerInfo?.playerId || ''}
                  maxSpectators={mockRoomInfo.settings.maxSpectators}
                />
                {!isSpectatorAtBottom && (
                  <button
                    onClick={() => handleSpectatorListScroll('down')}
                    style={{
                    position: 'absolute',
                    bottom: '0',
                    left: '0',
                    right: '0',
                    padding: '1px',
                    backgroundColor: isSpectatorHovered ? 'rgba(0, 0, 0, 0.7)' : 'rgba(0, 0, 0, 0.4)',
                    color: '#fff',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    zIndex: 10,
                    fontSize: '14px',
                    fontWeight: 'bold',
                    transition: 'opacity 0.2s, background-color 0.2s',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    opacity: isSpectatorHovered ? 0.3 : 0.1
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = 'rgba(0, 0, 0, 0.85)';
                    e.currentTarget.style.opacity = '0.5';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = isSpectatorHovered ? 'rgba(0, 0, 0, 0.7)' : 'rgba(0, 0, 0, 0.4)';
                    e.currentTarget.style.opacity = isSpectatorHovered ? '0.3' : '0.1';
                  }}
                >
                    <span style={{ fontSize: '20px' }}>▼</span>
                  </button>
                )}
              </div>

              {/* 채팅창 */}
              <div style={{
                flex: '2.9',
                display: 'flex',
                flexDirection: 'column',
                backgroundColor: 'transparent',
                padding: '8px 0 6px 0',
                minHeight: 0
              }}>
                {isCorrect && gameState.phase === 'DRAWING' && (
                  <div style={{
                    padding: '10px',
                    backgroundColor: '#d4edda',
                    border: '2px solid #28a745',
                    borderRadius: '4px',
                    marginBottom: '6px',
                    textAlign: 'center',
                    fontWeight: 'bold',
                    color: '#155724',
                    fontSize: '14px'
                  }}>
                    🎉 정답을 맞췄습니다!
                  </div>
                )}
                <ChatBox
                  messages={messages}
                  onSendMessage={handleSendMessage}
                  disabled={gameState.phase !== 'DRAWING' || isDrawer}
                  currentPlayerId={playerInfo?.playerId || ''}
                  isCorrect={isCorrect}
                  isDrawer={isDrawer}
                  headerMessage={
                    gameState.phase !== 'DRAWING' ? (
                      <div style={{
                        padding: '10px',
                        backgroundColor: '#fff3cd',
                        border: '2px solid #ffc107',
                        borderRadius: '4px',
                        textAlign: 'center',
                        fontWeight: 'bold',
                        color: '#856404',
                        fontSize: '14px'
                      }}>
                        ⏸️ 그림 그리기 중에만 채팅할 수 있습니다
                      </div>
                    ) : isDrawer ? (
                      <div style={{
                        padding: '10px',
                        backgroundColor: '#d1ecf1',
                        border: '2px solid #0c5460',
                        borderRadius: '4px',
                        textAlign: 'center',
                        fontWeight: 'bold',
                        color: '#0c5460',
                        fontSize: '14px'
                      }}>
                        🎨 출제자는 채팅을 입력할 수 없습니다
                      </div>
                    ) : undefined
                  }
                />
              </div>
            </div>
          </div>

            {/* 하단 영역: 그림 툴 및 추천/비추천 버튼 */}
            <div style={{
              width: '100%',
              display: 'grid',
              gridTemplateColumns: '200px 810px 300px',
              gap: '0',
              backgroundColor: 'transparent',
              borderRadius: '0 0 8px 8px',
            }}>
              {/* 왼쪽 빈 영역 */}
              <div></div>

              {/* 중앙: 캔버스 영역 - 그림 툴 또는 추천/비추천 버튼 */}
              <div style={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                padding: '0',
              }}>
                {isDrawer && gameState.phase === 'DRAWING' ? (
                  /* 출제자용 그림 툴바 */
                  <CanvasToolbar
                    tool={tool}
                    color={color}
                    width={brushWidth}
                    onToolChange={setTool}
                    onColorChange={setColor}
                    onWidthChange={setBrushWidth}
                    onClear={handleClearCanvas}
                  />
                ) : !isDrawer && gameState.phase === 'DRAWING' ? (
                  /* 참가자용 추천/비추천 버튼 */
                  <div style={{
                    display: 'flex',
                    gap: '6px',
                    justifyContent: 'center',
                  }}>
                    <button
                      onClick={() => handleVote('LIKE')}
                      disabled={isCorrect}
                      style={{
                        padding: '6px 16px',
                        fontSize: '15px',
                        fontWeight: 'bold',
                        backgroundColor: currentVote === 'LIKE' ? '#ffd75e' : '#fff',
                        color: currentVote === 'LIKE' ? '#333' : '#333',
                        border: 'none',
                        outline: 'none',
                        borderRadius: '4px',
                        cursor: isCorrect ? 'not-allowed' : 'pointer',
                        opacity: isCorrect ? 0.5 : 1,
                        boxShadow: currentVote === 'LIKE' ? '0 2px 4px rgba(0, 0, 0, 0.2)' : 'none',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '6px'
                      }}
                    >
                      <span className="material-symbols-outlined" style={{
                        fontSize: '20px',
                        fontVariationSettings: '"FILL" 1, "wght" 400, "GRAD" 0, "opsz" 48'
                      }}>thumb_up</span>
                    </button>
                    <button
                      onClick={() => handleVote('DISLIKE')}
                      disabled={isCorrect}
                      style={{
                        padding: '6px 16px',
                        fontSize: '15px',
                        fontWeight: 'bold',
                        backgroundColor: currentVote === 'DISLIKE' ? '#ff8566' : '#fff',
                        color: currentVote === 'DISLIKE' ? '#fff' : '#333',
                        border: 'none',
                        outline: 'none',
                        borderRadius: '4px',
                        cursor: isCorrect ? 'not-allowed' : 'pointer',
                        opacity: isCorrect ? 0.5 : 1,
                        boxShadow: currentVote === 'DISLIKE' ? '0 2px 4px rgba(0, 0, 0, 0.2)' : 'none',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '6px'
                      }}
                    >
                      <span className="material-symbols-outlined" style={{
                        fontSize: '20px',
                        fontVariationSettings: '"FILL" 1, "wght" 400, "GRAD" 0, "opsz" 48'
                      }}>thumb_down</span>
                    </button>
                  </div>
                ) : null}
              </div>

              {/* 오른쪽 빈 영역 */}
              <div></div>
            </div>
            </>
          )}
        </div>
      ) : null}

      {/* 페이즈 전환 컨트롤 */}
      <div style={{
        width: '1310px',
        margin: '20px auto 0',
        padding: '15px',
        backgroundColor: '#e3f2fd',
        borderRadius: '8px',
        border: '1px solid #2196f3',
      }}>
        <h4>페이즈 전환</h4>
        <div style={{ display: 'flex', gap: '10px', marginTop: '10px', flexWrap: 'wrap' }}>
          {(['COUNTDOWN', 'WORD_SELECT', 'DRAWING', 'TURN_RESULT', 'ROUND_END', 'GAME_END'] as GamePhase[]).map((phase) => (
            <button
              key={phase}
              onClick={() => handlePhaseChange(phase)}
              style={{
                padding: '8px 16px',
                backgroundColor: currentPhase === phase ? '#2196f3' : '#90caf9',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontWeight: currentPhase === phase ? 'bold' : 'normal',
              }}
            >
              {phase}
            </button>
          ))}
        </div>
      </div>

      {/* 타이머 컨트롤 (테스트용) */}
      {['COUNTDOWN', 'WORD_SELECT', 'DRAWING'].includes(currentPhase) && (
        <div style={{
          width: '1310px',
          margin: '20px auto 0',
          padding: '15px',
          backgroundColor: '#fff3cd',
          borderRadius: '8px',
          border: '1px solid #ffc107',
        }}>
          <h4>타이머 컨트롤</h4>
          <div style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
            <button
              onClick={() => setTimeLeft(Math.max(0, timeLeft - 5))}
              style={{
                padding: '8px 16px',
                backgroundColor: '#ff9800',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
              }}
            >
              시간 -5초
            </button>
            <button
              onClick={() => setTimeLeft(timeLeft + 5)}
              style={{
                padding: '8px 16px',
                backgroundColor: '#4caf50',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
              }}
            >
              시간 +5초
            </button>
            <span style={{ marginLeft: '10px', lineHeight: '32px' }}>
              현재 시간: {timeLeft}초
            </span>
          </div>
        </div>
      )}

      {/* 프리뷰 알림 배너 */}
      <div style={{
        width: '1310px',
        margin: '20px auto 0',
        padding: '15px',
        backgroundColor: '#f0f0f0',
        border: '2px solid #999',
        borderRadius: '8px',
        textAlign: 'center',
        fontWeight: 'bold',
        color: '#555',
      }}>
        🎨 게임 페이지 프리뷰 모드 (서버 연결 없음)
      </div>

      {/* 카운트다운 애니메이션 */}
      <style>
        {`
          @keyframes pulse {
            0%, 100% {
              transform: scale(1);
              opacity: 1;
            }
            50% {
              transform: scale(1.2);
              opacity: 0.8;
            }
          }
        `}
      </style>
    </div>
  );
}
