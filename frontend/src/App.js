import { Route, Routes } from 'react-router-dom';
import FestivalList from "./pages/festival/FestivalList";
import FestivalDetail from "./pages/festival/FestivalDetail";
import MyTicketList from "./pages/my/MyTicketList";
import LoginPage from "./pages/auth/LoginPage";
import ProtectedRoute from "./components/ProtectedRoute";
import Layout from "./components/ui/layout"
import { AuthProvider } from "./components/ui/AuthContext";
import FestivalDashboardInfo from "./pages/festival/FestivalDashboardInfo";
import FestivalDashboardInfo2 from "./pages/festival/FestivalDashboardInfo2";
import FestivalInfo from "./pages/admin/FestivalInfo";
import {Ticket} from "lucide-react";
import TicketPage from "./pages/admin/TicketPage";
import PurchaseManagement from "./pages/admin/PurchaseManagement";

function App() {
    return (
        <div>
        <AuthProvider>
            <Routes>
            <Route path="/" element={<Layout><FestivalList /></Layout>} />
            <Route path="/festivals" element={<Layout><FestivalList /></Layout>} />
            <Route path="/festivals/:festivalId" element={<Layout><FestivalDetail /></Layout>} />
            <Route path="/my/tickets" element={<Layout><MyTicketList /></Layout>} />
            <Route path="/login" element={<LoginPage />} />
                {/* <Route path="/my/tickets" element={<ProtectedRoute><MyTicketList /></ProtectedRoute>} /> */}
                <Route path="/" element={<FestivalList/>}/>
                <Route path="/festivals" element={<FestivalList/>}/>
                <Route path="/festivals/:id" element={<FestivalDetail/>}/>
                <Route path="/my/tickets" element={<MyTicketList/>}/>
                <Route path="/festivals/dashboard/info" element={<FestivalInfo/>}/>
                <Route path="/festivals/dashboard/ticket" element={<TicketPage/>}/>
                <Route path="/festivals/dashboard/purchase-management" element={<PurchaseManagement/>}/>
            </Routes>
        </AuthProvider>
        </div>
    );
}

export default App;