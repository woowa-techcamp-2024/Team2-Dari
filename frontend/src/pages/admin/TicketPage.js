import AdminLayout from "./AdminLayout";
import "./style.css";
function TicketPage() {
    return (
        <AdminLayout>
            <div className="ticket-page">
                <h1 className="title">티켓 (입장권)</h1>
                <p className="description">
                    참가자들이 이벤트에 접속, 혹은 입장 할 수 있도록 티켓을 만들어 주세요. 적어도 1개의 티켓은 필요합니다.
                </p>

                <div className="ticket-section">
                    <button className="create-ticket-button">티켓 새로 생성하기</button>
                    <div className="ticket-info">
                        <div className="ticket-header">
                            <span className="ticket-title">캠퍼</span>
                            <span className="ticket-status">판매중</span>
                        </div>
                        <p className="ticket-details">선착순 · 1,000원 · 27장</p>
                        <div className="ticket-progress">
                            <span className="sales-count">판매량 0 / 27</span>
                            <div className="progress-bar">
                                <div className="progress-bar-fill" style={{width: '0%'}}></div>
                            </div>
                            <span className="progress-percentage">0%</span>
                        </div>
                    </div>
                </div>
            </div>
        </AdminLayout>
    )
}

export default TicketPage;