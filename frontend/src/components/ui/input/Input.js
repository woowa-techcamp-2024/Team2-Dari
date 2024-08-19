import React from 'react';
import './Input.css';

const Input = ({type = 'text', value, onChange, placeholder = '', className = ''}) => {
    return (
        <input
            type={type}
            value={value}
            onChange={onChange}
            placeholder={placeholder}
            className={`custom-input ${className}`}
        />
    );
};

export default Input;
