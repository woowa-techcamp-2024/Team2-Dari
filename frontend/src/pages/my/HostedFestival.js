import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import apiClient from '../../utils/apiClient';
import { useInView } from 'react-intersection-observer';

export default function HostedFestivals() {
  const [festivals, setFestivals] = useState([]);
  const [cursor, setCursor] = useState(null);
  const [hasMore, setHasMore] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const { ref, inView } = useInView();

  const loadMoreFestivals = useCallback(async () => {
    if (isLoading || !hasMore) return;
    
    setIsLoading(true);
    try {
      const params = cursor
        ? { time: cursor.startTime, id: cursor.id, pageSize: 10 }
        : { pageSize: 10 };

      const response = await apiClient.get('/member/festivals', { params });
      const { content, cursor: newCursor, hasNext } = response.data.data;
      setFestivals(prev => [...prev, ...content]);
      setCursor(newCursor);
      setHasMore(hasNext);
    } catch (error) {
      console.error('Failed to fetch hosted festivals:', error);
    } finally {
      setIsLoading(false);
    }
  }, [cursor, isLoading, hasMore]);

  useEffect(() => {
    if (festivals.length === 0) {
      loadMoreFestivals();
    }
  }, []);

  useEffect(() => {
    if (inView && hasMore && !isLoading) {
      loadMoreFestivals();
    }
  }, [inView, hasMore, isLoading, loadMoreFestivals]);

  return (
    <div>
      <h2 className="text-2xl font-semibold mb-4">내가 주최한 축제</h2>
      <ul className="space-y-4">
        {festivals.map(festival => (
          <li key={festival.festivalId} className="bg-white shadow rounded-lg p-4 hover:shadow-lg transition-shadow duration-300">
            <Link to={`/admin/${festival.festivalId}`} className="block">
              <h3 className="text-lg font-semibold text-teal-600 hover:text-teal-800">{festival.title}</h3>
              <p className="text-sm text-gray-500">{new Date(festival.startTime).toLocaleDateString()}</p>
              <img src={festival.festivalImg} alt={festival.title} className="mt-2 w-full h-40 object-cover rounded" />
              <div className="mt-2 text-right">
                <span className="inline-block bg-teal-100 text-teal-800 px-2 py-1 rounded text-sm">관리하기</span>
              </div>
            </Link>
          </li>
        ))}
      </ul>
      {hasMore && <div ref={ref} className="h-10" />}
      {isLoading && <p className="text-center">로딩 중...</p>}
    </div>
  );
}