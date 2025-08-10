import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';

import { WebSocketProvider } from './contexts/WebSocketProvider';
import Navigation from './components/NavigationBar.jsx';
import DashboardPage from './pages/DashboardPage.jsx';
import DevicesPage from './pages/DevicesPage.jsx';
import TelemetryPage from './pages/TelemetryPage.jsx';

function App() {
    return (
        <WebSocketProvider url="ws://127.0.0.1:8080/ws-rpc">
            <Router>
                <div className="App" data-bs-theme="dark">
                    <Navigation />

                    <div className="container-fluid">
                        <Routes>
                            <Route path="/" element={<DashboardPage />} />
                            <Route path="/devices" element={<DevicesPage />} />
                            <Route path="/telemetry/:deviceId" element={<TelemetryPage />} />
                        </Routes>
                    </div>
                </div>
            </Router>
        </WebSocketProvider>
    );
}

export default App;