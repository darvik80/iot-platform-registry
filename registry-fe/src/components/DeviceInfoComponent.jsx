import React from 'react';

function DeviceInfo({ device }) {
    return (
        <div className="card mb-4">
            <div className="card-body">
                <div className="d-flex align-items-center">
                    <div className="symbol symbol-80px me-5">
                        <span className={`symbol-label bg-light-${device.status === 'online' ? 'success' : 'danger'}`}>
                            <i className={`bi ${device.icon || 'bi-cpu'} fs-1 text-${device.status === 'online' ? 'success' : 'danger'}`}></i>
                        </span>
                    </div>
                    <div className="d-flex flex-column">
                        <div className="fs-2 fw-bold">{device.name}</div>
                        <div className="text-muted">{device.model} • {device.type}</div>
                        <div className="d-flex mt-2">
                            <span className="badge bg-primary me-2">{device.group}</span>
                            <span className={`badge ${
                                device.status === 'online' ? 'bg-success' :
                                    device.status === 'warning' ? 'bg-warning' : 'bg-danger'
                            }`}>
                                {device.status}
                            </span>
                        </div>
                    </div>
                    <div className="ms-auto d-flex flex-column">
                        <div className="text-end">
                            <span className="text-muted">Last seen:</span>
                            <div className="fw-bold">{new Date(device.lastSeen).toLocaleString()}</div>
                        </div>
                        <div className="mt-2">
                            <span className="text-muted">IP Address:</span>
                            <div className="fw-bold">{device.ip || '192.168.1.100'}</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default DeviceInfo;
