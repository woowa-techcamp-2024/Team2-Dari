import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import { Input } from "../../components/ui/input";
import { Button } from "../../components/ui/button";
import { useAuth } from '../../components/contexts/AuthContext';

const LoginPage = () => {
    const [email, setEmail] = useState('');
    const [error, setError] = useState(null);
    const navigate = useNavigate();
    const { login } = useAuth();

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post(
                'http://localhost:8080/api/v1/auth/login',
                { email },
                { withCredentials: true }
            );
            login();
            navigate('/');
        } catch (error) {
            setError('로그인에 실패했습니다. 다시 시도해주세요.');
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-100">
            <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
                <h1 className="text-3xl font-bold mb-6 text-center text-teal-600">로그인</h1>
                {error && <p className="text-red-500 mb-4 text-center">{error}</p>}
                <form onSubmit={handleLogin}>
                    <div className="mb-4">
                        <Input
                            type="email"
                            placeholder="이메일을 입력하세요"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-teal-500"
                        />
                    </div>
                    <Button 
                        type="submit" 
                        className="w-full bg-teal-500 text-white py-2 rounded-md hover:bg-teal-600 transition duration-300"
                    >
                        로그인
                    </Button>
                </form>
                <p className="mt-4 text-center text-gray-600">
                    계정이 없으신가요? <Link to="/signup" className="text-teal-500 hover:underline">회원가입</Link>
                </p>
            </div>
        </div>
    );
};

export default LoginPage;