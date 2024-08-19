import React, { useEffect, useState } from 'react';
import AdminLayout from "./AdminLayout";
import "./style.css";

function TicketPage() {
    const [tickets, setTickets] = useState([]);

    // Mock 데이터를 설정
    const mockData = {
        festivalId: 1,
        tickets: [
            {
                id: 1,
                name: "티켓 이름1",
                detail: "티켓 설명1",
                price: 1000,
                quantity: 100,
                remainStock: 100,
                startSaleTime: "2024-08-20T23:18:56.5252",
                endSaleTime: "2024-08-21T23:18:56.525202",
                refundEndTime: "2024-08-22T23:18:56.525202",
                createdAt: "2024-08-19T23:18:56.525203",
                updatedAt: "2024-08-19T23:18:56.525203"
            },
            {
                id: 2,
                name: "티켓 이름2",
                detail: "티켓 설명2",
                price: 1000,
                quantity: 100,
                remainStock: 100,
                startSaleTime: "2024-08-20T23:18:56.525265",
                endSaleTime: "2024-08-21T23:18:56.525265",
                refundEndTime: "2024-08-22T23:18:56.525266",
                createdAt: "2024-08-19T23:18:56.525266",
                updatedAt: "2024-08-19T23:18:56.525266"
            },
            {
                id: 3,
                name: "티켓 이름3",
                detail: "티켓 설명3",
                price: 1000,
                quantity: 100,
                remainStock: 100,
                startSaleTime: "2024-08-20T23:18:56.525267",
                endSaleTime: "2024-08-21T23:18:56.525267",
                refundEndTime: "2024-08-22T23:18:56.525267",
                createdAt: "2024-08-19T23:18:56.525268",
                updatedAt: "2024-08-19T23:18:56.525268"
            },
            {
                id: 4,
                name: "티켓 이름4",
                detail: "티켓 설명4",
                price: 1000,
                quantity: 100,
                remainStock: 100,
                startSaleTime: "2024-08-20T23:18:56.525268",
                endSaleTime: "2024-08-21T23:18:56.525269",
                refundEndTime: "2024-08-22T23:18:56.525269",
                createdAt: "2024-08-19T23:18:56.525269",
                updatedAt: "2024-08-19T23:18:56.525269"
            },
            {
                id: 5,
                name: "티켓 이름5",
                detail: "티켓 설명5",
                price: 1000,
                quantity: 100,
                remainStock: 100,
                startSaleTime: "2024-08-20T23:18:56.52527",
                endSaleTime: "2024-08-21T23:18:56.52527",
                refundEndTime: "2024-08-22T23:18:56.52527",
                createdAt: "2024-08-19T23:18:56.525271",
                updatedAt: "2024-08-19T23:18:56.525271"
            }
        ]
    };

    useEffect(() => {
        // 데이터를 바로 설정 (API 대신)
        setTickets(mockData.tickets);
    }, []);

    return (
        <AdminLayout>
            <div className="ticket-page">
                <h1 className="title">티켓 (입장권)</h1>
                <p className="description">
                    참가자들이 이벤트에 접속, 혹은 입장 할 수 있도록 티켓을 만들어 주세요. 적어도 1개의 티켓은 필요합니다.
                </p>

                <div className="ticket-section">
                    <button className="create-ticket-button">티켓 새로 생성하기</button>
                    {tickets.map((ticket) => (
                        <div key={ticket.id} className="ticket-info">
                            <div className="ticket-header">
                                <span className="ticket-title">{ticket.name}</span>
                                <span className="ticket-status">{ticket.remainStock > 0 ? "판매중" : "매진"}</span>
                            </div>
                            <p className="ticket-details">
                                선착순 · {ticket.price.toLocaleString()}원 · {ticket.remainStock}장 남음
                            </p>
                            <div className="ticket-progress">
                                <span className="sales-count">판매량 {ticket.quantity - ticket.remainStock} / {ticket.quantity}</span>
                                <div className="progress-bar">
                                    <div className="progress-bar-fill" style={{width: `${(ticket.quantity - ticket.remainStock) / ticket.quantity * 100}%`}}></div>
                                </div>
                                <span className="progress-percentage">{((ticket.quantity - ticket.remainStock) / ticket.quantity * 100).toFixed(2)}%</span>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </AdminLayout>
    );
}

export default TicketPage;
