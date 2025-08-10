import React from 'react';
import { useWebSocket } from '../contexts/WebSocketProvider';

function ConnectionStatus() {
    const { 
        status, 
        lastError, 
        isConnected, 
        isOnline,
        queueSize,
        pendingRequests,
        connect, 
        disconnect 
    } = useWebSocket();

    const getStatusBadgeClass = () => {
        if (!isOnline) return 'bg-secondary';
        if (isConnected) return 'bg-success';
        if (status.includes('Connecting') || status.includes('Reconnecting')) return 'bg-warning';
        return 'bg-danger';
    };

    const getStatusIcon = () => {
        if (!isOnline) return 'bi-wifi-off';
        if (isConnected) return 'bi-wifi';
        if (status.includes('Connecting') || status.includes('Reconnecting')) return 'bi-arrow-repeat';
        return 'bi-wifi-off';
    };

    return (
        <div className="d-flex align-items-center">
            <span className={`badge ${getStatusBadgeClass()} me-2`}>
                <i className={`bi ${getStatusIcon()} me-1`}></i>
                {status}
            </span>
            
            {queueSize > 0 && (
                <span className="badge bg-info me-2" title={`${queueSize} requests queued`}>
                    <i className="bi bi-clock-history me-1"></i>
                    {queueSize}
                </span>
            )}
            
            {pendingRequests > 0 && (
                <span className="badge bg-primary me-2" title={`${pendingRequests} requests pending`}>
                    <i className="bi bi-hourglass-split me-1"></i>
                    {pendingRequests}
                </span>
            )}
            
            <div className="btn-group btn-group-sm">
                {!isConnected ? (
                    <button 
                        className="btn btn-outline-primary btn-sm"
                        onClick={connect}
                        disabled={status.includes('Connecting') || !isOnline}
                        title="Reconnect"
                    >
                        <i className="bi bi-arrow-clockwise"></i>
                    </button>
                ) : (
                    <button 
                        className="btn btn-outline-secondary btn-sm"
                        onClick={disconnect}
                        title="Disconnect"
                    >
                        <i className="bi bi-x-circle"></i>
                    </button>
                )}
            </div>
            
            {lastError && (
                <div className="ms-2">
                    <span 
                        className="badge bg-danger" 
                        title={lastError}
                        data-bs-toggle="tooltip"
                    >
                        <i className="bi bi-exclamation-triangle"></i>
                    </span>
                </div>
            )}
        </div>
    );
}

export default ConnectionStatus;
