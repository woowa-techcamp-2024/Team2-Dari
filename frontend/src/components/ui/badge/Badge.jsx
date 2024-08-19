// Badge.jsx
import React from 'react';
import './Badge.css';

export const Badge = ({ children, variant = 'default', className = '' }) => {
    return <span className={`custom-badge ${variant} ${className}`}>{children}</span>;
};