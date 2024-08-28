import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Loader2 } from 'lucide-react';
import waitClient from '../../utils/waitClient'

const TicketQueuePage = () => {
    const { festivalId, ticketId } = useParams();
    const navigate = useNavigate();
    const [relativeWaitOrder, setRelativeWaitOrder] = useState(null);
    const [absoluteWaitOrder, setAbsoluteWaitOrder] = useState(null);
    const [error, setError] = useState(null);

    const checkQueueStatus = useCallback(async () => {
        try {
            const url = `/festivals/${festivalId}/tickets/${ticketId}/purchase/wait`;
            const fullUrl = absoluteWaitOrder !== null ? `${url}?waitOrder=${absoluteWaitOrder}` : url;

            const response = await waitClient.get(fullUrl);
            const { purchasable, relativeWaitOrder, absoluteWaitOrder: newAbsoluteWaitOrder } = response.data.data;

            console.log('대기열 상태:', response.data.data);
            
            setRelativeWaitOrder(relativeWaitOrder);
            setAbsoluteWaitOrder(newAbsoluteWaitOrder);

            if (purchasable) {
                navigate(`/festivals/${festivalId}/tickets/${ticketId}/purchase`);
            }
        } catch (err) {
            setError('대기열 상태 확인 중 오류가 발생했습니다.');
            console.error('대기열 상태 확인 오류:', err);
        }
    }, [festivalId, ticketId, navigate, absoluteWaitOrder]);

    useEffect(() => {
        checkQueueStatus(); // 초기 호출

        const intervalId = setInterval(checkQueueStatus, 3000); // 3초마다 호출

        return () => clearInterval(intervalId); // 컴포넌트 언마운트 시 인터벌 제거
    }, [checkQueueStatus]);

    if (error) {
        return (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative" role="alert">
                <span className="block sm:inline">{error}</span>
            </div>
        );
    }

    return (
        <div className="max-w-2xl mx-auto p-4">
            <h1 className="text-2xl font-bold text-teal-600 mb-4">티켓 구매 대기열</h1>
            <div className="bg-white shadow-md rounded-lg p-6 mb-6">
                <h2 className="text-xl font-semibold mb-4">현재 대기 상태</h2>
                {relativeWaitOrder !== null ? (
                    <>
                        <p className="text-lg mb-4">대기 순서: {relativeWaitOrder}번째</p>
                        <div className="w-full bg-gray-200 rounded-full h-2.5 dark:bg-gray-700">
                            <div
                                className="bg-teal-600 h-2.5 rounded-full"
                                style={{ width: `${Math.max(0, 100 - relativeWaitOrder)}%` }}
                            ></div>
                        </div>
                        <p className="mt-2 text-sm text-gray-500">잠시만 기다려주세요. 곧 구매 페이지로 이동합니다.</p>
                    </>
                ) : (
                    <div className="flex items-center justify-center">
                        <Loader2 className="w-8 h-8 animate-spin text-teal-500" />
                        <span className="ml-2">대기열 정보를 불러오는 중...</span>
                    </div>
                )}
            </div>
        </div>
    );
};

export default TicketQueuePage;