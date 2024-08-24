import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import apiClient from '../../utils/apiClient';
import { Button } from '../../components/ui/button';
import { Loader2 } from 'lucide-react';

const TicketPurchasePage = () => {
    const { festivalId, ticketId } = useParams();
    const navigate = useNavigate();
    const [purchaseInfo, setPurchaseInfo] = useState(null);
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const checkPurchaseAvailability = async () => {
            try {
                const response = await apiClient.get(`/festivals/${festivalId}/tickets/${ticketId}/purchase/check`);
                if (!response.data.data.purchasable) {
                    setError('현재 이 티켓은 구매할 수 없습니다.');
                    return;
                }
                await fetchPurchaseInfo();
            } catch (err) {
                if (err.response && err.response.status === 401) {
                    // 인증되지 않은 경우 로그인 페이지로 리다이렉트
                    navigate('/login', { state: { from: `/festivals/${festivalId}/tickets/${ticketId}/purchase` } });
                } else {
                    setError('티켓 구매 가능 여부 확인 중 오류가 발생했습니다.');
                }
            } finally {
                setIsLoading(false);
            }
        };

        const fetchPurchaseInfo = async () => {
            try {
                const response = await apiClient.get(`/festivals/${festivalId}/tickets/${ticketId}/purchase`);
                setPurchaseInfo(response.data.data);
            } catch (err) {
                setError('티켓 정보를 불러오는 중 오류가 발생했습니다.');
            }
        };

        checkPurchaseAvailability();
    }, [festivalId, ticketId, navigate]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            // 실제 결제 로직을 구현합니다.
            // 예: const response = await apiClient.post(`/festivals/${festivalId}/tickets/${ticketId}/purchase`, purchaseData);
            console.log('결제 진행');
            // 성공 시 처리 (예: 결제 완료 페이지로 이동)
            // navigate('/purchase/complete', { state: { purchaseInfo: response.data } });
        } catch (err) {
            setError('결제 처리 중 오류가 발생했습니다.');
        }
    };

    if (isLoading) {
        return (
            <div className="flex justify-center items-center min-h-screen">
                <Loader2 className="w-8 h-8 animate-spin text-teal-500" />
                <span className="ml-2 text-gray-600">티켓 정보를 불러오는 중...</span>
            </div>
        );
    }

    if (error) {
        return <div className="text-red-500 text-center p-4">{error}</div>;
    }

    if (!purchaseInfo) {
        return <div className="text-center p-4">티켓 정보를 찾을 수 없습니다.</div>;
    }

    return (
        <div className="max-w-2xl mx-auto p-4">
            <h1 className="text-2xl font-bold text-teal-600 mb-4">티켓 구매</h1>
            <div className="bg-white shadow-md rounded-lg p-6 mb-6">
                <h2 className="text-xl font-semibold mb-2">{purchaseInfo.festivalTitle}</h2>
                <p className="text-gray-600 mb-4">{purchaseInfo.ticketName}</p>
                <p className="font-bold text-lg mb-2">가격: ₩{purchaseInfo.ticketPrice.toLocaleString()}</p>
                <p className="text-sm text-gray-500">남은 수량: {purchaseInfo.remainTicketQuantity}</p>
            </div>
            <form onSubmit={handleSubmit} className="space-y-4">
                <div className="pt-4">
                    <Button type="submit" className="w-full bg-teal-500 hover:bg-teal-600 text-white">
                        결제하기 (₩{purchaseInfo.ticketPrice.toLocaleString()})
                    </Button>
                </div>
            </form>
        </div>
    );
};

export default TicketPurchasePage;