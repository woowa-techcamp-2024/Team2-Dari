import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Loader2, AlertCircle } from 'lucide-react';
import waitClient from '../../utils/waitClient'
import { useRecoilState } from 'recoil';
import { waitOrdersState } from '../../utils/atoms';
import { Button } from '../../components/ui/button';

const TicketQueuePage = () => {
    const { festivalId, ticketId } = useParams();
    const navigate = useNavigate();
    const [waitOrders, setWaitOrders] = useRecoilState(waitOrdersState);
    const [relativeWaitOrder, setRelativeWaitOrder] = useState(null);
    const [error, setError] = useState(null);
    const [isSoldOut, setIsSoldOut] = useState(false);

    const checkQueueStatus = useCallback(async () => {
        try {
            const url = `/festivals/${festivalId}/tickets/${ticketId}/purchase/wait`;
            const currentWaitOrder = waitOrders[ticketId];
            const fullUrl = currentWaitOrder ? `${url}?waitOrder=${currentWaitOrder}` : url;

            const response = await waitClient.get(fullUrl);
            const { purchasable, relativeWaitOrder, absoluteWaitOrder } = response.data.data;

            console.log('대기열 상태:', response.data.data);

            setRelativeWaitOrder(relativeWaitOrder);

            if (!currentWaitOrder) {
                setWaitOrders(prev => ({
                    ...prev,
                    [ticketId]: absoluteWaitOrder
                }));
            }

            if (purchasable) {
                navigate(`/festivals/${festivalId}/tickets/${ticketId}/purchase`);
            }
        } catch (err) {
            if (err.response && err.response.data.errorCode === 'WT-0005') {
                setIsSoldOut(true);
            } else {
                setError('대기열 상태 확인 중 오류가 발생했습니다.');
                console.error('대기열 상태 확인 오류:', err);
            }
        }
    }, [festivalId, ticketId, navigate, waitOrders, setWaitOrders]);

    useEffect(() => {
        checkQueueStatus();
        const intervalId = setInterval(checkQueueStatus, 3000);
        return () => clearInterval(intervalId);
    }, [checkQueueStatus]);

    if (isSoldOut) {
        return (
            <div className="max-w-2xl mx-auto p-4">
                <div className="bg-yellow-100 border-l-4 border-yellow-500 text-yellow-700 p-4 mb-4" role="alert">
                    <div className="flex">
                        <div className="py-1">
                            <AlertCircle className="h-6 w-6 text-yellow-500 mr-4" />
                        </div>
                        <div>
                            <p className="font-bold">티켓 매진</p>
                            <p className="text-sm">죄송합니다. 현재 이 티켓은 매진되었습니다.</p>
                        </div>
                    </div>
                </div>
                <Button 
                    onClick={() => navigate(`/festivals/${festivalId}`)}
                    className="w-full bg-teal-500 hover:bg-teal-600 text-white"
                >
                    축제 상세 페이지로 돌아가기
                </Button>
            </div>
        );
    }

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