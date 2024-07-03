import React from 'react';
import './Loading.css';

export default function Loading() {
    return (
        <>
            <div className="overlay"></div>
            <div className="loading-popup">
                <span className="loader"></span>
                <h2 className="loading-popup-h2">Loading...</h2>
            </div>
        </>
    );
};
