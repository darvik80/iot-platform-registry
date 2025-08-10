import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useWebSocket } from '../contexts/WebSocketProvider';

function DashboardPage() {
    const { sendRequest, isConnected } = useWebSocket();
    const [registries, setRegistries] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const loadDeviceRegistries = async () => {
        try {
            setLoading(true);
            setError(null);
            
            const result = await sendRequest('getDeviceRegistries', {}, {
                timeout: 10000,
                queueWhenOffline: true
            });
            
            setRegistries(result || []);
        } catch (err) {
            console.error('Failed to load device registries:', err);
            setError(err.message);
            setLoading(false);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (isConnected) {
            loadDeviceRegistries();
        }
    }, [isConnected]);

    const handleRefresh = () => {
        loadDeviceRegistries();
    };

    return (
        <div>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2><i className="bi bi-grid me-2"></i>Device Registries</h2>
                <div className="d-flex">
                    <button className="btn btn-outline-secondary me-2">
                        <i className="bi bi-plus-circle"></i> Add Registry
                    </button>
                    <button
                        className="btn btn-primary"
                        onClick={handleRefresh}
                        disabled={loading}
                    >
                        {loading ? (
                            <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                        ) : (
                            <i className="bi bi-arrow-repeat me-2"></i>
                        )}
                        Refresh
                    </button>
                </div>
            </div>

            {error && (
                <div className="alert alert-danger" role="alert">
                    <i className="bi bi-exclamation-triangle me-2"></i>
                    {error}
                    <button 
                        className="btn btn-outline-danger btn-sm ms-2"
                        onClick={handleRefresh}
                    >
                        Retry
                    </button>
                </div>
            )}

            {loading && registries.length === 0 ? (
                <div className="d-flex justify-content-center my-5">
                    <div className="spinner-border text-primary" role="status">
                        <span className="visually-hidden">Loading...</span>
                    </div>
                </div>
            ) : (
                <div className="row">
                    {registries.length > 0 ? registries.map((registry) => (
                        <div key={registry.id} className="col-md-4 mb-4">
                            <div className="card h-100">
                                <div className="card-header bg-primary text-white">
                                    <div className="d-flex justify-content-between">
                                        <h5 className="mb-0">{registry.name}</h5>
                                        <span className="badge bg-light text-dark">
                                            {registry.deviceCount} devices
                                        </span>
                                    </div>
                                </div>
                                <div className="card-body">
                                    <p className="card-text">{registry.description}</p>
                                    <div className="mb-3">
                                        <span className={`badge ${
                                            registry.status === 'active' ? 'bg-success' :
                                                registry.status === 'warning' ? 'bg-warning' : 'bg-secondary'
                                        }`}>
                                            {registry.status}
                                        </span>
                                    </div>
                                    <div className="progress mb-2">
                                        <div
                                            className="progress-bar"
                                            role="progressbar"
                                            style={{ width: `${registry.onlinePercentage || 0}%` }}
                                        >
                                            {registry.onlinePercentage || 0}% Online
                                        </div>
                                    </div>
                                </div>
                                <div className="card-footer bg-light">
                                    <Link
                                        to={`/devices?registry=${registry.id}`}
                                        className="btn btn-sm btn-outline-primary"
                                    >
                                        <i className="bi bi-list"></i> View Devices
                                    </Link>
                                </div>
                            </div>
                        </div>
                    )) : !loading && (
                        <div className="col-12">
                            <div className="text-center py-5">
                                <i className="bi bi-inbox fs-1 text-muted"></i>
                                <h4 className="mt-3">No device registries found</h4>
                                <p className="text-muted">Start by creating your first device registry</p>
                                <button className="btn btn-primary">
                                    <i className="bi bi-plus-circle me-2"></i>
                                    Create Device Registry
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}

export default DashboardPage;
