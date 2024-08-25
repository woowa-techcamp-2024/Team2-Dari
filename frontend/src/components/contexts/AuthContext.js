import React, { createContext, useContext, useState, useCallback } from 'react';
import apiClient from '../../utils/apiClient';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(!!localStorage.getItem('isAuthenticated'));

    const login = useCallback(() => {
        localStorage.setItem('isAuthenticated', 'true');
        setIsAuthenticated(true);
    }, []);

    const logout = useCallback(async (callback) => {
        try {
            const response = await apiClient.post('/auth/logout');
            localStorage.removeItem('isAuthenticated');
            setIsAuthenticated(false);
            if (callback && typeof callback === 'function') {
                callback();
            }
        } catch (error) {
            console.error('Logout failed:', error.response ? error.response.data : error.message);
        }
    }, []);

    return (
        <AuthContext.Provider value={{ isAuthenticated, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);