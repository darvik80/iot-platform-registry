import React, { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import { useWebSocket } from '../contexts/WebSocketContext.jsx';

function DashboardPage() {
    const ws = useWebSocket();
    const [groups, setGroups] = useState([]);
    const [loading, setLoading] = useState(true);
    const requestId = useRef(0);

    useEffect(() => {
        if (!ws) return;

        const messageHandler = (event) => {
            const response = JSON.parse(event.data);

            if (response.id === requestId.current) {
                if (response.result) {
                    setGroups(response.result);
                    setLoading(false);
                } else if (response.error) {
                    console.error('RPC Error:', response.error);
                    setLoading(false);
                }
            }
        };

        ws.addEventListener('message', messageHandler);

        // Запрос данных при монтировании
        requestId.current = Date.now();
        const request = {
            jsonrpc: "2.0",
            method: "getDeviceGroups",
            id: requestId.current
        };
        ws.send(JSON.stringify(request));

        return () => {
            ws.removeEventListener('message', messageHandler);
        };
    }, [ws]);

    return (
        <div>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2><i className="bi bi-grid me-2"></i>Device Groups</h2>
                <div className="d-flex">
                    <button className="btn btn-outline-secondary me-2">
                        <i className="bi bi-plus-circle"></i> Add Group
                    </button>
                    <button
                        className="btn btn-primary"
                        onClick={() => window.location.reload()}
                    >
                        <i className="bi bi-arrow-repeat"></i> Refresh
                    </button>
                </div>
            </div>

            {loading ? (
                <div className="d-flex justify-content-center my-5">
                    <div className="spinner-border text-primary" role="status">
                        <span className="visually-hidden">Loading...</span>
                    </div>
                </div>
            ) : (
                <div className="row">
                    {groups.map((group) => (
                        <div key={group.id} className="col-md-4 mb-4">
                            <div className="card h-100">
                                <div className="card-header bg-primary text-white">
                                    <div className="d-flex justify-content-between">
                                        <h5 className="mb-0">{group.name}</h5>
                                        <span className="badge bg-light text-dark">
                                            {group.deviceCount} devices
                                        </span>
                                    </div>
                                </div>
                                <div className="card-body">
                                    <p className="card-text">{group.description}</p>
                                    <div className="mb-3">
                                        <span className={`badge ${
                                            group.status === 'active' ? 'bg-success' :
                                                group.status === 'warning' ? 'bg-warning' : 'bg-secondary'
                                        }`}>
                                            {group.status}
                                        </span>
                                    </div>
                                    <div className="progress mb-2">
                                        <div
                                            className="progress-bar"
                                            role="progressbar"
                                            style={{ width: `${group.onlinePercentage}%` }}
                                        >
                                            {group.onlinePercentage}% Online
                                        </div>
                                    </div>
                                </div>
                                <div className="card-footer bg-light">
                                    <Link
                                        to={`/devices?group=${group.id}`}
                                        className="btn btn-sm btn-outline-primary"
                                    >
                                        <i className="bi bi-list"></i> View Devices
                                    </Link>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

export default DashboardPage;
