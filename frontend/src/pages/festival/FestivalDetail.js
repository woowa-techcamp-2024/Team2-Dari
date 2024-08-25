import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import apiClient from '../../utils/apiClient';
import { Button } from "../../components/ui/button";
import { Card } from "../../components/ui/card";

const FestivalDetail = () => {
    const { festivalId } = useParams();
    const navigate = useNavigate();
    const [festival, setFestival] = useState(null);
    const [tickets, setTickets] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchFestivalDetails = async () => {
            try {
                const festivalResponse = await apiClient.get(`/festivals/${festivalId}`);
                setFestival(festivalResponse.data.data);

                const ticketsResponse = await apiClient.get(`/festivals/${festivalId}/tickets`);
                setTickets(ticketsResponse.data.data.tickets || []);
            } catch (err) {
                setError('축제 정보를 불러오는 데 실패했습니다.');
            } finally {
                setIsLoading(false);
            }
        };

        fetchFestivalDetails();
    }, [festivalId]);

    const handlePurchase = (ticketId) => {
        // navigate(`/festivals/${festivalId}/tickets/${ticketId}/queue`);
        navigate(`/festivals/${festivalId}/tickets/${ticketId}/purchase`);
    };

    if (isLoading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>{error}</div>;
    }

    if (!festival) {
        return <div>축제 정보를 찾을 수 없습니다.</div>;
    }

    return (
        <div className="container mx-auto px-4 py-8">
            <Card className="mb-8">
                <img
                    src={festival.festivalImg || "https://via.placeholder.com/400x200"}
                    alt={festival.title}
                    className="w-full h-64 object-cover"
                />
                <div className="p-4">
                    <h1 className="text-2xl font-bold mb-2">{festival.title}</h1>
                    <p className="text-gray-600 mb-4">
                        {new Date(festival.startTime).toLocaleDateString()} ~ 
                        {new Date(festival.endTime).toLocaleDateString()}
                    </p>
                    <p className="mb-4">{festival.description}</p>
                </div>
            </Card>

            <h2 className="text-xl font-bold mb-4">Available Tickets</h2>
            {tickets.length > 0 ? (
                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                    {tickets.map(ticket => (
                        <Card key={ticket.id} className="p-4">
                            <h3 className="text-lg font-semibold mb-2">{ticket.name}</h3>
                            <p className="text-gray-600 mb-2">Price: {ticket.price}원</p>
                            <p className="mb-4">{ticket.detail}</p>
                            <Button 
                                onClick={() => handlePurchase(ticket.id)}
                                className="w-full bg-teal-500 hover:bg-teal-600 text-white"
                            >
                                Buy Ticket
                            </Button>
                        </Card>
                    ))}
                </div>
            ) : (
                <p>현재 구매 가능한 티켓이 없습니다.</p>
            )}
        </div>
    );
};

export default FestivalDetail;