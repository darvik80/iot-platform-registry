import React from 'react';

function DeviceFilters({ filters, onFilterChange, onApply, loading }) {
    const clearFilters = () => {
        onFilterChange({ target: { name: 'registry', value: '' } });
        onFilterChange({ target: { name: 'status', value: '' } });
        onFilterChange({ target: { name: 'search', value: '' } });
    };

    return (
        <div className="card mb-4">
            <div className="card-body py-3">
                {/* Одна строка, без переноса */}
                <div className="d-flex flex-nowrap align-items-end w-100 gap-2">
                    {/* Registry */}
                    <div className="d-flex flex-column" style={{ minWidth: 220 }}>
                        <label className="form-label small mb-1">Registry</label>
                        <select
                            className="form-select form-select-sm"
                            name="registry"
                            value={filters.registry}
                            onChange={onFilterChange}
                        >
                            <option value="">All Registries</option>
                            <option value="1">Production Line</option>
                            <option value="2">Warehouse Sensors</option>
                            <option value="3">Office Equipment</option>
                        </select>
                    </div>

                    {/* Status */}
                    <div className="d-flex flex-column" style={{ minWidth: 180 }}>
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

                    {/* Search (растягивается) */}
                    <div className="d-flex flex-column flex-grow-1" style={{ minWidth: 260 }}>
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

                    {/* Actions */}
                    <div className="d-flex flex-column" style={{ minWidth: 170 }}>
                        <label className="form-label small mb-1 invisible">Actions</label>
                        <div className="d-flex gap-1">
                            <button
                                className="btn btn-primary btn-sm"
                                type="button"
                                onClick={onApply}
                                disabled={loading}
                                title="Apply filters"
                            >
                                {loading ? (
                                    <span className="spinner-border spinner-border-sm" role="status" />
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
                                Clear
                            </button>
                        </div>
                    </div>
                </div>

                {/* Подсказка: на очень узких экранах появится горизонтальная прокрутка, чтобы сохранить одну строку */}
                <style>{`
                    .card-body > .d-flex.flex-nowrap {
                        overflow-x: auto;
                    }
                `}</style>
            </div>
        </div>
    );
}

export default DeviceFilters;