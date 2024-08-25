import React, { useState, useEffect, useCallback } from 'react';
import apiClient from '../../utils/apiClient';
import { useInView } from 'react-intersection-observer';
import { Dialog, Transition } from '@headlessui/react';

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
  }, []);

  useEffect(() => {
    if (inView && hasMore && !isLoading) {
      loadMoreTickets();
    }
  }, [inView, hasMore, isLoading, loadMoreTickets]);

  const openModal = async (ticketId) => {
    try {
      const response = await apiClient.get(`/member/tickets/${ticketId}`);
      setSelectedTicket(response.data.data);
      setIsOpen(true);
    } catch (error) {
      console.error('Failed to fetch ticket details:', error);
    }
  };

  return (
    <div>
      <h2 className="text-2xl font-semibold mb-4">내가 구매한 티켓</h2>
      <ul className="space-y-4">
        {tickets.map(ticket => (
          <li key={ticket.purchaseId} className="bg-white shadow rounded-lg p-4">
            <h3 className="text-lg font-semibold">{ticket.title}</h3>
            <p className="text-sm text-gray-500">
              {new Date(ticket.startTime).toLocaleDateString()} ~ {new Date(ticket.endTime).toLocaleDateString()}
            </p>
            <button 
              onClick={() => openModal(ticket.purchaseId)} 
              className="mt-2 px-4 py-2 bg-teal-500 text-white rounded hover:bg-teal-600 transition-colors"
            >
              상세 정보
            </button>
          </li>
        ))}
      </ul>
      {hasMore && <div ref={ref} className="h-10" />}
      {isLoading && <p className="text-center">로딩 중...</p>}

      <Transition appear show={isOpen} as={React.Fragment}>
        <Dialog as="div" className="relative z-10" onClose={() => setIsOpen(false)}>
          <Transition.Child
            as={React.Fragment}
            enter="ease-out duration-300"
            enterFrom="opacity-0"
            enterTo="opacity-100"
            leave="ease-in duration-200"
            leaveFrom="opacity-100"
            leaveTo="opacity-0"
          >
            <div className="fixed inset-0 bg-black bg-opacity-25" />
          </Transition.Child>

          <div className="fixed inset-0 overflow-y-auto">
            <div className="flex min-h-full items-center justify-center p-4 text-center">
              <Transition.Child
                as={React.Fragment}
                enter="ease-out duration-300"
                enterFrom="opacity-0 scale-95"
                enterTo="opacity-100 scale-100"
                leave="ease-in duration-200"
                leaveFrom="opacity-100 scale-100"
                leaveTo="opacity-0 scale-95"
              >
                <Dialog.Panel className="w-full max-w-md transform overflow-hidden rounded-2xl bg-white p-6 text-left align-middle shadow-xl transition-all">
                  <Dialog.Title
                    as="h3"
                    className="text-lg font-medium leading-6 text-gray-900"
                  >
                    티켓 상세 정보
                  </Dialog.Title>
                  {selectedTicket && (
                    <div className="mt-2">
                      <p className="text-sm text-gray-500">축제: {selectedTicket.festival.title}</p>
                      <p className="text-sm text-gray-500">티켓: {selectedTicket.ticket.name}</p>
                      <p className="text-sm text-gray-500">가격: {selectedTicket.ticket.price}원</p>
                      <p className="text-sm text-gray-500">구매 시간: {new Date(selectedTicket.purchaseTime).toLocaleString()}</p>
                    </div>
                  )}

                  <div className="mt-4">
                    <button
                      type="button"
                      className="inline-flex justify-center rounded-md border border-transparent bg-teal-100 px-4 py-2 text-sm font-medium text-teal-900 hover:bg-teal-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-teal-500 focus-visible:ring-offset-2"
                      onClick={() => setIsOpen(false)}
                    >
                      닫기
                    </button>
                  </div>
                </Dialog.Panel>
              </Transition.Child>
            </div>
          </div>
        </Dialog>
      </Transition>
    </div>
  );
}