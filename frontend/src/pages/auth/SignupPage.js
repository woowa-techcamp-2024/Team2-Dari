import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Input } from "../../components/ui/input";
import { Button } from "../../components/ui/button";
import apiClient from '../../utils/apiClient';

const SignupPage = () => {
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [profileImg, setProfileImg] = useState('');
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const handleSignup = async (e) => {
        e.preventDefault();
        try {
            const response = await apiClient.post(
                '/mbmer/signup',
                {name, email, profileImg}
            )
            navigate('/login'); // 회원가입 성공 시 로그인 페이지로 이동
        } catch (error) {
            setError('회원가입에 실패했습니다. 다시 시도해주세요.');
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-100">
            <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
                <h1 className="text-3xl font-bold mb-6 text-center text-teal-600">회원가입</h1>
                {error && <p className="text-red-500 mb-4 text-center">{error}</p>}
                <form onSubmit={handleSignup}>
                    <div className="mb-4">
                        <Input
                            type="text"
                            placeholder="이름을 입력하세요"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-teal-500"
                        />
                    </div>
                    <div className="mb-4">
                        <Input
                            type="email"
                            placeholder="이메일을 입력하세요"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-teal-500"
                        />
                    </div>
                    <div className="mb-4">
                        <Input
                            type="text"
                            placeholder="프로필 이미지 URL을 입력하세요"
                            value={profileImg}
                            onChange={(e) => setProfileImg(e.target.value)}
                            className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-teal-500"
                        />
                    </div>
                    <Button 
                        type="submit" 
                        className="w-full bg-teal-500 text-white py-2 rounded-md hover:bg-teal-600 transition duration-300"
                    >
                        회원가입
                    </Button>
                </form>
                <p className="mt-4 text-center text-gray-600">
                    이미 계정이 있으신가요? <Link to="/login" className="text-teal-500 hover:underline">로그인</Link>
                </p>
            </div>
        </div>
    );
};

export default SignupPage;