import { BrowserRouter, Routes, Route } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import MainPage from './pages/MainPage';
import RoomPage from './pages/RoomPage';
import GameRoomPage from './pages/GameRoomPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/room/:roomId" element={<LoginPage />} />
        <Route path="/main" element={<MainPage />} />
        <Route path="/room/:roomId/lobby" element={<RoomPage />} />
        <Route path="/room/:roomId/game" element={<GameRoomPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;