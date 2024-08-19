// Card.jsx
import React from 'react';
import './Card.css';

export const Card = ({ children, className = '' }) => {
    return <div className={`custom-card ${className}`}>{children}</div>;
};

export const CardHeader = ({ children, className = '' }) => {
    return <div className={`custom-card-header ${className}`}>{children}</div>;
};

export const CardTitle = ({ children, className = '' }) => {
    return <h2 className={`custom-card-title ${className}`}>{children}</h2>;
};

export const CardDescription = ({ children, className = '' }) => {
    return <p className={`custom-card-description ${className}`}>{children}</p>;
};

export const CardContent = ({ children, className = '' }) => {
    return <div className={`custom-card-content ${className}`}>{children}</div>;
};

export const CardFooter = ({ children, className = '' }) => {
    return <div className={`custom-card-footer ${className}`}>{children}</div>;
};