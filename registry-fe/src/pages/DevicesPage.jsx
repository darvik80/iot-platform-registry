import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useWebSocket } from '../contexts/WebSocketProvider';
import DeviceFilters from '../components/DeviceFilters';
import DeviceTable from '../components/DeviceTableComponent.jsx';

function DevicesPage() {
    const { sendRequest, isConnected } = useWebSocket();
    const [devices, setDevices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [filters, setFilters] = useState({
        registry: '',
        status: '',
        search: ''
    });
    const [sortConfig, setSortConfig] = useState({
        key: 'name',
        direction: 'ascending'
    });
    const navigate = useNavigate();

    const loadDevices = async () => {
        try {
            setLoading(true);
            setError(null);

            const result = await sendRequest('getDevices', {
                ...filters,
                sortBy: sortConfig.key,
                sortOrder: sortConfig.direction === 'ascending' ? 'asc' : 'desc'
            }, {
                timeout: 10000,
                queueWhenOffline: true
            });

            setDevices(result || []);
        } catch (err) {
            console.error('Failed to load devices:', err);
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (isConnected) {
            loadDevices();
        }
    }, [isConnected, filters, sortConfig]);

    const handleSort = (key) => {
        let direction = 'ascending';
        if (sortConfig.key === key && sortConfig.direction === 'ascending') {
            direction = 'descending';
        }
        setSortConfig({ key, direction });
    };

    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setFilters(prev => ({ ...prev, [name]: value }));
    };

    const viewTelemetry = (deviceId) => {
        navigate(`/telemetry/${deviceId}`);
    };

    const clearFilters = () => {
        setFilters({ registry: '', status: '', search: '' });
    };

    return (
        <div>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2><i className="bi bi-device-ssd me-2"></i>Devices</h2>
                <div>
                    <button
                        className="btn btn-primary"
                        onClick={loadDevices}
                        disabled={loading}
                    >
                        {loading ? (
                            <span className="spinner-border spinner-border-sm" role="status"></span>
                        ) : (
                            <span><i className="bi bi-funnel"></i> Refresh</span>
                        )}
                    </button>
                </div>
            </div>

            {error && (
                <div className="alert alert-danger" role="alert">
                    <i className="bi bi-exclamation-triangle me-2"></i>
                    {error}
                    <button
                        className="btn btn-outline-danger btn-sm ms-2"
                        onClick={loadDevices}
                    >
                        Retry
                    </button>
                </div>
            )}

            <DeviceFilters
                filters={filters}
                onFilterChange={handleFilterChange}
                onApply={loadDevices}
                loading={loading}
            />

            <DeviceTable
                devices={devices}
                loading={loading}
                sortConfig={sortConfig}
                onSort={handleSort}
                onViewTelemetry={viewTelemetry}
                onClearFilters={clearFilters}
            />
        </div>
    );
}

export default DevicesPage;