import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useWebSocket } from '../contexts/WebSocketProvider.jsx';
import DeviceInfo from '../components/DeviceInfoComponent.jsx';
import TelemetryChart from '../components/TelemetryChartComponent.jsx';
import CurrentStatus from '../components/CurrentStatusDisplay.jsx';
import TelemetryTable from '../components/TelemetryTableComponent.jsx';

function TelemetryPage() {
    const { deviceId } = useParams();
    const ws = useWebSocket();
    const [device, setDevice] = useState(null);
    const [telemetry, setTelemetry] = useState([]);
    const [loading, setLoading] = useState(true);
    const [timeRange, setTimeRange] = useState('1h');
    const requestId = useRef(0);
    const navigate = useNavigate();

    useEffect(() => {
        if (!ws || !deviceId) return;

        const messageHandler = (event) => {
            const response = JSON.parse(event.data);

            if (response.id === requestId.current) {
                if (response.result) {
                    if (response.result.device) {
                        setDevice(response.result.device);
                    }
                    if (response.result.telemetry) {
                        setTelemetry(response.result.telemetry);
                    }
                    setLoading(false);
                } else if (response.error) {
                    console.error('RPC Error:', response.error);
                    setLoading(false);
                }
            }
        };

        ws.addEventListener('message', messageHandler);

        // Запрос данных устройства и телеметрии
        requestId.current = Date.now();
        const request = {
            jsonrpc: "2.0",
            method: "getDeviceTelemetry",
            params: {
                deviceId,
                timeRange
            },
            id: requestId.current
        };
        ws.send(JSON.stringify(request));

        return () => {
            ws.removeEventListener('message', messageHandler);
        };
    }, [ws, deviceId, timeRange]);

    const handleTimeRangeChange = (range) => {
        setTimeRange(range);
        setLoading(true);
    };

    return (
        <div>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <button
                    className="btn btn-outline-secondary"
                    onClick={() => navigate('/devices')}
                >
                    <i className="bi bi-arrow-left"></i> Back to Devices
                </button>
                <h2 className="mb-0">Device Telemetry</h2>
                <div className="btn-group">
                    {['15m', '1h', '6h', '24h'].map(range => (
                        <button
                            key={range}
                            className={`btn ${timeRange === range ? 'btn-primary' : 'btn-outline-primary'}`}
                            onClick={() => handleTimeRangeChange(range)}
                        >
                            {range}
                        </button>
                    ))}
                </div>
            </div>

            {loading ? (
                <div className="d-flex justify-content-center my-5">
                    <div className="spinner-border text-primary" role="status">
                        <span className="visually-hidden">Loading...</span>
                    </div>
                </div>
            ) : device ? (
                <>
                    <DeviceInfo device={device} />
                    
                    <div className="row">
                        <div className="col-lg-8">
                            <TelemetryChart telemetry={telemetry} timeRange={timeRange} />
                        </div>

                        <div className="col-lg-4">
                            <CurrentStatus telemetry={telemetry} />
                        </div>
                    </div>

                    <TelemetryTable telemetry={telemetry} />
                </>
            ) : (
                <div className="alert alert-danger">
                    <i className="bi bi-exclamation-circle"></i> Device not found
                </div>
            )}
        </div>
    );
}

export default TelemetryPage;
