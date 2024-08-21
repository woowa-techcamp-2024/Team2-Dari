import React from 'react';
import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({ children }) => {
    const session = localStorage.getItem('session'); // 예: 세션 정보는 로컬 스토리지에 저장되어 있다고 가정
    if (!session) {
        return <Navigate to="/login" replace />;
    }
    return children;
};

export default ProtectedRoute;
