import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import apiClient from '../../utils/apiClient';
import FestivalInfo from './FestivalInfo';
import TicketManagement from './TicketManagement';
import PurchasersList from './PurchasersList';
import { Menu, X } from 'lucide-react';

const FestivalManagement = () => {
  const { festivalId } = useParams();
  const [festival, setFestival] = useState(null);
  const [activeTab, setActiveTab] = useState('info');
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

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

  if (!festival) return <div>Loading...</div>;

  const navItems = [
    { id: 'info', label: '축제 상세 정보' },
    { id: 'tickets', label: '티켓 관리' },
    { id: 'purchasers', label: '구매자 목록' },
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
      </div>
    </div>
  );
};

export default FestivalManagement;