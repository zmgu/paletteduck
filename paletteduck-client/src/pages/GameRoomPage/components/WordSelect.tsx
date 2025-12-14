import { useState, useEffect } from 'react';
import type { TurnInfo } from '../../../types/game.types';
import { getPlayerInfo } from '../../../utils/apiClient';

interface WordSelectProps {
  turnInfo: TurnInfo;
  onSelectWord: (word: string) => void;
  roomId: string;
}

export default function WordSelect({ turnInfo, onSelectWord, roomId }: WordSelectProps) {
  const [customWord, setCustomWord] = useState('');
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [error, setError] = useState('');
  const [hasUsedCustomInput, setHasUsedCustomInput] = useState(false);
  const [showCustomInput, setShowCustomInput] = useState(false);

  // localStorage에서 해당 방+플레이어의 직접 입력 사용 여부 확인
  useEffect(() => {
    const playerInfo = getPlayerInfo();
    if (!playerInfo) return;

    const storageKey = `customWordUsed_${roomId}_${playerInfo.playerId}`;
    const used = localStorage.getItem(storageKey) === 'true';
    setHasUsedCustomInput(used);
  }, [roomId]);

  // 턴이 변경될 때 입력 초기화
  useEffect(() => {
    setCustomWord('');
    setError('');
    setShowConfirmModal(false);
  }, [turnInfo.turnNumber]);

  // 한글만 입력 가능한지 검사
  const validateWord = (word: string): boolean => {
    // 한글만 허용하는 정규식 (자음/모음/완성형 한글)
    const koreanOnly = /^[가-힣ㄱ-ㅎㅏ-ㅣ]+$/;

    if (word.length < 2 || word.length > 10) {
      setError('2~10글자로 입력해주세요');
      return false;
    }

    if (!koreanOnly.test(word)) {
      setError('한글만 입력 가능합니다');
      return false;
    }

    setError('');
    return true;
  };

  const handleCustomWordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setCustomWord(value);

    if (value === '') {
      setError('');
      return;
    }

    validateWord(value);
  };

  const handleCustomWordSubmit = () => {
    if (!customWord.trim()) {
      setError('단어를 입력해주세요');
      return;
    }

    if (validateWord(customWord)) {
      setShowConfirmModal(true);
    }
  };

  const handleConfirm = () => {
    const wordToSend = customWord;

    onSelectWord(wordToSend);

    const playerInfo = getPlayerInfo();
    if (playerInfo) {
      const storageKey = `customWordUsed_${roomId}_${playerInfo.playerId}`;
      localStorage.setItem(storageKey, 'true');
    }

    setShowConfirmModal(false);
  };

  const handleCancel = () => {
    setShowConfirmModal(false);
  };

  return (
    <>
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

        {/* 단어 선택지 (기존 단어 + 직접 입력 버튼) */}
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

          {/* 직접 입력 버튼 */}
          <button
            onClick={() => !hasUsedCustomInput && setShowCustomInput(!showCustomInput)}
            disabled={hasUsedCustomInput}
            style={{
              flex: 1,
              padding: '30px 20px',
              fontSize: '24px',
              fontWeight: 'bold',
              backgroundColor: hasUsedCustomInput ? '#ccc' : showCustomInput ? '#45a049' : '#4caf50',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              cursor: hasUsedCustomInput ? 'not-allowed' : 'pointer',
              transition: 'all 0.2s',
              position: 'relative'
            }}
            onMouseEnter={(e) => {
              if (!hasUsedCustomInput && !showCustomInput) {
                e.currentTarget.style.backgroundColor = '#45a049';
              }
            }}
            onMouseLeave={(e) => {
              if (!hasUsedCustomInput && !showCustomInput) {
                e.currentTarget.style.backgroundColor = '#4caf50';
              }
            }}
          >
            <div>직접 입력</div>
            <div style={{ fontSize: '12px', marginTop: '8px', opacity: 0.9 }}>
              {hasUsedCustomInput ? '(사용됨)' : '(1회만)'}
            </div>
          </button>
        </div>

        {/* 직접 입력란 (버튼 클릭 시 하단에 표시) */}
        {showCustomInput && !hasUsedCustomInput && (
          <div style={{
            marginTop: '20px',
            padding: '20px',
            backgroundColor: '#f1f8e9',
            borderRadius: '8px',
            border: '2px solid #4caf50'
          }}>
            <div style={{ display: 'flex', gap: '10px', alignItems: 'flex-start' }}>
              <div style={{ flex: 1 }}>
                <input
                  type="text"
                  value={customWord}
                  onChange={handleCustomWordChange}
                  placeholder="2~10글자 한글 단어 입력"
                  maxLength={10}
                  autoFocus
                  style={{
                    width: '100%',
                    padding: '12px',
                    fontSize: '18px',
                    color: '#000',
                    border: error ? '2px solid #d32f2f' : '2px solid #4caf50',
                    borderRadius: '8px',
                    outline: 'none',
                    backgroundColor: 'white',
                    boxSizing: 'border-box',
                  }}
                  onKeyPress={(e) => {
                    if (e.key === 'Enter') {
                      handleCustomWordSubmit();
                    }
                  }}
                />
                {error && (
                  <div style={{
                    color: '#d32f2f',
                    fontSize: '12px',
                    marginTop: '5px'
                  }}>
                    {error}
                  </div>
                )}
              </div>

              <button
                onClick={handleCustomWordSubmit}
                disabled={!customWord.trim()}
                style={{
                  padding: '12px 24px',
                  fontSize: '18px',
                  fontWeight: 'bold',
                  backgroundColor: !customWord.trim() ? '#ccc' : '#4caf50',
                  color: 'white',
                  border: 'none',
                  borderRadius: '8px',
                  cursor: !customWord.trim() ? 'not-allowed' : 'pointer',
                  transition: 'all 0.2s',
                  whiteSpace: 'nowrap'
                }}
                onMouseEnter={(e) => {
                  if (customWord.trim()) {
                    e.currentTarget.style.backgroundColor = '#45a049';
                  }
                }}
                onMouseLeave={(e) => {
                  if (customWord.trim()) {
                    e.currentTarget.style.backgroundColor = '#4caf50';
                  }
                }}
              >
                확인
              </button>
            </div>
          </div>
        )}
      </div>

      {/* 확인 모달 */}
      {showConfirmModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 1000
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '30px',
            borderRadius: '12px',
            maxWidth: '400px',
            width: '90%',
            boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)'
          }}>
            <h3 style={{
              marginTop: 0,
              fontSize: '20px',
              textAlign: 'center',
              color: '#333'
            }}>
              단어 확인
            </h3>

            <p style={{
              fontSize: '16px',
              textAlign: 'center',
              margin: '20px 0',
              color: '#666'
            }}>
              "<span style={{
                fontSize: '24px',
                fontWeight: 'bold',
                color: '#2196f3'
              }}>{customWord}</span>"
              <br />
              이 단어로 선택하시겠습니까?
            </p>

            <p style={{
              fontSize: '12px',
              textAlign: 'center',
              color: '#d32f2f',
              marginBottom: '20px'
            }}>
              직접 입력은 게임에서 한 번만 사용할 수 있습니다.
            </p>

            <div style={{
              display: 'flex',
              gap: '10px',
              justifyContent: 'center'
            }}>
              <button
                onClick={handleCancel}
                style={{
                  padding: '10px 30px',
                  fontSize: '16px',
                  backgroundColor: '#757575',
                  color: 'white',
                  border: 'none',
                  borderRadius: '8px',
                  cursor: 'pointer',
                  transition: 'all 0.2s'
                }}
                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#616161'}
                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#757575'}
              >
                취소
              </button>

              <button
                onClick={handleConfirm}
                style={{
                  padding: '10px 30px',
                  fontSize: '16px',
                  fontWeight: 'bold',
                  backgroundColor: '#4caf50',
                  color: 'white',
                  border: 'none',
                  borderRadius: '8px',
                  cursor: 'pointer',
                  transition: 'all 0.2s'
                }}
                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#45a049'}
                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#4caf50'}
              >
                확인
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}