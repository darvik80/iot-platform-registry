import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useWebSocket } from '../contexts/WebSocketContext.jsx';
import DeviceFilters from '../components/DeviceFilters.jsx';
import DeviceTable from '../components/DeviceTableComponent.jsx';

function DevicesPage() {
    const ws = useWebSocket();
    const [devices, setDevices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filters, setFilters] = useState({
        group: '',
        status: '',
        search: ''
    });
    const [sortConfig, setSortConfig] = useState({
        key: 'name',
        direction: 'ascending'
    });
    const requestId = useRef(0);
    const navigate = useNavigate();

    // Применение фильтров и сортировки
    const applyFiltersAndSort = () => {
        if (!ws) return;

        setLoading(true);
        requestId.current = Date.now();
        const request = {
            jsonrpc: "2.0",
            method: "getDevices",
            params: {
                ...filters,
                sortBy: sortConfig.key,
                sortOrder: sortConfig.direction === 'ascending' ? 'asc' : 'desc'
            },
            id: requestId.current
        };
        ws.send(JSON.stringify(request));
    };

    // Запрос данных при монтировании и изменении фильтров/сортировки
    useEffect(() => {
        if (!ws) return;

        const messageHandler = (event) => {
            const response = JSON.parse(event.data);

            if (response.id === requestId.current) {
                if (response.result) {
                    setDevices(response.result);
                    setLoading(false);
                } else if (response.error) {
                    console.error('RPC Error:', response.error);
                    setLoading(false);
                }
            }
        };

        ws.addEventListener('message', messageHandler);
        applyFiltersAndSort();

        return () => {
            ws.removeEventListener('message', messageHandler);
        };
    }, [ws, filters, sortConfig]);

    // Обработчик сортировки
    const handleSort = (key) => {
        let direction = 'ascending';
        if (sortConfig.key === key && sortConfig.direction === 'ascending') {
            direction = 'descending';
        }
        setSortConfig({ key, direction });
    };

    // Обработчик фильтров
    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setFilters(prev => ({ ...prev, [name]: value }));
    };

    // Переход на страницу телеметрии
    const viewTelemetry = (deviceId) => {
        navigate(`/telemetry/${deviceId}`);
    };

    // Очистка фильтров
    const clearFilters = () => {
        setFilters({ group: '', status: '', search: '' });
    };

    return (
        <div>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2><i className="bi bi-device-ssd me-2"></i>Devices</h2>
                <div>
                    <button
                        className="btn btn-primary"
                        onClick={applyFiltersAndSort}
                        disabled={loading}
                    >
                        {loading ? (
                            <span className="spinner-border spinner-border-sm" role="status"></span>
                        ) : (
                            <span><i className="bi bi-funnel"></i> Apply Filters</span>
                        )}
                    </button>
                </div>
            </div>

            <DeviceFilters 
                filters={filters}
                onFilterChange={handleFilterChange}
                onApply={applyFiltersAndSort}
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
