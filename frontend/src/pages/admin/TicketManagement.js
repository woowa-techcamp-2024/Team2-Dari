import React, { useState, useEffect, useCallback, memo } from 'react';
import apiClient from '../../utils/apiClient';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Textarea } from '../../components/ui/textarea';

const TicketCreationModal = memo(({ isOpen, onClose, onSubmit, festivalId }) => {
  const initialTicketState = {
    name: '',
    detail: '',
    price: '',
    quantity: '',
    startSaleTime: '',
    endSaleTime: '',
    refundEndTime: ''
  };

  const [newTicket, setNewTicket] = useState(initialTicketState);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNewTicket(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await onSubmit(newTicket);
      setNewTicket(initialTicketState); // 폼 초기화
      onClose();
    } catch (error) {
      console.error('티켓 생성에 실패했습니다:', error);
    }
  };

  useEffect(() => {
    if (!isOpen) {
      setNewTicket(initialTicketState); // 모달이 닫힐 때 폼 초기화
    }
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center">
      <div className="bg-white p-6 rounded-lg w-full max-w-md">
        <h3 className="text-xl font-bold mb-4">새 티켓 생성</h3>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="name" className="block text-sm font-medium text-gray-700">티켓 이름</label>
            <Input
              id="name"
              name="name"
              value={newTicket.name}
              onChange={handleInputChange}
              placeholder="VIP 티켓"
              className="mt-1"
              required
            />
          </div>
          <div>
            <label htmlFor="detail" className="block text-sm font-medium text-gray-700">티켓 설명</label>
            <Textarea
              id="detail"
              name="detail"
              value={newTicket.detail}
              onChange={handleInputChange}
              placeholder="VIP 좌석과 특별 서비스가 포함된 프리미엄 티켓입니다."
              className="mt-1"
            />
          </div>
          <div>
            <label htmlFor="price" className="block text-sm font-medium text-gray-700">가격 (원)</label>
            <Input
              id="price"
              type="number"
              name="price"
              value={newTicket.price}
              onChange={handleInputChange}
              placeholder="50000"
              className="mt-1"
              required
            />
          </div>
          <div>
            <label htmlFor="quantity" className="block text-sm font-medium text-gray-700">수량</label>
            <Input
              id="quantity"
              type="number"
              name="quantity"
              value={newTicket.quantity}
              onChange={handleInputChange}
              placeholder="100"
              className="mt-1"
              required
            />
          </div>
          <div>
            <label htmlFor="startSaleTime" className="block text-sm font-medium text-gray-700">판매 시작 시간</label>
            <Input
              id="startSaleTime"
              type="datetime-local"
              name="startSaleTime"
              value={newTicket.startSaleTime}
              onChange={handleInputChange}
              className="mt-1"
              required
            />
          </div>
          <div>
            <label htmlFor="endSaleTime" className="block text-sm font-medium text-gray-700">판매 종료 시간</label>
            <Input
              id="endSaleTime"
              type="datetime-local"
              name="endSaleTime"
              value={newTicket.endSaleTime}
              onChange={handleInputChange}
              className="mt-1"
              required
            />
          </div>
          <div>
            <label htmlFor="refundEndTime" className="block text-sm font-medium text-gray-700">환불 마감 시간</label>
            <Input
              id="refundEndTime"
              type="datetime-local"
              name="refundEndTime"
              value={newTicket.refundEndTime}
              onChange={handleInputChange}
              className="mt-1"
              required
            />
          </div>
          <div className="flex justify-end space-x-2 mt-6">
            <Button type="button" onClick={onClose} variant="outline">
              취소
            </Button>
            <Button type="submit">
              생성
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
});
const TicketManagement = ({ festivalId }) => {
  const [tickets, setTickets] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [error, setError] = useState('');

  const fetchTickets = useCallback(async () => {
    try {
      const response = await apiClient.get(`/festivals/${festivalId}/tickets`);
      setTickets(response.data.data.tickets);
    } catch (error) {
      console.error('티켓 정보를 불러오는데 실패했습니다:', error);
      setError('티켓 정보를 불러오는데 실패했습니다.');
    }
  }, [festivalId]);

  useEffect(() => {
    fetchTickets();
  }, [fetchTickets]);

  const handleCreateTicket = async (newTicket) => {
    try {
      await apiClient.post(`/festivals/${festivalId}/tickets`, newTicket);
      fetchTickets(); // 티켓 목록 갱신
      setError('');
    } catch (error) {
      console.error('티켓 생성에 실패했습니다:', error);
      setError('티켓 생성에 실패했습니다. 다시 시도해주세요.');
    }
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold">티켓 관리</h2>
        <Button onClick={() => setIsModalOpen(true)}>
          새 티켓 생성
        </Button>
      </div>
      {error && <p className="text-red-500 mb-4">{error}</p>}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {tickets.map(ticket => (
          <div key={ticket.id} className="bg-white shadow-md rounded-lg p-6">
            <h3 className="text-xl font-semibold mb-2">{ticket.name}</h3>
            <p className="text-gray-600 mb-4">{ticket.detail}</p>
            <div className="grid grid-cols-2 gap-2 text-sm">
              <div>
                <p className="font-semibold">가격:</p>
                <p>₩{ticket.price.toLocaleString()}</p>
              </div>
              <div>
                <p className="font-semibold">수량:</p>
                <p>{ticket.quantity}</p>
              </div>
              <div>
                <p className="font-semibold">남은 수량:</p>
                <p>{ticket.remainStock}</p>
              </div>
              <div>
                <p className="font-semibold">판매 시작:</p>
                <p>{new Date(ticket.startSaleTime).toLocaleString()}</p>
              </div>
              <div>
                <p className="font-semibold">판매 종료:</p>
                <p>{new Date(ticket.endSaleTime).toLocaleString()}</p>
              </div>
              <div>
                <p className="font-semibold">환불 마감:</p>
                <p>{new Date(ticket.refundEndTime).toLocaleString()}</p>
              </div>
            </div>
          </div>
        ))}
      </div>
      <TicketCreationModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleCreateTicket}
        festivalId={festivalId}
      />
    </div>
  );
};

export default TicketManagement;