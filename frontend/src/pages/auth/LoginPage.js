import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Input } from "../../components/ui/input";
import { Button } from "../../components/ui/button";
import { useAuth } from '../../components/ui/AuthContext';

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
                { withCredentials: true } // 쿠키 자동 처리
            );
            login(); // 로그인 상태 업데이트
            navigate('/'); // 로그인 후 메인 페이지로 이동
        } catch (error) {
            setError('로그인에 실패했습니다. 다시 시도해주세요.');
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-background">
            <form className="p-8 bg-white shadow-lg rounded-lg" onSubmit={handleLogin}>
                <h1 className="text-2xl font-bold mb-6">로그인</h1>
                {error && <p className="text-destructive mb-4">{error}</p>}
                <div className="mb-4">
                    <Input
                        type="email"
                        placeholder="이메일을 입력하세요"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className="w-full"
                    />
                </div>
                <Button type="submit" variant="primary" className="w-full">로그인</Button>
            </form>
        </div>
    );
};

export default LoginPage;
