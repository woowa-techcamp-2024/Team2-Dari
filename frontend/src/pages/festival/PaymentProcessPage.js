import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import apiClient from '../../utils/apiClient';
import { Loader2, AlertCircle, CheckCircle } from 'lucide-react';
import { Button } from '../../components/ui/button';

const PaymentProcessPage = () => {
    const { festivalId, ticketId } = useParams();
    const [paymentId, setPaymentId] = useState(null);
    const [paymentStatus, setPaymentStatus] = useState(null);
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [elapsedTime, setElapsedTime] = useState(0);
    const navigate = useNavigate();

    const startPayment = useCallback(async () => {
        try {
            const response = await apiClient.post(`/festivals/${festivalId}/tickets/${ticketId}/purchase`);
            console.log('Payment started:', response.data);
            setPaymentId(response.data.data.paymentId);
            setPaymentStatus('PENDING');
        } catch (err) {
            console.error('Error starting payment:', err);
            setError('결제 시작 중 오류가 발생했습니다.');
        } finally {
            setIsLoading(false);
        }
    }, [festivalId, ticketId]);

    const checkPaymentStatus = useCallback(async () => {
        if (!paymentId) {
            console.log('No paymentId available');
            return;
        }

        console.log('Checking payment status for:', paymentId);
        try {
            const response = await apiClient.get(`festivals/${festivalId}/tickets/${ticketId}/purchase/${paymentId}/status`);
            console.log('Payment status response:', response.data);
            setPaymentStatus(response.data.data.paymentStatus);

            if (response.data.data.paymentStatus === 'SUCCESS') {
                // 결제 성공 시 3초 후 마이페이지의 구매한 티켓 탭으로 이동
                setTimeout(() => {
                    navigate('/mypage', { state: { activeTab: 'purchased-tickets' } });
                }, 3000);
            } else if (response.data.data.paymentStatus === 'FAILED') {
                setError('결제에 실패했습니다. 다시 시도해 주세요.');
            }
        } catch (err) {
            console.error('Error checking payment status:', err);
            if (err.response && err.response.status === 404) {
                setPaymentStatus('TIMEOUT');
                setError('결제 시간이 초과되었습니다. 다시 시도해 주세요.');
            } else {
                setError('결제 상태 확인 중 오류가 발생했습니다.');
            }
        }
    }, [paymentId, navigate, festivalId, ticketId]);

    useEffect(() => {
        startPayment();
    }, [startPayment]);

    useEffect(() => {
        if (paymentStatus === 'PENDING') {
            const intervalId = setInterval(() => {
                setElapsedTime(prevTime => {
                    if (prevTime >= 30) {
                        clearInterval(intervalId);
                        return 30;
                    }
                    return prevTime + 3;
                });
                checkPaymentStatus();
            }, 3000);

            return () => clearInterval(intervalId);
        }
    }, [paymentStatus, checkPaymentStatus]);

    useEffect(() => {
        if (elapsedTime >= 30 && paymentStatus === 'PENDING') {
            setError('결제 시간이 초과되었습니다. 다시 시도해 주세요.');
            setPaymentStatus('TIMEOUT');
        }
    }, [elapsedTime, paymentStatus]);

    const handleRetry = () => {
        setError(null);
        setPaymentStatus(null);
        setPaymentId(null);
        setElapsedTime(0);
        setIsLoading(true);
        startPayment();
    };

    return (
        <div className="max-w-2xl mx-auto p-4">
            <h1 className="text-2xl font-bold text-teal-600 mb-4">결제 처리</h1>
            <div className="bg-white shadow-md rounded-lg p-6 mb-6">
                <h2 className="text-xl font-semibold mb-4">결제 상태: {paymentStatus || '처리 중'}</h2>
                {isLoading ? (
                    <div className="flex items-center justify-center">
                        <Loader2 className="w-8 h-8 animate-spin text-teal-500" />
                        <span className="ml-2">결제를 시작하고 있습니다...</span>
                    </div>
                ) : error ? (
                    <div className="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4" role="alert">
                        <div className="flex items-center">
                            <AlertCircle className="w-6 h-6 mr-2" />
                            <p>{error}</p>
                        </div>
                        <Button onClick={handleRetry} className="mt-4 bg-red-500 hover:bg-red-600 text-white">
                            다시 시도
                        </Button>
                    </div>
                ) : paymentStatus === 'PENDING' && (
                    <>
                        <p className="mb-2">결제를 처리하고 있습니다. 잠시만 기다려주세요.</p>
                        <div className="w-full bg-gray-200 rounded-full h-2.5 mb-4">
                            <div
                                className="bg-teal-600 h-2.5 rounded-full transition-all duration-300 ease-in-out"
                                style={{ width: `${(elapsedTime / 30) * 100}%` }}
                            ></div>
                        </div>
                        <p className="text-sm text-gray-500">남은 시간: {Math.max(0, 30 - elapsedTime)}초</p>
                    </>
                )}
                {paymentStatus === 'SUCCESS' && (
                    <div className="bg-green-100 border-l-4 border-green-500 text-green-700 p-4" role="alert">
                        <div className="flex items-center">
                            <CheckCircle className="w-6 h-6 mr-2" />
                            <p className="font-bold">결제 성공</p>
                        </div>
                        <p>결제가 성공적으로 완료되었습니다. 잠시 후 구매한 티켓 목록으로 이동합니다.</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default PaymentProcessPage;