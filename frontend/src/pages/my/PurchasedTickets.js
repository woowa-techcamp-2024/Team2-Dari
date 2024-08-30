import React, { useState, useEffect, useCallback } from 'react';
import apiClient from '../../utils/apiClient';
import { useInView } from 'react-intersection-observer';
import { XIcon } from '@heroicons/react/outline';
import { QRCodeSVG } from 'qrcode.react';

export default function PurchasedTickets() {
  const [tickets, setTickets] = useState([]);
  const [cursor, setCursor] = useState(null);
  const [hasMore, setHasMore] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const { ref, inView } = useInView();
  const [selectedTicket, setSelectedTicket] = useState(null);
  const [isOpen, setIsOpen] = useState(false);

  const loadMoreTickets = useCallback(async () => {
    if (isLoading || !hasMore) return;

    setIsLoading(true);
    try {
      const params = cursor
        ? { time: cursor.startTime, id: cursor.id, pageSize: 10 }
        : { pageSize: 10 };

      const response = await apiClient.get('/member/tickets', { params });
      const { content, cursor: newCursor, hasNext } = response.data.data;
      setTickets(prev => [...prev, ...content]);
      setCursor(newCursor);
      setHasMore(hasNext);
    } catch (error) {
      console.error('Failed to fetch purchased tickets:', error);
    } finally {
      setIsLoading(false);
    }
  }, [cursor, isLoading, hasMore]);

  useEffect(() => {
    if (tickets.length === 0) {
      loadMoreTickets();
    }
  }, [loadMoreTickets]);

  useEffect(() => {
    if (inView && hasMore && !isLoading) {
      loadMoreTickets();
    }
  }, [inView, hasMore, isLoading, loadMoreTickets]);

  const openTicketDetail = async (ticketId) => {
    try {
      const response = await apiClient.get(`/member/tickets/${ticketId}`);
      setSelectedTicket(response.data.data);
      setIsOpen(true);
    } catch (error) {
      console.error('Failed to fetch ticket details:', error);
    }
  };

  const TicketDetail = ({ ticket, onClose }) => {
    const qrCodeData = JSON.stringify({
      festivalId: ticket.festival.festivalId,
      ticketId: ticket.ticket.id,
      checkinId: ticket.checkinId
    });

    return (
      <div className="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center p-4 z-50">
        <div className="bg-white rounded-lg shadow-xl max-w-md w-full overflow-hidden">
          <div className="relative bg-teal-500 text-white p-6">
            <h3 className="text-2xl font-bold">{ticket.festival.title}</h3>
            <button onClick={onClose} className="absolute top-4 right-4 text-white">
              <XIcon className="h-6 w-6" />
            </button>
          </div>
          <div className="p-6">
            <div className="mb-4 text-center">
              <p className="text-3xl font-bold text-teal-600">{ticket.ticket.name}</p>
              <p className="text-gray-600">
                {new Date(ticket.festival.startTime).toLocaleDateString()} ~ {new Date(ticket.festival.endTime).toLocaleDateString()}
              </p>
            </div>
            <div className="border-t border-b border-gray-200 py-4 mb-4">
              <p className="text-gray-700"><span className="font-semibold">구매 시간:</span> {new Date(ticket.purchaseTime).toLocaleString()}</p>
              <p className="text-gray-700"><span className="font-semibold">가격:</span> {ticket.ticket.price.toLocaleString()}원</p>
              <p className="text-gray-700"><span className="font-semibold">체크인 상태:</span> {ticket.isCheckedIn ? '완료' : '미완료'}</p>
            </div>
            {!ticket.isCheckin && (
              <div className="bg-gray-100 p-4 rounded-lg mb-4 flex flex-col items-center">
                <QRCodeSVG value={qrCodeData} size={200} />
                <p className="text-center text-gray-600 mt-2">입장 시 이 QR 코드를 제시해 주세요</p>
              </div>
            )}
            <p className="text-xs text-gray-500 text-center">본 티켓은 양도 및 환불이 불가능합니다.</p>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="container mx-auto px-4">
      <h2 className="text-2xl font-semibold mb-4">내가 구매한 티켓</h2>
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {tickets.map(ticket => (
          <div key={ticket.purchaseId} className="bg-white shadow rounded-lg overflow-hidden">
            <div className={`text-white p-4 ${ticket.isCheckin ? 'bg-gray-500' : 'bg-teal-500'}`}>
              <h3 className="text-lg font-semibold truncate">{ticket.title}</h3>
            </div>
            <div className="p-4">
              <p className="text-sm text-gray-600 mb-2">
                {new Date(ticket.startTime).toLocaleDateString()} ~ {new Date(ticket.endTime).toLocaleDateString()}
              </p>
              <p className="text-sm font-semibold mb-2">
                {ticket.isCheckin ? '체크인 완료' : '미체크인'}
              </p>
              <button 
                onClick={() => openTicketDetail(ticket.ticketId)}
                className={`w-full px-4 py-2 text-white rounded transition-colors ${
                  ticket.isCheckin ? 'bg-gray-500 hover:bg-gray-600' : 'bg-teal-500 hover:bg-teal-600'
                }`}
              >
                티켓 보기
              </button>
            </div>
          </div>
        ))}
      </div>
      {hasMore && <div ref={ref} className="h-10" />}
      {isLoading && <p className="text-center mt-4">로딩 중...</p>}
      {isOpen && selectedTicket && (
        <TicketDetail ticket={selectedTicket} onClose={() => setIsOpen(false)} />
      )}
    </div>
  );
}