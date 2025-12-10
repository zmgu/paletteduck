import { BrowserRouter, Routes, Route } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import MainPage from './pages/MainPage';
import RoomPage from './pages/RoomPage'; // 폴더명으로 import
import GameRoomPage from './pages/GameRoomPage';
import GameRoomPreview from './pages/GameRoomPreview';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/room/:roomId" element={<LoginPage />} />
        <Route path="/main" element={<MainPage />} />
        <Route path="/room/:roomId/lobby" element={<RoomPage />} />
        <Route path="/room/:roomId/game" element={<GameRoomPage />} />
        <Route path="/game-preview" element={<GameRoomPreview />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;