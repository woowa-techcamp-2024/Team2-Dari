// Textarea.jsx
import React from 'react';
import './Textarea.css';

export const Textarea = ({ rows = 3, placeholder, value, onChange, className = '' }) => {
    return (
        <textarea
            rows={rows}
            placeholder={placeholder}
            value={value}
            onChange={onChange}
            className={`custom-textarea ${className}`}
        />
    );
};