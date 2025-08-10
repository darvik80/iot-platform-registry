import React from 'react';
import { Link } from 'react-router-dom';
import ConnectionStatus from "./ConnectionStatusComponent.jsx";

function Navigation({ status }) {
    return (
        <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
            <div className="container-fluid">
                <Link className="navbar-brand" to="/">
                    <i className="bi bi-cpu me-2"></i>IoT Dashboard
                </Link>
                <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                    <span className="navbar-toggler-icon"></span>
                </button>
                <div className="collapse navbar-collapse" id="navbarNav">
                    <ul className="navbar-nav">
                        <li className="nav-item">
                            <Link className="nav-link" to="/">
                                <i className="bi bi-grid"></i> Dashboard
                            </Link>
                        </li>
                        <li className="nav-item">
                            <Link className="nav-link" to="/devices">
                                <i className="bi bi-device-ssd"></i> Devices
                            </Link>
                        </li>
                    </ul>
                    <span className="navbar-text ms-auto">
                        <ConnectionStatus/>
                    </span>
                </div>
            </div>
        </nav>
    );
}

export default Navigation;
