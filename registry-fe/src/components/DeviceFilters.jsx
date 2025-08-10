import React from 'react';

function DeviceFilters({ filters, onFilterChange, onApply, loading }) {
    return (
        <div className="card mb-4">
            <div className="card-body">
                <div className="row g-3">
                    <div className="col-md-3">
                        <label className="form-label">Group</label>
                        <select
                            className="form-select"
                            name="group"
                            value={filters.group}
                            onChange={onFilterChange}
                        >
                            <option value="">All Groups</option>
                            <option value="1">Production Line</option>
                            <option value="2">Warehouse Sensors</option>
                            <option value="3">Office Equipment</option>
                        </select>
                    </div>

                    <div className="col-md-3">
                        <label className="form-label">Status</label>
                        <select
                            className="form-select"
                            name="status"
                            value={filters.status}
                            onChange={onFilterChange}
                        >
                            <option value="">All Statuses</option>
                            <option value="online">Online</option>
                            <option value="offline">Offline</option>
                            <option value="warning">Warning</option>
                            <option value="error">Error</option>
                        </select>
                    </div>

                    <div className="col-md-6">
                        <label className="form-label">Search</label>
                        <div className="input-group">
                            <input
                                type="text"
                                className="form-control"
                                placeholder="Search by device name or ID..."
                                name="search"
                                value={filters.search}
                                onChange={onFilterChange}
                            />
                            <button className="btn btn-outline-secondary" type="button">
                                <i className="bi bi-search"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default DeviceFilters;
