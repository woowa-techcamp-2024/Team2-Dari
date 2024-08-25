import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import apiClient from '../../utils/apiClient';

const FestivalManagement = () => {
  const { festivalId } = useParams();
  const [festival, setFestival] = useState(null);
  const [activeTab, setActiveTab] = useState('info');

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

  return (
    <div className="flex h-screen bg-gray-100">
      {/* 사이드바 */}
      <div className="w-64 bg-white shadow-md">
        <div className="p-4">
          <h2 className="text-xl font-bold">축제 관리</h2>
        </div>
        <nav className="mt-4">
          <button
            className={`w-full text-left p-4 ${activeTab === 'info' ? 'bg-teal-100 text-teal-800' : 'hover:bg-gray-100'}`}
            onClick={() => setActiveTab('info')}
          >
            축제 상세 정보
          </button>
          <button
            className={`w-full text-left p-4 ${activeTab === 'tickets' ? 'bg-teal-100 text-teal-800' : 'hover:bg-gray-100'}`}
            onClick={() => setActiveTab('tickets')}
          >
            티켓 관리
          </button>
          <button
            className={`w-full text-left p-4 ${activeTab === 'purchasers' ? 'bg-teal-100 text-teal-800' : 'hover:bg-gray-100'}`}
            onClick={() => setActiveTab('purchasers')}
          >
            구매자 목록
          </button>
        </nav>
      </div>

      {/* 메인 컨텐츠 */}
      <div className="flex-1 p-8 overflow-auto">
        {activeTab === 'info' && <FestivalInfo festival={festival} />}
        {activeTab === 'tickets' && <TicketManagement festivalId={festivalId} />}
        {activeTab === 'purchasers' && <PurchasersList festivalId={festivalId} />}
      </div>
    </div>
  );
};

const FestivalInfo = ({ festival }) => (
  <div>
    <h2 className="text-2xl font-bold mb-4">{festival.title}</h2>
    <p>{festival.description}</p>
    {/* 추가 축제 정보 표시 */}
  </div>
);

const TicketManagement = ({ festivalId }) => {
  const [tickets, setTickets] = useState([]);

  useEffect(() => {
    const fetchTickets = async () => {
      try {
        const response = await apiClient.get(`/festivals/${festivalId}/tickets`);
        setTickets(response.data.data.tickets);
      } catch (error) {
        console.error('티켓 정보를 불러오는데 실패했습니다:', error);
      }
    };
    fetchTickets();
  }, [festivalId]);

  return (
    <div>
      <h2 className="text-2xl font-bold mb-4">티켓 관리</h2>
      <button className="bg-teal-500 text-white px-4 py-2 rounded mb-4">새 티켓 생성</button>
      <ul>
        {tickets.map(ticket => (
          <li key={ticket.id} className="bg-white shadow p-4 mb-2 rounded">
            {ticket.name} - ₩{ticket.price}
          </li>
        ))}
      </ul>
    </div>
  );
};

const PurchasersList = ({ festivalId }) => {
  const [purchasers, setPurchasers] = useState([]);

  useEffect(() => {
    const fetchPurchasers = async () => {
      try {
        // API 엔드포인트는 실제 구현에 맞게 조정해야 합니다
        const response = await apiClient.get(`/festivals/${festivalId}/purchasers`);
        setPurchasers(response.data.data);
      } catch (error) {
        console.error('구매자 목록을 불러오는데 실패했습니다:', error);
      }
    };
    fetchPurchasers();
  }, [festivalId]);

  const handleCheckin = async (purchaserId) => {
    try {
      await apiClient.post(`/festivals/${festivalId}/checkin`, { purchaserId });
      // 체크인 성공 후 목록 갱신
      setPurchasers(purchasers.map(p => 
        p.id === purchaserId ? { ...p, checkedIn: true } : p
      ));
    } catch (error) {
      console.error('체크인에 실패했습니다:', error);
    }
  };

  return (
    <div>
      <h2 className="text-2xl font-bold mb-4">구매자 목록</h2>
      <ul>
        {purchasers.map(purchaser => (
          <li key={purchaser.id} className="bg-white shadow p-4 mb-2 rounded flex justify-between items-center">
            <span>{purchaser.name} - {purchaser.email}</span>
            <button 
              onClick={() => handleCheckin(purchaser.id)}
              className={`px-4 py-2 rounded ${purchaser.checkedIn ? 'bg-gray-300' : 'bg-teal-500 text-white'}`}
              disabled={purchaser.checkedIn}
            >
              {purchaser.checkedIn ? '체크인 완료' : '체크인'}
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default FestivalManagement;