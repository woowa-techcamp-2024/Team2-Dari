// Select.jsx
import React, { useState } from 'react';
import './Select.css';

export const Select = ({ children, defaultValue, onChange }) => {
    const [value, setValue] = useState(defaultValue);

    const handleChange = (newValue) => {
        setValue(newValue);
        if (onChange) onChange(newValue);
    };

    return (
        <div className="custom-select">
            {React.Children.map(children, child =>
                React.cloneElement(child, { onSelect: handleChange, selectedValue: value })
            )}
        </div>
    );
};

export const SelectTrigger = ({ children, selectedValue }) => {
    return <div className="custom-select-trigger">{selectedValue || children}</div>;
};

export const SelectContent = ({ children }) => {
    return <div className="custom-select-content">{children}</div>;
};

export const SelectItem = ({ value, children, onSelect, selectedValue }) => {
    return (
        <div
            className={`custom-select-item ${selectedValue === value ? 'selected' : ''}`}
            onClick={() => onSelect(value)}
        >
            {children}
        </div>
    );
};

export const SelectValue = ({ children }) => {
    return <span className="custom-select-value">{children}</span>;
};