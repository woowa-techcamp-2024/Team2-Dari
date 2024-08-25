import React from 'react';
import './Input.css';

const Input = React.forwardRef(({type = 'text', value, onChange, placeholder = '', className = '', ...props}, ref) => {
    return (
        <input
            type={type}
            value={value}
            onChange={onChange}
            placeholder={placeholder}
            className={`custom-input ${className}`}
            ref={ref}
            {...props}
        />
    );
});

Input.displayName = 'Input';

export default Input;