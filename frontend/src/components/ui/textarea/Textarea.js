import React from 'react';
import './Textarea.css';

const Textarea = React.forwardRef(({className = '', ...props}, ref) => {
    return (
        <textarea 
            className={`custom-textarea ${className}`}
            ref={ref}
            {...props}
        />
    );
});

Textarea.displayName = 'Textarea';

export default Textarea;