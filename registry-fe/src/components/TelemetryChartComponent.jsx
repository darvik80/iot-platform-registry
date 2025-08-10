
import React from 'react';

function TelemetryChart({ telemetry, timeRange }) {
    return (
        <div className="card mb-4">
            <div className="card-header">
                <h5 className="card-title mb-0">Temperature Readings</h5>
            </div>
            <div className="card-body">
                <div className="chart-container" style={{ height: '300px' }}>
                    {telemetry.length > 0 ? (
                        <div className="p-3">
                            {/* Здесь будет график (например, с использованием Chart.js) */}
                            <div className="bg-light border rounded p-4 text-center">
                                <i className="bi bi-graph-up fs-1 text-primary"></i>
                                <div className="mt-3">Temperature chart visualization</div>
                                <div className="text-muted small">
                                    {telemetry.length} readings for the last {timeRange}
                                </div>
                            </div>
                        </div>
                    ) : (
                        <div className="text-center py-5">
                            <i className="bi bi-thermometer-half fs-1 text-muted"></i>
                            <div className="mt-3">No temperature data available</div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default TelemetryChart;
