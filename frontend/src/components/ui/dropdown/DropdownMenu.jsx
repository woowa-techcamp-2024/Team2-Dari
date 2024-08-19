// DropdownMenu.jsx
import React, { useState } from 'react';
import './DropdownMenu.css';

export const DropdownMenu = ({ children }) => {
    return <div className="dropdown-menu">{children}</div>;
};

export const DropdownMenuTrigger = ({ children, onClick }) => {
    return (
        <div className="dropdown-menu-trigger" onClick={onClick}>
            {children}
        </div>
    );
};

export const DropdownMenuContent = ({ children }) => {
    return <div className="dropdown-menu-content">{children}</div>;
};

export const DropdownMenuLabel = ({ children }) => {
    return <div className="dropdown-menu-label">{children}</div>;
};

export const DropdownMenuSeparator = () => {
    return <hr className="dropdown-menu-separator" />;
};

export const DropdownMenuItem = ({ children, onClick }) => {
    return (
        <div className="dropdown-menu-item" onClick={onClick}>
            {children}
        </div>
    );
};