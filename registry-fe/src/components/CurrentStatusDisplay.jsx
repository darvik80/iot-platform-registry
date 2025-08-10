import React from 'react';

function DeviceFilters({ filters, onFilterChange, onApply, loading }) {
    const clearFilters = () => {
        onFilterChange({ target: { name: 'group', value: '' } });
        onFilterChange({ target: { name: 'status', value: '' } });
        onFilterChange({ target: { name: 'search', value: '' } });
    };

    return (
        <div className="card mb-4">
            <div className="card-body py-3">
                <div className="row g-2 align-items-end">
                    {/* Group Filter */}
                    <div className="col-lg-2 col-md-3">
                        <label className="form-label small mb-1">Group</label>
                        <select
                            className="form-select form-select-sm"
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

                    {/* Status Filter */}
                    <div className="col-lg-2 col-md-3">
                        <label className="form-label small mb-1">Status</label>
                        <select
                            className="form-select form-select-sm"
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

                    {/* Search Filter */}
                    <div className="col-lg-5 col-md-4">
                        <label className="form-label small mb-1">Search</label>
                        <input
                            type="text"
                            className="form-control form-control-sm"
                            placeholder="Search by device name or ID..."
                            name="search"
                            value={filters.search}
                            onChange={onFilterChange}
                        />
                    </div>

                    {/* Action Buttons */}
                    <div className="col-lg-3 col-md-2">
                        <div className="d-flex gap-1">
                            <button 
                                className="btn btn-primary btn-sm"
                                type="button"
                                onClick={onApply}
                                disabled={loading}
                                title="Apply filters"
                            >
                                {loading ? (
                                    <span className="spinner-border spinner-border-sm" role="status"></span>
                                ) : (
                                    <>
                                        <i className="bi bi-search me-1"></i>
                                        Apply
                                    </>
                                )}
                            </button>
                            <button 
                                className="btn btn-outline-secondary btn-sm"
                                type="button"
                                onClick={clearFilters}
                                title="Clear all filters"
                            >
                                <i className="bi bi-x-circle"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default DeviceFilters;
