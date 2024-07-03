import React from 'react';
import './Error.css';
import error from '../../../../images/error.png';

export default function Error ({ message, onClose }) {
    return (
        <>
            <div className="overlay" onClick={onClose}></div>
            <div className="error-popup">
                <img src={error} alt="Account Details" className="error-popup-img"/>
                <h2 className="error-popup-h2">Error</h2>
                <p className="error-popup-p">{message}</p>
                <button className="error-popup-button" onClick={onClose}>Ok</button>
            </div>
        </>
    );
};
