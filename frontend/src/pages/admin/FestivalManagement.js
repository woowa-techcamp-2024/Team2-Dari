import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import apiClient from '../../utils/apiClient';
import FestivalInfo from './FestivalInfo';
import TicketManagement from './TicketManagement';
import PurchasersList from './PurchasersList';
import { Menu, X } from 'lucide-react';
import jsQR from 'jsqr';

const FestivalManagement = () => {
  const { festivalId } = useParams();
  const [festival, setFestival] = useState(null);
  const [activeTab, setActiveTab] = useState('info');
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const [modalMessage, setModalMessage] = useState(null);
  const streamRef = useRef(null);

  useEffect(() => {
    const fetchFestivalData = async () => {
      try {
        const response = await apiClient.get(`/festivals/${festivalId}`);
        setFestival(response.data.data);
      } catch (error) {
        console.error('축제 정보를 불러오는데 실패했습니다:', error);
      }
    };
    fetchFestivalData();
  }, [festivalId]);

  const stopScanner = useCallback(() => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(track => track.stop());
      streamRef.current = null;
    }
  }, []);

  useEffect(() => {
    if (activeTab === 'qr-scanner') {
      startScanner();
    } else {
      stopScanner();
    }

    return () => {
      stopScanner();
    };
  }, [activeTab, stopScanner]);

  const startScanner = async () => {
    try {
      stopScanner();  // 기존 스트림이 있다면 중지
      const stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } });
      streamRef.current = stream;
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        videoRef.current.play();
      }
      scanQRCode();
    } catch (error) {
      console.error('카메라 액세스 오류:', error);
      setModalMessage('카메라 접근에 실패했습니다. 카메라 권한을 확인해주세요.');
    }
  };

  const scanQRCode = () => {
    const video = videoRef.current;
    const canvas = canvasRef.current;
    if (!video || !canvas) return;

    const context = canvas.getContext('2d');

    const scanInterval = setInterval(() => {
      if (video.readyState === video.HAVE_ENOUGH_DATA) {
        canvas.height = video.videoHeight;
        canvas.width = video.videoWidth;
        context.drawImage(video, 0, 0, canvas.width, canvas.height);
        const imageData = context.getImageData(0, 0, canvas.width, canvas.height);
        const code = jsQR(imageData.data, imageData.width, imageData.height);
        
        if (code) {
          handleScan(code.data);
          clearInterval(scanInterval);
        }
      }
    }, 100);

    return () => {
      clearInterval(scanInterval);
    };
  };

  const handleScan = async (data) => {
    try {
      const scannedData = JSON.parse(data);
      if (scannedData.festivalId !== parseInt(festivalId)) {
        setModalMessage('유효하지 않은 QR 코드입니다. 다른 축제의 티켓입니다.');
        return;
      }
      const response = await apiClient.patch(`/festivals/${festivalId}/tickets/${scannedData.ticketId}/checkins/${scannedData.checkinId}`);
      setModalMessage('체크인이 완료되었습니다.');
      setActiveTab('purchasers');  // 체크인 후 구매자 목록으로 이동
    } catch (error) {
      if (error.response && error.response.data.errorCode === 'CI-0002') {
        setModalMessage('이미 체크인된 티켓입니다.');
      } else {
        console.error('체크인 처리 중 오류가 발생했습니다:', error);
        setModalMessage('체크인 처리 중 오류가 발생했습니다.');
      }
    }
  };

  if (!festival) return <div>Loading...</div>;

  const navItems = [
    { id: 'info', label: '축제 상세 정보' },
    { id: 'tickets', label: '티켓 관리' },
    { id: 'purchasers', label: '구매자 목록' },
    { id: 'qr-scanner', label: 'QR 스캔' },
  ];

  const renderNavItems = () => (
    <>
      {navItems.map((item) => (
        <button
          key={item.id}
          className={`w-full text-left p-4 ${
            activeTab === item.id ? 'bg-teal-100 text-teal-800' : 'hover:bg-gray-100'
          }`}
          onClick={() => {
            setActiveTab(item.id);
            setIsMobileMenuOpen(false);
          }}
        >
          {item.label}
        </button>
      ))}
    </>
  );

  const Modal = ({ message, onClose }) => (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white p-6 rounded-lg shadow-xl">
        <p className="text-lg mb-4">{message}</p>
        <button
          onClick={onClose}
          className="bg-teal-500 text-white px-4 py-2 rounded hover:bg-teal-600"
        >
          확인
        </button>
      </div>
    </div>
  );

  return (
    <div className="flex flex-col md:flex-row min-h-screen bg-gray-100">
      {/* 모바일 네비게이션 바 */}
      <div className="md:hidden bg-white shadow-md p-4">
        <div className="flex justify-between items-center">
          <h2 className="text-xl font-bold">축제 관리</h2>
          <button onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}>
            {isMobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>
        {isMobileMenuOpen && <nav className="mt-4">{renderNavItems()}</nav>}
      </div>

      {/* 데스크톱 사이드바 */}
      <div className="hidden md:block w-64 bg-white shadow-md">
        <div className="p-4">
          <h2 className="text-xl font-bold">축제 관리</h2>
        </div>
        <nav className="mt-4">{renderNavItems()}</nav>
      </div>

      {/* 메인 컨텐츠 */}
      <div className="flex-1 p-4 md:p-8 overflow-auto">
        {activeTab === 'info' && <FestivalInfo festival={festival} />}
        {activeTab === 'tickets' && <TicketManagement festivalId={festivalId} />}
        {activeTab === 'purchasers' && <PurchasersList festivalId={festivalId} />}
        {activeTab === 'qr-scanner' && (
          <div className="mt-4">
            <h3 className="text-xl font-bold mb-4">QR 코드 스캔</h3>
            <div className="w-full max-w-2xl mx-auto">
              <video ref={videoRef} className="w-full" />
              <canvas ref={canvasRef} style={{ display: 'none' }} />
            </div>
          </div>
        )}
      </div>

      {modalMessage && (
        <Modal
          message={modalMessage}
          onClose={() => {
            setModalMessage(null);
            if (activeTab === 'qr-scanner') {
              startScanner();  // 모달을 닫은 후 스캐너 재시작
            }
          }}
        />
      )}
    </div>
  );
};

export default FestivalManagement;