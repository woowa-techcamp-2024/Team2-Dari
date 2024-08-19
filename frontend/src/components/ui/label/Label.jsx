// Label.jsx
import React from 'react';
import './Label.css';

export const Label = ({ children, htmlFor, className = '' }) => {
    return (
        <label htmlFor={htmlFor} className={`custom-label ${className}`}>
            {children}
        </label>
    );
};