
import React, { createContext, useContext } from 'react';

// Создаем контекст для WebSocket
const WebSocketContext = createContext(null);

// Хук для доступа к WebSocket
export const useWebSocket = () => useContext(WebSocketContext);

export default WebSocketContext;
