import React from 'react';
import './style.css';
import AdminLayout from "./AdminLayout"; // Assuming you'll style this using CSS

function PurchaseManagement() {
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
                        체크인 <span>0/0</span> | 미승인 <span>0</span>
                    </div>
                    <div className="actions">
                        <button className="download-button">구매자 데이터 다운로드</button>
                        <button className="filter-button">필터</button>
                        <button className="email-button">선택 이메일 보내기</button>
                    </div>
                </div>

                <div className="table">
                    <div className="table-header">
                        <div className="header-item">티켓 번호</div>
                        <div className="header-item">이름</div>
                        <div className="header-item">이메일 주소</div>
                        <div className="header-item">휴대폰 번호</div>
                        <div className="header-item">구매 일자</div>
                        <div className="header-item">티켓 이름</div>
                        <div className="header-item">구매 가격(원)</div>
                        <div className="header-item">체크인</div>
                    </div>
                    <div className="table-row no-results">
                        결과가 없습니다.
                    </div>
                </div>
            </div>
        </AdminLayout>
    );
}

export default PurchaseManagement;
