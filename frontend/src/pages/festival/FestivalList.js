import React, { useState, useEffect, useCallback, memo } from 'react';
import axios from 'axios';
import InfiniteScroll from 'react-infinite-scroll-component';
import { Input } from "../../components/ui/input";
import { Card } from "../../components/ui/card";
import { Link } from 'react-router-dom';
import { Loader2 } from 'lucide-react';

const DEFAULT_IMAGE = "https://image.dongascience.com/Photo/2023/10/2be5b7157e8b50ebd8fa34b4772a97c1.jpg"

const FestivalCard = memo(({ festival }) => {
    const formattedStartDate = new Date(festival.startTime).toLocaleDateString();
    const formattedEndDate = new Date(festival.endTime).toLocaleDateString();

    return (
        <Link to={`/festivals/${festival.festivalId}`} className="flex flex-col h-full overflow-hidden transition-all duration-300 hover:shadow-lg">
            <Card>
                <div className="relative aspect-video overflow-hidden">
                    <img
                        src={festival.festivalImg || DEFAULT_IMAGE}
                        alt={festival.title}
                        className="w-full h-full object-cover transition-transform duration-300 hover:scale-105"
                        onError={(e) => { e.target.onerror = null; e.target.src = DEFAULT_IMAGE }}
                        loading="lazy"
                    />
                </div>
                <div className="p-4 flex-1 flex flex-col">
                    <h3 className="text-lg font-semibold mb-2 line-clamp-2">{festival.title}</h3>
                    <p className="text-sm text-muted-foreground mb-2">{formattedStartDate} ~ {formattedEndDate}</p>
                    <p className="text-sm mb-4 flex-grow line-clamp-3">{festival.description}</p>
                    <div className="flex justify-between items-center mt-auto">
                        <span className="text-sm text-muted-foreground">By: {festival.admin.name}</span>
                    </div>
                </div>
            </Card>
        </Link>
    );
});

const useFestivals = () => {
    const [festivals, setFestivals] = useState([]);
    const [cursor, setCursor] = useState(null);
    const [hasMore, setHasMore] = useState(true);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);

    const fetchFestivals = useCallback(async (isInitialLoad = false) => {
        if (isLoading || (!isInitialLoad && !hasMore)) return;
        setIsLoading(true);
        setError(null);

        try {
            const params = {
                pageSize: '6',
                ...(cursor && !isInitialLoad ? { time: cursor.time, id: cursor.id.toString() } : {})
            };

            const response = await axios.get('http://localhost:8080/api/v1/festivals', { params });
            const { data } = response.data;

            setFestivals((prev) => {
                if (isInitialLoad) {
                    return data.content;
                } else {
                    return [...prev, ...data.content];
                }
            });

            if (data.content.length > 0) {
                const lastFestival = data.content[data.content.length - 1];
                setCursor({ time: lastFestival.startTime, id: lastFestival.festivalId });
            } else {
                setHasMore(false);
            }

        } catch (error) {
            console.error('Failed to fetch festivals:', error);
            setError('축제 목록을 불러오는 데 실패했습니다. 다시 시도해주세요.');
        } finally {
            setIsLoading(false);
        }
    }, [cursor, isLoading, hasMore]);

    useEffect(() => {
        fetchFestivals(true);
    }, []);

    const loadMore = useCallback(() => {
        if (hasMore && !isLoading) {
            fetchFestivals(false);
        }
    }, [hasMore, isLoading, fetchFestivals]);

    return { festivals, hasMore, isLoading, error, loadMore };
};

export default function FestivalList() {
    const { festivals, hasMore, isLoading, error, loadMore } = useFestivals();

    return (
        <div className="min-h-screen flex flex-col bg-background">
            <main className="flex-grow container mx-auto px-4 py-8 max-w-4xl">
                <h1 className="text-3xl font-bold mb-8 text-center">세상의 모든 축제!</h1>
                <div className="mb-8 w-full">
                    <Input
                        type="search"
                        placeholder="Search for festivals..."
                        className="w-full"
                    />
                </div>

                {error && <p className="text-destructive text-center mb-4">{error}</p>}
                <InfiniteScroll
                    dataLength={festivals.length}
                    next={loadMore}
                    hasMore={hasMore}
                    loader={
                        <div className="flex justify-center items-center py-4">
                            <Loader2 className="w-6 h-6 animate-spin text-primary" />
                            <span className="ml-2 text-muted-foreground">Loading more festivals...</span>
                        </div>
                    }
                    endMessage={<p className="text-center py-4 text-muted-foreground"><b>You've seen all festivals!</b></p>}
                    scrollThreshold={0.9}
                >
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                        {festivals.map((festival) => (
                            <FestivalCard key={festival.festivalId} festival={festival} />
                        ))}
                    </div>
                </InfiniteScroll>
                {isLoading && festivals.length === 0 && (
                    <div className="flex justify-center items-center py-4">
                        <Loader2 className="w-6 h-6 animate-spin text-primary" />
                        <span className="ml-2 text-muted-foreground">Loading festivals...</span>
                    </div>
                )}
            </main>
        </div>
    );
}
