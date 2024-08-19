import React, { useEffect, useState } from 'react';
import AdminLayout from "./AdminLayout";
import "./style.css";
import { useParams } from "react-router-dom";

function FestivalInfo() {
    const { festivalId } = useParams();
    const [festivalData, setFestivalData] = useState(null);

    // Mock 데이터를 설정
    const mockData = {
        festivalId: festivalId,
        adminId: 1,
        title: "축제 이름",
        description: "축제 설명",
        festivalImg: "image",
        startTime: "2024-09-18T23:18",
        endTime: "2024-09-20T23:18",
        festivalPublicationStatus: "DRAFT",
        festivalProgressStatus: "ONGOING"
    };

    useEffect(() => {
        // 데이터를 바로 설정 (API 대신)
        setFestivalData(mockData);
    }, [festivalId]);

    if (!festivalData) {
        return <div>Loading...</div>;
    }

    return (
        <AdminLayout>
            <div className="main-content">
                <main className="content">
                    <div className="card">
                        <h2>페스티벌 정보</h2>
                        <div className="festival-details">
                            <p><strong>Festival Name:</strong> {festivalData.title}</p>
                            <p><strong>Description:</strong> {festivalData.description}</p>
                            <p><strong>Start Time:</strong> {festivalData.startTime}</p>
                            <p><strong>End Time:</strong> {festivalData.endTime}</p>
                            <p><strong>Status:</strong> {festivalData.festivalPublicationStatus}</p>
                            <p><strong>Progress:</strong> {festivalData.festivalProgressStatus}</p>
                            <p>어드민 정보 들어갈 자리 🚨API 수정 필요</p>
                        </div>
                    </div>
                </main>
            </div>
        </AdminLayout>
    );
}

export default FestivalInfo;
