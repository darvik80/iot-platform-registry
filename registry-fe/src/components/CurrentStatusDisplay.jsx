import React from 'react';

function CurrentStatus({ telemetry }) {
    const currentData = telemetry.length > 0 ? telemetry[telemetry.length - 1] : null;

    return (
        <div className="card mb-4">
            <div className="card-header">
                <h5 className="card-title mb-0">Current Status</h5>
            </div>
            <div className="card-body">
                <div className="d-flex flex-column">
                    <div className="d-flex align-items-center mb-4">
                        <div className="symbol symbol-50px me-4">
                            <span className="symbol-label bg-light-success">
                                <i className="bi bi-thermometer-sun fs-1 text-success"></i>
                            </span>
                        </div>
                        <div>
                            <div className="fs-3 fw-bold">
                                {currentData ? `${currentData.temperature}°C` : 'N/A'}
                            </div>
                            <div className="text-muted">Current Temperature</div>
                        </div>
                    </div>

                    <div className="d-flex align-items-center mb-4">
                        <div className="symbol symbol-50px me-4">
                            <span className="symbol-label bg-light-info">
                                <i className="bi bi-droplet fs-1 text-info"></i>
                            </span>
                        </div>
                        <div>
                            <div className="fs-3 fw-bold">
                                {currentData ? `${currentData.humidity}%` : 'N/A'}
                            </div>
                            <div className="text-muted">Humidity</div>
                        </div>
                    </div>

                    <div className="d-flex align-items-center">
                        <div className="symbol symbol-50px me-4">
                            <span className="symbol-label bg-light-primary">
                                <i className="bi bi-lightning fs-1 text-primary"></i>
                            </span>
                        </div>
                        <div>
                            <div className="fs-3 fw-bold">
                                {currentData ? `${currentData.voltage}V` : 'N/A'}
                            </div>
                            <div className="text-muted">Voltage</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default CurrentStatus;
