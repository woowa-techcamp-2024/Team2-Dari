import React, {useEffect, useState} from 'react';
import {Link, useParams} from "react-router-dom";
import "./style.css";

function AdminLayout( {children} ) {
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
        <div className="layout-container">
            <aside className="sidebar">
                <div className="sidebar-content">
                    <nav className="sidebar-bottom">
                        <div className="flex flex-col items-start gap-4">
                            <div className="mt-6 flex flex-col items-start gap-4" style={{width: "100%"}}>
                                <div>
                                    <Link
                                        href="#"
                                        className="flex items-center gap-4 rounded-lg bg-accent px-2.5 py-2 text-accent-foreground transition-colors hover:bg-accent/90 md:px-4"
                                        prefetch={false}
                                    >
                                        <CalendarIcon className="h-5 w-5"/>
                                        <span className="text-sm font-medium">페스티벌 정보</span>
                                    </Link>
                                </div>
                                <div>
                                    <Link
                                        href="#"
                                        className="flex items-center gap-4 rounded-lg px-2.5 py-2 text-muted-foreground transition-colors hover:bg-muted/50 md:px-4"
                                        prefetch={false}
                                    >
                                        <TicketIcon className="h-5 w-5"/>
                                        <span className="text-sm font-medium">티켓</span>
                                    </Link>
                                </div>
                                <div>
                                    <Link
                                        href="#"
                                        className="flex items-center gap-4 rounded-lg px-2.5 py-2 text-muted-foreground transition-colors hover:bg-muted/50 md:px-4"
                                        prefetch={false}
                                    >
                                        <UsersIcon className="h-5 w-5"/>
                                        <span className="text-sm font-medium">구매/참가자 관리</span>
                                    </Link>
                                </div>
                            </div>
                        </div>
                    </nav>
                </div>
            </aside>

            <div className="main-content">
                <header className="header">
                    <h1>축제의 민족</h1>
                    <div>{festivalData.title}</div>
                    <button className="btn">프로필</button>
                </header>

                <main className="content">
                    {children}
                </main>
            </div>
        </div>
    );
}

function CalendarIcon(props) {
    return (
        <svg
            {...props}
            xmlns="http://www.w3.org/2000/svg"
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
        >
            <path d="M8 2v4" />
            <path d="M16 2v4" />
            <rect width="18" height="18" x="3" y="4" rx="2" />
            <path d="M3 10h18" />
        </svg>
    );
}

function ZoomInIcon(props) {
    return (
        <svg
            {...props}
            xmlns="http://www.w3.org/2000/svg"
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
        >
            <circle cx="11" cy="11" r="8" />
            <line x1="21" x2="16.65" y1="21" y2="16.65" />
            <line x1="11" x2="11" y1="8" y2="14" />
            <line x1="8" x2="14" y1="11" y2="11" />
        </svg>
    );
}

function TicketIcon(props) {
    return (
        <svg
            {...props}
            xmlns="http://www.w3.org/2000/svg"
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
        >
            <path d="M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2Z" />
            <path d="M13 5v2" />
            <path d="M13 17v2" />
            <path d="M13 11v2" />
        </svg>
    );
}

function UsersIcon(props) {
    return (
        <svg
            {...props}
            xmlns="http://www.w3.org/2000/svg"
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
        >
            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
            <circle cx="9" cy="7" r="4" />
            <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
            <path d="M16 3.13a4 4 0 0 1 0 7.75" />
        </svg>
    );
}

export default AdminLayout;
