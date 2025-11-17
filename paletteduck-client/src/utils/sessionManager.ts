export const generateSessionId = () => {
  return `session_${Date.now()}_${Math.random().toString(36).substring(7)}`;
};

export const getSessionId = () => {
  let sessionId = sessionStorage.getItem('paletteduck_session');
  if (!sessionId) {
    sessionId = generateSessionId();
    sessionStorage.setItem('paletteduck_session', sessionId);
  }
  return sessionId;
};

export const clearSession = () => {
  sessionStorage.removeItem('paletteduck_session');
  sessionStorage.removeItem('paletteduck_token');
};