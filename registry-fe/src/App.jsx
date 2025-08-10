import React, { useState, useEffect, useRef } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';

import WebSocketContext from './contexts/WebSocketContext.jsx';
import Navigation from './components/NavigationBar.jsx';
import DashboardPage from './pages/DashboardPage.jsx';
import DevicesPage from './pages/DevicesPage.jsx';
import TelemetryPage from './pages/TelemetryPage.jsx';

function App() {
    const [ws, setWs] = useState(null);
    const [status, setStatus] = useState('Connecting...');
    const wsRef = useRef(null);

    // Инициализация WebSocket
    useEffect(() => {
        const websocket = new WebSocket('ws://localhost:8080/ws-rpc');
        wsRef.current = websocket;

        websocket.onopen = () => {
            setStatus('Connected');
            setWs(websocket);
        };

        websocket.onerror = (error) => {
            setStatus('Connection error');
            console.error('WebSocket error:', error);
        };

        websocket.onclose = () => {
            setStatus('Disconnected');
        };

        return () => {
            websocket.close();
        };
    }, []);

    return (
        <WebSocketContext.Provider value={ws}>
            <Router>
                <div className="App" data-bs-theme="dark">
                    <Navigation status={status} />

                    <div className="container-fluid">
                        <Routes>
                            <Route path="/" element={<DashboardPage />} />
                            <Route path="/devices" element={<DevicesPage />} />
                            <Route path="/telemetry/:deviceId" element={<TelemetryPage />} />
                        </Routes>
                    </div>
                </div>
            </Router>
        </WebSocketContext.Provider>
    );
}

export default App;