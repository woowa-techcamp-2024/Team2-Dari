// Table.jsx
import React from 'react';
import './Table.css';

export const Table = ({ children, className = '' }) => {
    return <table className={`custom-table ${className}`}>{children}</table>;
};

export const TableHeader = ({ children }) => {
    return <thead>{children}</thead>;
};

export const TableRow = ({ children }) => {
    return <tr>{children}</tr>;
};

export const TableHead = ({ children }) => {
    return <th>{children}</th>;
};

export const TableBody = ({ children }) => {
    return <tbody>{children}</tbody>;
};

export const TableCell = ({ children }) => {
    return <td>{children}</td>;
};