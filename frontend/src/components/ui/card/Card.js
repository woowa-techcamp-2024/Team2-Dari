import React from 'react';
import './Card.css';

const Card = ({children, className = ''}) => {
    return <div className={`custom-card ${className}`}>{children}</div>;
};

export default Card;
