import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useInView } from 'react-intersection-observer';
import { Transition } from '@headlessui/react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../components/contexts/AuthContext'
import apiClient from '../../utils/apiClient';
import { debounce } from 'lodash';

const DEFAULT_IMAGE = "https://image.dongascience.com/Photo/2023/10/2be5b7157e8b50ebd8fa34b4772a97c1.jpg";

const FestivalCard = React.memo(({ festival }) => {
  const formattedStartDate = useMemo(() => new Date(festival.startTime).toLocaleDateString('ko-KR', { month: 'long', day: 'numeric' }), [festival.startTime]);
  const formattedEndDate = useMemo(() => new Date(festival.endTime).toLocaleDateString('ko-KR', { month: 'long', day: 'numeric' }), [festival.endTime]);

  return (
    <div className="bg-white rounded-lg shadow-md overflow-hidden">
      <img
        src={festival.festivalImg || DEFAULT_IMAGE}
        alt={festival.title}
        className="w-full h-48 object-cover"
        onError={(e) => { e.target.onerror = null; e.target.src = DEFAULT_IMAGE }}
        loading="lazy"
      />
      <div className="p-4">
        <h3 className="text-lg font-semibold mb-2">{festival.title}</h3>
        <p className="text-sm text-gray-600 mb-2">{formattedStartDate} ~ {formattedEndDate}</p>
        <p className="text-sm mb-2 line-clamp-2">{festival.description}</p>
      </div>
      <div className="p-4 pt-0 flex justify-between items-center">
        <span className="text-sm text-gray-600">By: {festival.admin.name}</span>
      </div>
    </div>
  );
});

const useFestivals = () => {
  const [festivals, setFestivals] = useState([]);
  const [cursor, setCursor] = useState(null);
  const [hasMore, setHasMore] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [retryCount, setRetryCount] = useState(0);

  const fetchFestivals = useCallback(async (isInitialLoad = false) => {
    if (isLoading || (!isInitialLoad && !hasMore)) return;
    setIsLoading(true);

    try {
      const params = {
        pageSize: '4',
        ...(cursor && !isInitialLoad ? { time: cursor.time, id: cursor.id.toString() } : {})
      };

      const response = await apiClient.get(`/festivals`, {params});
      const { data } = response.data;

      setFestivals((prev) => {
        const newFestivals = isInitialLoad ? data.content : [...prev, ...data.content];
        return newFestivals.filter((festival, index, self) =>
          index === self.findIndex((t) => t.festivalId === festival.festivalId)
        );
      });

      if (data.content.length > 0) {
        const lastFestival = data.content[data.content.length - 1];
        setCursor({ time: lastFestival.startTime, id: lastFestival.festivalId });
      }

      setHasMore(data.hasNext);
      setError(null);
      setRetryCount(0);
    } catch (error) {
      console.error('Failed to fetch festivals:', error);
      setError('축제 목록을 불러오는 데 실패했습니다. 다시 시도해주세요.');
      setRetryCount(prev => prev + 1);
    } finally {
      setIsLoading(false);
    }
  }, [cursor, isLoading, hasMore]);

  useEffect(() => {
    fetchFestivals(true);
  }, []);

  const loadMore = useCallback(() => {
    if (!isLoading && hasMore && retryCount < 3) {
      fetchFestivals(false);
    }
  }, [isLoading, hasMore, fetchFestivals, retryCount]);

  return { festivals, hasMore, isLoading, error, loadMore, retryCount };
};

export default function FestivalList() {
  const { festivals, hasMore, isLoading, error, loadMore, retryCount } = useFestivals();
  const [ref, inView] = useInView({
    threshold: 0,
    rootMargin: '100px',
    triggerOnce: false,
  });
  const { isAuthenticated } = useAuth();

  const debouncedLoadMore = useMemo(
    () => debounce(loadMore, 500),
    [loadMore]
  );

  useEffect(() => {
    if (inView && hasMore && !isLoading && retryCount < 3) {
      debouncedLoadMore();
    }
  }, [inView, hasMore, isLoading, debouncedLoadMore, retryCount]);

  const renderFestivals = useMemo(() => (
    festivals.map((festival) => (
      <Link key={`festival-${festival.festivalId}`} to={`/festivals/${festival.festivalId}`}>
        <FestivalCard festival={festival} />
      </Link>
    ))
  ), [festivals]);

  return (
    <div className="flex flex-col h-full">
      <div className="flex-grow overflow-y-auto px-4 py-8">
        <div className="max-w-6xl mx-auto space-y-8">
          <h1 className="text-3xl font-bold text-teal-500">Events Board</h1>
          <p className="text-sm text-gray-600">이벤트 보드에서 축제의 민족의 이벤트를 한눈에 볼 수 있습니다.</p>

          {error && retryCount >= 3 && (
            <div className="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4" role="alert">
              <p className="font-bold">서버 오류</p>
              <p>현재 서버에 문제가 있어 축제 목록을 불러올 수 없습니다. 잠시 후 다시 시도해 주세요.</p>
            </div>
          )}

          {error && retryCount < 3 && <p className="text-red-500 text-center mb-4">{error}</p>}

          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {renderFestivals}
          </div>

          <Transition
            show={isLoading}
            enter="transition-opacity duration-300"
            enterFrom="opacity-0"
            enterTo="opacity-100"
            leave="transition-opacity duration-300"
            leaveFrom="opacity-100"
            leaveTo="opacity-0"
          >
            <div className="space-y-6 mt-6">
              {[...Array(3)].map((_, index) => (
                <div key={`skeleton-${index}`} className="bg-white p-4 rounded-md shadow-md animate-pulse">
                  <div className="h-48 bg-gray-300 mb-4 rounded"></div>
                  <div className="h-6 bg-gray-300 w-3/4 mb-2 rounded"></div>
                  <div className="h-4 bg-gray-300 w-1/2 mb-2 rounded"></div>
                  <div className="h-4 bg-gray-300 w-1/4 rounded"></div>
                </div>
              ))}
            </div>
          </Transition>

          {!isLoading && !hasMore && festivals.length > 0 && (
            <p className="text-center py-4 text-gray-600">모든 축제를 불러왔습니다.</p>
          )}

          {!isLoading && festivals.length === 0 && !error && (
            <p className="text-center py-4 text-gray-600">표시할 축제가 없습니다.</p>
          )}

          <div ref={ref} style={{ height: '10px' }} />
        </div>
      </div>
    </div>
  );
}