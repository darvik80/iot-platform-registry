import React, { createContext, useContext } from 'react';
import { useRpcRequest } from '../hooks/WebSocketRpcRequestHook';

const WebSocketContext = createContext(null);

export const WebSocketProvider = ({ children, url = 'ws://localhost:8080/ws-rpc' }) => {
    const rpcData = useRpcRequest(url);
    
    return (
        <WebSocketContext.Provider value={rpcData}>
            {children}
        </WebSocketContext.Provider>
    );
};

export const useWebSocket = () => {
    const context = useContext(WebSocketContext);
    if (!context) {
        throw new Error('useWebSocket must be used within a WebSocketProvider');
    }
    return context;
};

export default WebSocketContext;
