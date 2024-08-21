import React, { useState, useEffect, useCallback, memo } from 'react';
import axios from 'axios';
import InfiniteScroll from 'react-infinite-scroll-component';
import { Button } from "../../components/ui/button"
import { Input } from "../../components/ui/input"
import { Card } from "../../components/ui/card"
import { Link } from 'react-router-dom';
import { Loader2 } from 'lucide-react';

const DEFAULT_IMAGE = 'https://via.placeholder.com/400x300';

const FestivalCard = memo(({ festival }) => {
    const formattedStartDate = new Date(festival.startTime).toLocaleDateString();
    const formattedEndDate = new Date(festival.endTime).toLocaleDateString();

    return (
        <Card className="flex flex-col h-full overflow-hidden transition-all duration-300 hover:shadow-lg">
            <div className="relative aspect-video overflow-hidden">
                <img
                    src={festival.festivalImg || DEFAULT_IMAGE}
                    alt={festival.title}
                    className="w-full h-full object-cover transition-transform duration-300 hover:scale-105"
                    onError={(e) => {e.target.onerror = null; e.target.src = DEFAULT_IMAGE}}
                    loading="lazy"
                />
            </div>
            <div className="p-4 flex-1 flex flex-col">
                <h3 className="text-lg font-semibold mb-2 line-clamp-2">{festival.title}</h3>
                <p className="text-sm text-muted-foreground mb-2">{formattedStartDate} ~ {formattedEndDate}</p>
                <p className="text-sm mb-4 flex-grow line-clamp-3">{festival.description}</p>
                <div className="flex justify-between items-center mt-auto">
                    <span className="text-sm text-muted-foreground">By: {festival.admin.name}</span>
                    <Button variant="outline" size="sm" className="hover:bg-primary hover:text-primary-foreground">
                        View Details
                    </Button>
                </div>
            </div>
        </Card>
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
                pageSize: '9',
                ...(cursor && !isInitialLoad ? { time: cursor.time, id: cursor.id.toString() } : {})
            };

            const response = await axios.get('http://localhost:8080/api/v1/festivals', { params });
            const { data } = response.data;

            setFestivals(prev => {
                const newFestivals = isInitialLoad ? data.content : [...prev, ...data.content];
                return Array.from(new Map(newFestivals.map(item => [item.festivalId, item])).values());
            });

            if (data.content.length > 0) {
                const lastFestival = data.content[data.content.length - 1];
                setCursor({ time: lastFestival.startTime, id: lastFestival.festivalId });
            }
            setHasMore(data.hasNext && data.content.length > 0);
        } catch (error) {
            console.error('Failed to fetch festivals:', error);
            setError('축제 목록을 불러오는 데 실패했습니다. 다시 시도해주세요.');
            setHasMore(false);
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

const Header = () => (
    <header className="bg-background shadow-sm">
        <div className="container mx-auto px-4 py-4 flex justify-between items-center">
            <div className="flex items-center">
                <Link to="/" className="text-2xl font-bold text-primary">축제의 민족</Link>
            </div>
            <nav>
                <ul className="flex space-x-4">
                    <li><Link to="/" className="text-foreground hover:text-primary transition-colors">Home</Link></li>
                    <li><Link to="/about" className="text-foreground hover:text-primary transition-colors">About</Link></li>
                    <li><Link to="/contact" className="text-foreground hover:text-primary transition-colors">Contact</Link></li>
                </ul>
            </nav>
        </div>
    </header>
);

const Footer = () => (
    <footer className="bg-background border-t border-border mt-8">
        <div className="container mx-auto px-4 py-8">
            <div className="flex flex-col md:flex-row justify-between items-center">
                <div className="mb-4 md:mb-0">
                    <Link to="/" className="text-2xl font-bold text-primary">축제의 민족</Link>
                    <p className="text-sm text-muted-foreground mt-2">Discover and celebrate amazing festivals</p>
                </div>
                <div className="flex space-x-4">
                    <Link to="/privacy" className="text-sm text-muted-foreground hover:text-primary transition-colors">Privacy Policy</Link>
                    <Link to="/terms" className="text-sm text-muted-foreground hover:text-primary transition-colors">Terms of Service</Link>
                </div>
            </div>
            <div className="mt-8 text-center text-sm text-muted-foreground">
                © {new Date().getFullYear()} 축제의 민족, Inc. All rights reserved.
            </div>
        </div>
    </footer>
);

export default function FestivalList() {
    const { festivals, hasMore, isLoading, error, loadMore } = useFestivals();

    return (
        <div className="min-h-screen flex flex-col bg-background">
            <Header />
            <main className="flex-grow container mx-auto px-4 py-8">
                <h1 className="text-3xl font-bold mb-8 text-center">Discover Amazing Festivals</h1>
                <div className="mb-8">
                    <Input
                        type="search"
                        placeholder="Search for festivals..."
                        className="w-full max-w-md mx-auto"
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
                            <FestivalCard key={`${festival.festivalId}-${festival.startTime}`} festival={festival} />
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
            <Footer />
        </div>
    );
}