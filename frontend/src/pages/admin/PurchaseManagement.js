import React, { useEffect, useState } from 'react';
import './style.css';
import AdminLayout from "./AdminLayout";

function PurchaseManagement() {
    const [participants, setParticipants] = useState([]);

    // Mock 데이터를 설정
    const mockData = {
        data: {
            participants: [
                {
                    participantId: 1,
                    participantName: "John Doe",
                    participantEmail: "john.doe@example.com",
                    ticketId: 101,
                    ticketName: "VIP Ticket",
                    purchaseId: 1001,
                    purchaseTime: "2024-08-19T10:00",
                    checkinId: 1,
                    isCheckin: true,
                },
                {
                    participantId: 2,
                    participantName: "Jane Smith",
                    participantEmail: "jane.smith@example.com",
                    ticketId: 102,
                    ticketName: "Standard Ticket",
                    purchaseId: 1002,
                    purchaseTime: "2024-08-19T11:00",
                    checkinId: 2,
                    isCheckin: false,
                },
                {
                    participantId: 3,
                    participantName: "Alice Johnson",
                    participantEmail: "alice.johnson@example.com",
                    ticketId: 103,
                    ticketName: "Standard Ticket",
                    purchaseId: 1003,
                    purchaseTime: "2024-08-19T12:00",
                    checkinId: 3,
                    isCheckin: true,
                },
                {
                    participantId: 4,
                    participantName: "Bob Brown",
                    participantEmail: "bob.brown@example.com",
                    ticketId: 104,
                    ticketName: "Standard Ticket",
                    purchaseId: 1004,
                    purchaseTime: "2024-08-19T13:00",
                    checkinId: 4,
                    isCheckin: false,
                },
                {
                    participantId: 5,
                    participantName: "Charlie Black",
                    participantEmail: "charlie.black@example.com",
                    ticketId: 105,
                    ticketName: "VIP Ticket",
                    purchaseId: 1005,
                    purchaseTime: "2024-08-19T14:00",
                    checkinId: 5,
                    isCheckin: true,
                },
                {
                    participantId: 6,
                    participantName: "Diana Green",
                    participantEmail: "diana.green@example.com",
                    ticketId: 106,
                    ticketName: "Standard Ticket",
                    purchaseId: 1006,
                    purchaseTime: "2024-08-19T15:00",
                    checkinId: 6,
                    isCheckin: false,
                },
                {
                    participantId: 7,
                    participantName: "Ethan White",
                    participantEmail: "ethan.white@example.com",
                    ticketId: 107,
                    ticketName: "Standard Ticket",
                    purchaseId: 1007,
                    purchaseTime: "2024-08-19T16:00",
                    checkinId: 7,
                    isCheckin: true,
                },
                {
                    participantId: 8,
                    participantName: "Fiona Blue",
                    participantEmail: "fiona.blue@example.com",
                    ticketId: 108,
                    ticketName: "VIP Ticket",
                    purchaseId: 1008,
                    purchaseTime: "2024-08-19T17:00",
                    checkinId: 8,
                    isCheckin: true,
                },
                {
                    participantId: 9,
                    participantName: "George Red",
                    participantEmail: "george.red@example.com",
                    ticketId: 109,
                    ticketName: "Standard Ticket",
                    purchaseId: 1009,
                    purchaseTime: "2024-08-19T18:00",
                    checkinId: 9,
                    isCheckin: false,
                },
                {
                    participantId: 10,
                    participantName: "Helen Yellow",
                    participantEmail: "helen.yellow@example.com",
                    ticketId: 110,
                    ticketName: "Standard Ticket",
                    purchaseId: 1010,
                    purchaseTime: "2024-08-19T19:00",
                    checkinId: 10,
                    isCheckin: false,
                }
            ],
            currentPage: 1,
            itemsPerPage: 10,
            totalItems: 10,
            totalPages: 1,
            hasNext: false,
            hasPrevious: false
        }
    };

    useEffect(() => {
        // Mock 데이터를 바로 설정 (API 대신)
        setParticipants(mockData.data.participants);
    }, []);

    return (
        <AdminLayout>
            <div className="purchase-management">
                <h1 className="title">구매 / 참가자 관리</h1>

                <div className="search-bar">
                    <input
                        type="text"
                        placeholder="이름, 이메일, 전화번호, 티켓ID로 검색"
                        className="search-input"
                    />
                </div>

                <div className="tabs">
                    <button className="tab active">구매자 목록</button>
                    <button className="tab">미승인 티켓 목록</button>
                </div>

                <div className="controls">
                    <div className="stats">
                        체크인 <span>{participants.filter(p => p.isCheckin).length}/{participants.length}</span> | 미승인 <span>0</span>
                    </div>
                    <div className="actions">
                        <button className="download-button">구매자 데이터 다운로드</button>
                        <button className="filter-button">필터</button>
                        <button className="email-button">선택 이메일 보내기</button>
                    </div>
                </div>

                <div className="table">
                    <table>
                        <thead className="table-header">
                        <tr>
                            <th className="header-item">티켓 번호</th>
                            <th className="header-item">이름</th>
                            <th className="header-item">이메일 주소</th>
                            <th className="header-item">구매 일자</th>
                            <th className="header-item">티켓 이름</th>
                            <th className="header-item">구매 ID</th>
                            <th className="header-item">체크인</th>
                        </tr>
                        </thead>
                        <tbody>
                        {participants.length > 0 ? (
                            participants.map(participant => (
                                <tr key={participant.participantId} className="table-row">
                                    <td className="row-item">{participant.ticketId}</td>
                                    <td className="row-item">{participant.participantName}</td>
                                    <td className="row-item">{participant.participantEmail}</td>
                                    <td className="row-item">{participant.purchaseTime}</td>
                                    <td className="row-item">{participant.ticketName}</td>
                                    <td className="row-item">{participant.purchaseId}</td>
                                    <td className="row-item">{participant.isCheckin ? "체크인됨" : "미체크인"}</td>
                                </tr>
                            ))
                        ) : (
                            <tr className="table-row no-results">
                                <td colSpan="7">결과가 없습니다.</td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </div>
            </div>
        </AdminLayout>
    );
}

export default PurchaseManagement;
