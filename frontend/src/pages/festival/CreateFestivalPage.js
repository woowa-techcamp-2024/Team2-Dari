import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../../utils/apiClient';
import { Input } from '../../components/ui/input';
import { Button } from '../../components/ui/button';
import { Textarea } from '../../components/ui/textarea';

const CreateFestivalPage = () => {
    const navigate = useNavigate();
    const [festivalData, setFestivalData] = useState({
        title: '',
        description: '',
        startTime: '',
        endTime: ''
    });
    const [error, setError] = useState(null);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFestivalData(prev => ({ ...prev, [name]: value }));
    };

    const handleDateTimeChange = (e) => {
        const { name, value } = e.target;
        const isoDateTime = value ? new Date(value).toISOString() : '';
        setFestivalData(prev => ({ ...prev, [name]: isoDateTime }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await apiClient.post('/festivals', festivalData);
            navigate(`/festivals/${response.data.data.festivalId}`);
        } catch (err) {
            if (err.response && err.response.data) {
                // 서버에서 반환된 에러 메시지를 사용하여 에러 상태 업데이트
                setError(err.response.data.message || '축제 생성 중 오류가 발생했습니다.');
            } else {
                setError('축제 생성 중 오류가 발생했습니다.');
            }
        }
    };

    return (
        <div className="max-w-2xl mx-auto p-4">
            <h1 className="text-2xl font-bold text-teal-600 mb-4">새로운 축제 생성</h1>
            {error && <p className="text-red-500 mb-4">{error}</p>}
            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label htmlFor="title" className="block text-sm font-medium text-gray-700">축제 제목</label>
                    <Input
                        id="title"
                        name="title"
                        value={festivalData.title}
                        onChange={handleChange}
                        placeholder="축제 제목을 입력하세요"
                        required
                        className="mt-1"
                    />
                </div>
                <div>
                    <label htmlFor="description" className="block text-sm font-medium text-gray-700">축제 설명</label>
                    <Textarea
                        id="description"
                        name="description"
                        value={festivalData.description}
                        onChange={handleChange}
                        placeholder="축제에 대한 설명을 입력하세요"
                        required
                        className="mt-1"
                    />
                </div>
                <div>
                    <label htmlFor="startTime" className="block text-sm font-medium text-gray-700">시작 시간</label>
                    <Input
                        id="startTime"
                        name="startTime"
                        type="datetime-local"
                        value={festivalData.startTime ? new Date(festivalData.startTime).toISOString().slice(0, 16) : ''}
                        onChange={handleDateTimeChange}
                        required
                        className="mt-1"
                    />
                </div>
                <div>
                    <label htmlFor="endTime" className="block text-sm font-medium text-gray-700">종료 시간</label>
                    <Input
                        id="endTime"
                        name="endTime"
                        type="datetime-local"
                        value={festivalData.endTime ? new Date(festivalData.endTime).toISOString().slice(0, 16) : ''}
                        onChange={handleDateTimeChange}
                        required
                        className="mt-1"
                    />
                </div>
                <Button type="submit" className="w-full bg-teal-500 hover:bg-teal-600 text-white">
                    축제 생성
                </Button>
            </form>
        </div>
    );
};

export default CreateFestivalPage;
