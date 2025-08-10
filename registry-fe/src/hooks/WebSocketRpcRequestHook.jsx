import { useCallback, useRef, useState, useEffect } from 'react';

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

// Константы для переподключения
const RECONNECT_INTERVALS = [1000, 2000, 5000, 10000, 30000];
const MAX_RECONNECT_ATTEMPTS = 5;
const PING_INTERVAL = 30000;

// Хук для WebSocket с переподключением
export const useWebSocketWithReconnect = (url) => {
    const [ws, setWs] = useState(null);
    const [status, setStatus] = useState('Connecting...');
    const [reconnectAttempt, setReconnectAttempt] = useState(0);
    const [lastError, setLastError] = useState(null);
    const [isOnline, setIsOnline] = useState(navigator.onLine);

    const wsRef = useRef(null);
    const reconnectTimeoutRef = useRef(null);
    const reconnectAttemptsRef = useRef(0);
    const isManuallyClosedRef = useRef(false);
    const mountedRef = useRef(true);
    const pingIntervalRef = useRef(null);
    const messageHandlersRef = useRef(new Set());
    const urlRef = useRef(url); // Стабильная ссылка на URL

    // Обновляем URL при изменении
    useEffect(() => {
        urlRef.current = url;
    }, [url]);

    // Добавление обработчика сообщений
    const addMessageHandler = useCallback((handler) => {
        messageHandlersRef.current.add(handler);
        return () => messageHandlersRef.current.delete(handler);
    }, []);

    // Обработка входящих сообщений
    const handleMessage = useCallback((event) => {
        messageHandlersRef.current.forEach(handler => {
            try {
                handler(event);
            } catch (error) {
                console.error('Message handler error:', error);
            }
        });
    }, []);

    // Отправка сообщения
    const sendMessage = useCallback((message) => {
        if (ws && ws.readyState === WebSocket.OPEN) {
            try {
                const payload = typeof message === 'string' ? message : JSON.stringify(message);
                ws.send(payload);
                return true;
            } catch (error) {
                console.error('Failed to send WebSocket message:', error);
                setLastError('Failed to send message');
                return false;
            }
        }
        return false;
    }, [ws]);

    // Ping для проверки соединения
    const sendPing = useCallback(() => {
        if (ws && ws.readyState === WebSocket.OPEN) {
            sendMessage(createRpcRequest('ping', { timestamp: Date.now() }));
        }
    }, [ws, sendMessage]);

    // Подключение к WebSocket - БЕЗ useCallback чтобы избежать циклов
    const connect = useRef();
    connect.current = () => {
        if (!mountedRef.current || !isOnline) return;

        console.log('Connecting to WebSocket:', urlRef.current);

        try {
            setStatus('Connecting...');
            setLastError(null);

            const websocket = new WebSocket(urlRef.current);
            wsRef.current = websocket;

            websocket.onopen = () => {
                if (!mountedRef.current) return;

                console.log('WebSocket connected');
                setStatus('Connected');
                setWs(websocket);
                setReconnectAttempt(0);
                reconnectAttemptsRef.current = 0;

                // Запускаем ping
                if (pingIntervalRef.current) {
                    clearInterval(pingIntervalRef.current);
                }
                pingIntervalRef.current = setInterval(() => {
                    if (websocket.readyState === WebSocket.OPEN) {
                        websocket.send(JSON.stringify(createRpcRequest('ping', { timestamp: Date.now() })));
                    }
                }, PING_INTERVAL);
            };

            websocket.onmessage = handleMessage;

            websocket.onerror = (error) => {
                if (!mountedRef.current) return;

                console.error('WebSocket error:', error);
                setLastError('Connection error');
                setStatus('Connection error');
            };

            websocket.onclose = (event) => {
                if (!mountedRef.current) return;

                console.log('WebSocket closed:', event.code, event.reason);
                
                setWs(null);

                // Очищаем ping интервал
                if (pingIntervalRef.current) {
                    clearInterval(pingIntervalRef.current);
                    pingIntervalRef.current = null;
                }

                if (isManuallyClosedRef.current) {
                    setStatus('Disconnected');
                    return;
                }

                // Автоматическое переподключение только для определенных кодов
                if (reconnectAttemptsRef.current < MAX_RECONNECT_ATTEMPTS && isOnline) {
                    const interval = RECONNECT_INTERVALS[Math.min(reconnectAttemptsRef.current, RECONNECT_INTERVALS.length - 1)];
                    setStatus(`Reconnecting in ${Math.ceil(interval / 1000)}s... (${reconnectAttemptsRef.current + 1}/${MAX_RECONNECT_ATTEMPTS})`);
                    setReconnectAttempt(reconnectAttemptsRef.current + 1);

                    console.log(`Attempting to reconnect in ${interval}ms...`);
                    reconnectTimeoutRef.current = setTimeout(() => {
                        reconnectAttemptsRef.current++;
                        if (connect.current) {
                            connect.current();
                        }
                    }, interval);
                } else {
                    const reason = !isOnline ? 'No internet connection' : 'Max attempts reached';
                    setStatus(`Disconnected (${reason})`);
                    setLastError(`Failed to reconnect: ${reason}`);
                }
            };

        } catch (error) {
            console.error('Failed to create WebSocket:', error);
            setStatus('Connection failed');
            setLastError(error.message);
        }
    };

    // Отключение
    const disconnect = useCallback(() => {
        console.log('Manually disconnecting WebSocket');
        isManuallyClosedRef.current = true;

        if (reconnectTimeoutRef.current) {
            clearTimeout(reconnectTimeoutRef.current);
            reconnectTimeoutRef.current = null;
        }

        if (pingIntervalRef.current) {
            clearInterval(pingIntervalRef.current);
            pingIntervalRef.current = null;
        }

        if (wsRef.current) {
            wsRef.current.close();
        }

        setStatus('Disconnected');
        setWs(null);
    }, []);

    // Ручное переподключение
    const reconnect = useCallback(() => {
        console.log('Manual reconnect requested');
        isManuallyClosedRef.current = false;
        reconnectAttemptsRef.current = 0;
        setReconnectAttempt(0);

        if (reconnectTimeoutRef.current) {
            clearTimeout(reconnectTimeoutRef.current);
            reconnectTimeoutRef.current = null;
        }

        if (wsRef.current) {
            wsRef.current.close();
        }

        if (connect.current) {
            connect.current();
        }
    }, []);

    // Мониторинг состояния сети
    useEffect(() => {
        const handleOnline = () => {
            console.log('Network came online');
            setIsOnline(true);
            if (!ws && !isManuallyClosedRef.current) {
                console.log('Network restored, attempting to reconnect...');
                if (connect.current) {
                    connect.current();
                }
            }
        };

        const handleOffline = () => {
            console.log('Network went offline');
            setIsOnline(false);
            setStatus('No internet connection');
        };

        window.addEventListener('online', handleOnline);
        window.addEventListener('offline', handleOffline);

        return () => {
            window.removeEventListener('online', handleOnline);
            window.removeEventListener('offline', handleOffline);
        };
    }, [ws]);

    // ЕДИНСТВЕННАЯ инициализация при монтировании
    useEffect(() => {
        console.log('WebSocket hook initialized');
        mountedRef.current = true;
        isManuallyClosedRef.current = false;
        
        // Подключаемся только один раз
        if (connect.current) {
            connect.current();
        }

        return () => {
            console.log('WebSocket hook cleanup');
            mountedRef.current = false;
            isManuallyClosedRef.current = true;

            if (reconnectTimeoutRef.current) {
                clearTimeout(reconnectTimeoutRef.current);
            }

            if (pingIntervalRef.current) {
                clearInterval(pingIntervalRef.current);
            }

            if (wsRef.current) {
                wsRef.current.close();
            }
        };
    }, []); // ПУСТОЙ массив зависимостей!

    return {
        ws,
        status,
        reconnectAttempt,
        lastError,
        isOnline,
        isConnected: ws && ws.readyState === WebSocket.OPEN,
        connect: reconnect,
        disconnect,
        sendMessage,
        addMessageHandler
    };
};

// Обновленный хук для отправки RPC запросов
export const useRpcRequest = (url = 'ws://127.0.0.1:8080/ws-rpc') => {
    const webSocketData = useWebSocketWithReconnect(url);
    const { sendMessage, isConnected, addMessageHandler } = webSocketData;
    
    const pendingRequestsRef = useRef(new Map());
    const requestQueueRef = useRef([]);

    // Обработка входящих RPC сообщений
    useEffect(() => {
        const handleRpcMessage = (event) => {
            const parsed = parseRpcMessage(event.data);
            if (!parsed || !isRpcResponse(parsed)) return;

            const pendingRequest = pendingRequestsRef.current.get(parsed.id);
            if (!pendingRequest) return;

            const { resolve, reject, timeoutId } = pendingRequest;
            clearTimeout(timeoutId);
            pendingRequestsRef.current.delete(parsed.id);

            if (parsed.error) {
                reject(new Error(`RPC Error [${parsed.error.code}]: ${parsed.error.message}`));
            } else {
                resolve(parsed.result);
            }
        };

        return addMessageHandler(handleRpcMessage);
    }, [addMessageHandler]);

    // Обработка очереди запросов при подключении
    useEffect(() => {
        if (isConnected && requestQueueRef.current.length > 0) {
            console.log(`Processing ${requestQueueRef.current.length} queued requests`);

            const queue = [...requestQueueRef.current];
            requestQueueRef.current = [];

            queue.forEach(({ request, resolve, reject, timeoutId }) => {
                const newId = Date.now() + Math.random();
                const updatedRequest = { ...request, id: newId };

                pendingRequestsRef.current.delete(request.id);
                pendingRequestsRef.current.set(newId, { resolve, reject, timeoutId });

                if (!sendMessage(updatedRequest)) {
                    clearTimeout(timeoutId);
                    pendingRequestsRef.current.delete(newId);
                    reject(new Error('Failed to send queued request'));
                }
            });
        }
    }, [isConnected, sendMessage]);

    const sendRequest = useCallback((method, params = {}, options = {}) => {
        const { timeout = 30000, queueWhenOffline = true } = options;

        return new Promise((resolve, reject) => {
            const requestId = Date.now() + Math.random();
            const request = createRpcRequest(method, params, requestId);

            const timeoutId = setTimeout(() => {
                pendingRequestsRef.current.delete(requestId);
                requestQueueRef.current = requestQueueRef.current.filter(
                    item => item.request.id !== requestId
                );
                reject(new Error(`Request timeout: ${method}`));
            }, timeout);

            const requestData = { resolve, reject, timeoutId };

            if (!isConnected) {
                if (queueWhenOffline) {
                    requestQueueRef.current.push({ request, ...requestData });
                    console.log(`Queued request: ${method} (queue size: ${requestQueueRef.current.length})`);
                } else {
                    clearTimeout(timeoutId);
                    reject(new Error('WebSocket is not connected'));
                }
                return;
            }

            pendingRequestsRef.current.set(requestId, requestData);

            if (!sendMessage(request)) {
                clearTimeout(timeoutId);
                pendingRequestsRef.current.delete(requestId);
                reject(new Error('Failed to send request'));
            }
        });
    }, [sendMessage, isConnected]);

    // Очистка при размонтировании
    useEffect(() => {
        return () => {
            pendingRequestsRef.current.forEach(({ reject, timeoutId }) => {
                clearTimeout(timeoutId);
                reject(new Error('Component unmounted'));
            });

            requestQueueRef.current.forEach(({ reject, timeoutId }) => {
                clearTimeout(timeoutId);
                reject(new Error('Component unmounted'));
            });
        };
    }, []);

    return {
        ...webSocketData,
        sendRequest,
        queueSize: requestQueueRef.current.length,
        pendingRequests: pendingRequestsRef.current.size
    };
};

// Упрощенный хук для удобного использования
export const useRpcCall = (url) => {
    const { sendRequest, isConnected } = useRpcRequest(url);
    
    return {
        call: sendRequest,
        isConnected
    };
};
