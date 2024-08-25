import React, { createContext, useContext, useState, useCallback } from 'react';
import axios from 'axios';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(!!localStorage.getItem('isAuthenticated'));

    const login = useCallback(() => {
        localStorage.setItem('isAuthenticated', 'true');
        setIsAuthenticated(true);
    }, []);

    const logout = useCallback(async (callback) => {
        try {
            await axios.post('http://localhost:8080/api/v1/auth/logout', {}, { withCredentials: true });
            localStorage.removeItem('isAuthenticated');
            setIsAuthenticated(false);
            if (callback && typeof callback === 'function') {
                callback();
            }
        } catch (error) {
            console.error('Logout failed:', error);
        }
    }, []);

    return (
        <AuthContext.Provider value={{ isAuthenticated, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);