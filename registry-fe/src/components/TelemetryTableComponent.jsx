import React from 'react';

function TelemetryTable({ telemetry }) {
    return (
        <div className="card">
            <div className="card-header">
                <h5 className="card-title mb-0">Latest Telemetry Data</h5>
            </div>
            <div className="card-body">
                <div className="table-responsive">
                    <table className="table table-hover">
                        <thead>
                            <tr>
                                <th>Timestamp</th>
                                <th>Temperature (°C)</th>
                                <th>Humidity (%)</th>
                                <th>Voltage (V)</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {telemetry.slice(0, 10).map((data, index) => (
                                <tr key={index}>
                                    <td>{new Date(data.timestamp).toLocaleTimeString()}</td>
                                    <td>
                                        <span className={`fw-bold ${
                                            data.temperature > 35 ? 'text-danger' :
                                                data.temperature < 15 ? 'text-info' : 'text-success'
                                        }`}>
                                            {data.temperature}
                                        </span>
                                    </td>
                                    <td>{data.humidity}</td>
                                    <td>
                                        <span className={`fw-bold ${
                                            data.voltage < 3.2 ? 'text-danger' :
                                                data.voltage < 3.5 ? 'text-warning' : 'text-success'
                                        }`}>
                                            {data.voltage}
                                        </span>
                                    </td>
                                    <td>
                                        <span className="badge bg-success">Normal</span>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}

export default TelemetryTable;
