import React, { useState, useEffect, useCallback } from 'react';
import apiClient from '../../utils/apiClient';
import { Button } from '../../components/ui/button';
import { Loader2 } from 'lucide-react';

const PurchasersList = ({ festivalId }) => {
  const [participants, setParticipants] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [itemsPerPage, setItemsPerPage] = useState(10);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchParticipants = useCallback(async (page, pageSize) => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await apiClient.get(`/festivals/${festivalId}/participants`, {
        params: { page, size: pageSize }
      });
      const { participants, currentPage, totalPages, itemsPerPage } = response.data.data;
      setParticipants(participants);
      setCurrentPage(currentPage);
      setTotalPages(totalPages);
      setItemsPerPage(itemsPerPage);
    } catch (error) {
      console.error('참가자 목록을 불러오는데 실패했습니다:', error);
      setError('참가자 목록을 불러오는데 실패했습니다. 다시 시도해주세요.');
    } finally {
      setIsLoading(false);
    }
  }, [festivalId]);

  useEffect(() => {
    fetchParticipants(1, itemsPerPage);
  }, [fetchParticipants, itemsPerPage]);

  const handleCheckin = async (participant) => {
    try {
      await apiClient.patch(`/festivals/${festivalId}/tickets/${participant.ticketId}/checkins/${participant.checkinId}`);
      setParticipants(participants.map(p => 
        p.checkinId === participant.checkinId ? { ...p, isCheckin: true } : p
      ));
      setError(`${participant.participantName}님이 체크인되었습니다.`);
      setTimeout(() => setError(null), 3000);
    } catch (error) {
      console.error('체크인에 실패했습니다:', error);
      setError(`체크인에 실패했습니다: ${error.response?.data?.message || '알 수 없는 오류가 발생했습니다.'}`);
    }
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 1 && newPage <= totalPages) {
      fetchParticipants(newPage, itemsPerPage);
    }
  };

  const handleItemsPerPageChange = (newItemsPerPage) => {
    setItemsPerPage(newItemsPerPage);
    fetchParticipants(1, newItemsPerPage);
  };

  const getPageNumbers = () => {
    const pageNumbers = [];
    const totalPageNumbers = 7; // 표시할 최대 페이지 번호 수
    const sidePageNumbers = 2; // 현재 페이지 양옆에 표시할 페이지 수

    if (totalPages <= totalPageNumbers) {
      for (let i = 1; i <= totalPages; i++) {
        pageNumbers.push(i);
      }
    } else {
      if (currentPage <= sidePageNumbers + 1) {
        for (let i = 1; i <= totalPageNumbers - 2; i++) {
          pageNumbers.push(i);
        }
        pageNumbers.push('...', totalPages);
      } else if (currentPage >= totalPages - sidePageNumbers) {
        pageNumbers.push(1, '...');
        for (let i = totalPages - totalPageNumbers + 3; i <= totalPages; i++) {
          pageNumbers.push(i);
        }
      } else {
        pageNumbers.push(1, '...');
        for (let i = currentPage - sidePageNumbers; i <= currentPage + sidePageNumbers; i++) {
          pageNumbers.push(i);
        }
        pageNumbers.push('...', totalPages);
      }
    }

    return pageNumbers;
  };

  return (
    <div className="space-y-4">
      <h2 className="text-2xl font-bold mb-4">참가자 목록</h2>
      {error && (
        <div className={`p-2 rounded ${error.includes('체크인되었습니다') ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
          {error}
        </div>
      )}
      <div className="mb-4">
        <label htmlFor="itemsPerPage" className="mr-2">페이지당 항목 수:</label>
        <select
          id="itemsPerPage"
          value={itemsPerPage}
          onChange={(e) => handleItemsPerPageChange(Number(e.target.value))}
          className="border rounded p-1"
        >
          <option value={10}>10</option>
          <option value={20}>20</option>
          <option value={50}>50</option>
        </select>
      </div>
      {isLoading ? (
        <div className="flex justify-center items-center">
          <Loader2 className="h-8 w-8 animate-spin" />
          <span className="ml-2">로딩 중...</span>
        </div>
      ) : (
        <>
          <div className="overflow-x-auto">
            <table className="min-w-full bg-white">
              <thead className="bg-gray-100">
                <tr>
                  <th className="px-4 py-2">이름</th>
                  <th className="px-4 py-2">이메일</th>
                  <th className="px-4 py-2">티켓</th>
                  <th className="px-4 py-2">구매 시간</th>
                  <th className="px-4 py-2">체크인 상태</th>
                  <th className="px-4 py-2">액션</th>
                </tr>
              </thead>
              <tbody>
                {participants.map((participant) => (
                  <tr key={participant.participantId} className="border-b">
                    <td className="px-4 py-2">{participant.participantName}</td>
                    <td className="px-4 py-2">{participant.participantEmail}</td>
                    <td className="px-4 py-2">{participant.ticketName}</td>
                    <td className="px-4 py-2">{new Date(participant.purchaseTime).toLocaleString()}</td>
                    <td className="px-4 py-2">{participant.isCheckin ? '완료' : '미완료'}</td>
                    <td className="px-4 py-2">
                      <Button
                        onClick={() => handleCheckin(participant)}
                        disabled={participant.isCheckin}
                        className={participant.isCheckin ? 'bg-gray-300' : 'bg-teal-500 hover:bg-teal-600'}
                      >
                        {participant.isCheckin ? '체크인 완료' : '체크인'}
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="flex justify-center items-center space-x-2 mt-4">
            <Button
              onClick={() => handlePageChange(currentPage - 1)}
              disabled={currentPage === 1}
            >
              이전
            </Button>
            {getPageNumbers().map((number, index) => (
              <Button
                key={index}
                onClick={() => typeof number === 'number' && handlePageChange(number)}
                className={`${currentPage === number ? 'bg-teal-500 text-white' : 'bg-white text-teal-500'} border border-teal-500`}
                disabled={typeof number !== 'number'}
              >
                {number}
              </Button>
            ))}
            <Button
              onClick={() => handlePageChange(currentPage + 1)}
              disabled={currentPage === totalPages}
            >
              다음
            </Button>
          </div>
        </>
      )}
    </div>
  );
};

export default PurchasersList;