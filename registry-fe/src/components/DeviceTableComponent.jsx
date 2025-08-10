import React from 'react';

function DeviceTable({ devices, loading, sortConfig, onSort, onViewTelemetry, onClearFilters }) {
    return (
        <div className="card">
            <div className="card-body p-0">
                <div className="table-responsive">
                    <table className="table table-hover align-middle">
                        <thead className="table-light">
                            <tr>
                                <th onClick={() => onSort('id')}>
                                    ID {sortConfig.key === 'id' && (
                                    <i className={`bi bi-arrow-${sortConfig.direction === 'ascending' ? 'up' : 'down'}`}></i>
                                )}
                                </th>
                                <th onClick={() => onSort('name')}>
                                    Device Name {sortConfig.key === 'name' && (
                                    <i className={`bi bi-arrow-${sortConfig.direction === 'ascending' ? 'up' : 'down'}`}></i>
                                )}
                                </th>
                                <th>Group</th>
                                <th>Type</th>
                                <th onClick={() => onSort('lastSeen')}>
                                    Last Seen {sortConfig.key === 'lastSeen' && (
                                    <i className={`bi bi-arrow-${sortConfig.direction === 'ascending' ? 'up' : 'down'}`}></i>
                                )}
                                </th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {devices.length > 0 ? devices.map(device => (
                                <tr key={device.id}>
                                    <td className="text-muted">#{device.id}</td>
                                    <td>
                                        <div className="d-flex align-items-center">
                                            <div className="symbol symbol-50px me-3">
                                                <span className={`symbol-label bg-light-${device.status === 'online' ? 'success' : device.status === 'warning' ? 'warning' : 'danger'}`}>
                                                    <i className={`bi ${device.icon || 'bi-cpu'} fs-3 text-${device.status === 'online' ? 'success' : device.status === 'warning' ? 'warning' : 'danger'}`}></i>
                                                </span>
                                            </div>
                                            <div>
                                                <div className="fw-bold">{device.name}</div>
                                                <div className="text-muted">{device.model}</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <span className="badge bg-primary">{device.group}</span>
                                    </td>
                                    <td>{device.type}</td>
                                    <td>
                                        {new Date(device.lastSeen).toLocaleString()}
                                        <div className="text-muted small">
                                            {device.lastSeenHumanized}
                                        </div>
                                    </td>
                                    <td>
                                        <span className={`badge ${
                                            device.status === 'online' ? 'bg-success' :
                                                device.status === 'warning' ? 'bg-warning' : 'bg-danger'
                                        }`}>
                                            {device.status}
                                        </span>
                                    </td>
                                    <td>
                                        <button
                                            className="btn btn-sm btn-outline-primary"
                                            onClick={() => onViewTelemetry(device.id)}
                                        >
                                            <i className="bi bi-graph-up"></i> View Telemetry
                                        </button>
                                    </td>
                                </tr>
                            )) : (
                                <tr>
                                    <td colSpan="7" className="text-center py-5">
                                        {loading ? (
                                            <div className="d-flex justify-content-center">
                                                <div className="spinner-border text-primary" role="status">
                                                    <span className="visually-hidden">Loading...</span>
                                                </div>
                                            </div>
                                        ) : (
                                            <div className="text-muted">
                                                <i className="bi bi-inboxes fs-1"></i>
                                                <div className="mt-3">No devices found</div>
                                                <button
                                                    className="btn btn-link p-0"
                                                    onClick={onClearFilters}
                                                >
                                                    Clear filters
                                                </button>
                                            </div>
                                        )}
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}

export default DeviceTable;
