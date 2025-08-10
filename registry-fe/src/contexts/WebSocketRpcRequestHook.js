// Утилиты для работы с WebSocket сообщениями
export const createRpcRequest = (method, params = {}, id = null) => {
    return {
        jsonrpc: "2.0",
        method,
        params,
        id: id || Date.now()
    };
};

export const isRpcResponse = (message) => {
    try {
        const parsed = typeof message === 'string' ? JSON.parse(message) : message;
        return parsed.jsonrpc === "2.0" && (parsed.result !== undefined || parsed.error !== undefined);
    } catch {
        return false;
    }
};

export const parseRpcMessage = (message) => {
    try {
        return typeof message === 'string' ? JSON.parse(message) : message;
    } catch (error) {
        console.error('Failed to parse RPC message:', error);
        return null;
    }
};

// Хук для отправки RPC запросов
import { useCallback, useRef } from 'react';
import { useWebSocket } from '../contexts/WebSocketContext';

export const useRpcRequest = () => {
    const { sendMessage, isConnected } = useWebSocket();
    const pendingRequestsRef = useRef(new Map());

    const sendRequest = useCallback((method, params = {}, timeout = 30000) => {
        return new Promise((resolve, reject) => {
            if (!isConnected) {
                reject(new Error('WebSocket is not connected'));
                return;
            }

            const requestId = Date.now() + Math.random();
            const request = createRpcRequest(method, params, requestId);

            // Сохраняем промис для обработки ответа
            const timeoutId = setTimeout(() => {
                pendingRequestsRef.current.delete(requestId);
                reject(new Error(`Request timeout: ${method}`));
            }, timeout);

            pendingRequestsRef.current.set(requestId, { resolve, reject, timeoutId });

            const success = sendMessage(request);
            if (!success) {
                pendingRequestsRef.current.delete(requestId);
                clearTimeout(timeoutId);
                reject(new Error('Failed to send request'));
            }
        });
    }, [sendMessage, isConnected]);

    const handleMessage = useCallback((message) => {
        const parsed = parseRpcMessage(message);
        if (!parsed || !isRpcResponse(parsed)) return false;

        const pendingRequest = pendingRequestsRef.current.get(parsed.id);
        if (!pendingRequest) return false;

        const { resolve, reject, timeoutId } = pendingRequest;
        clearTimeout(timeoutId);
        pendingRequestsRef.current.delete(parsed.id);

        if (parsed.error) {
            reject(new Error(parsed.error.message || 'RPC Error'));
        } else {
            resolve(parsed.result);
        }

        return true;
    }, []);

    return { sendRequest, handleMessage };
};
