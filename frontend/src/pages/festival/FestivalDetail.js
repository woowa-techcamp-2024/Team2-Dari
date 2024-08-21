import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useParams } from 'react-router-dom';
import { Button } from "../../components/ui/button";
import { Card } from "../../components/ui/card";
import { Link } from 'react-router-dom';
import { Loader2 } from 'lucide-react';

const FestivalDetail = () => {
    const { festivalId } = useParams();
    const [festival, setFestival] = useState(null);
    const [tickets, setTickets] = useState([]); // 빈 배열로 초기화
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchFestivalDetails = async () => {
            try {
                const festivalResponse = await axios.get(`http://localhost:8080/api/v1/festivals/${festivalId}`);
                setFestival(festivalResponse.data.data);

                // 티켓 정보를 가져오는 API 호출
                const ticketsResponse = await axios.get(`http://localhost:8080/api/v1/festivals/${festivalId}/tickets`);
                console.log(ticketsResponse.data); // 응답 데이터 확인
                setTickets(ticketsResponse.data.data.tickets || []); // tickets가 없을 경우 빈 배열로 설정
            } catch (err) {
                setError('축제 정보를 불러오는 데 실패했습니다.');
            } finally {
                setIsLoading(false);
            }
        };

        fetchFestivalDetails();
    }, [festivalId]);

    if (isLoading) {
        return (
            <div className="flex justify-center items-center py-8">
                <Loader2 className="w-6 h-6 animate-spin text-primary" />
                <span className="ml-2 text-muted-foreground">Loading festival details...</span>
            </div>
        );
    }

    if (error) {
        return <p className="text-center text-destructive">{error}</p>;
    }

    if (!festival) {
        return <p className="text-center">축제 정보를 찾을 수 없습니다.</p>;
    }

    return (
        <div className="min-h-screen flex flex-col bg-background">
            <main className="flex-grow container mx-auto px-4 py-8">
                <Card className="overflow-hidden">
                    <img
                        src={festival.festivalImg || "https://image.dongascience.com/Photo/2023/10/2be5b7157e8b50ebd8fa34b4772a97c1.jpg"}
                        alt={festival.title}
                        className="w-full h-64 object-cover"
                    />
                    <div className="p-4">
                        <h1 className="text-2xl font-bold mb-2">{festival.title}</h1>
                        <p className="text-sm text-muted-foreground mb-4">
                            {new Date(festival.startTime).toLocaleDateString()} ~ {new Date(festival.endTime).toLocaleDateString()}
                        </p>
                        <p className="text-base mb-4">{festival.description}</p>
                        <p className="text-sm text-muted-foreground mb-4">Hosted by: {festival.adminId}</p>

                        {/* 티켓 정보 표시 */}
                        <h2 className="text-xl font-bold mt-8 mb-4">Available Tickets</h2>
                        {tickets.length > 0 ? (
                            <ul>
                                {tickets.map(ticket => (
                                    <li key={ticket.id} className="mb-4">
                                        <div className="p-4 border rounded-lg">
                                            <h3 className="text-lg font-semibold">{ticket.name}</h3>
                                            <p className="text-sm text-muted-foreground mb-2">Price: {ticket.price}원</p>
                                            <p className="text-sm">{ticket.detail}</p>
                                            <Button variant="outline" size="sm" className="mt-2">
                                                Buy Ticket
                                            </Button>
                                        </div>
                                    </li>
                                ))}
                            </ul>
                        ) : (
                            <p className="text-muted-foreground">No tickets available.</p>
                        )}
                    </div>
                </Card>
            </main>
        </div>
    );
};

export default FestivalDetail;
