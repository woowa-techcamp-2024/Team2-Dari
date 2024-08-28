import { Route, Routes } from 'react-router-dom';
import FestivalList from "./pages/festival/FestivalList";
import FestivalDetail from "./pages/festival/FestivalDetail";
import MyPage from './pages/my/MyPage';
import LoginPage from "./pages/auth/LoginPage";
import SignupPage from "./pages/auth/SignupPage";
import ProtectedRoute from "./components/ProtectedRoute";
import Layout from "./components/ui/layout"
import { AuthProvider } from "./components/contexts/AuthContext";
import TicketPurchasePage from './pages/festival/TicketPurchasePage';
import CreateFestivalPage from './pages/festival/CreateFestivalPage';
import FestivalManagement from './pages/admin/FestivalManagement';
import PaymentProcessPage from './pages/festival/PaymentProcessPage';
import TicketQueuePage from './pages/festival/TicketQueuePage';
import setupAxiosInterceptors from "./components/exception/SetupAxiosInterceptors";
import {useEffect} from "react";
import ErrorBoundary from "./components/exception/ErrorBoundary";
import ErrorPage from "./components/exception/ErrorPage";

function App() {
    useEffect(() => {
        setupAxiosInterceptors();
    }, []);

    return (
        <ErrorBoundary fallback={<Layout><ErrorPage /></Layout>}>
        <div>
        <AuthProvider>
            <Routes>
            <Route path="/" element={<Layout><FestivalList /></Layout>} />
            <Route path="/festivals" element={<Layout><FestivalList /></Layout>} />
            <Route path="/festivals/:festivalId" element={<Layout><FestivalDetail /></Layout>} />
            <Route path="/mypage" element={<Layout><ProtectedRoute><MyPage/></ProtectedRoute></Layout>} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<SignupPage />} />
            <Route path="/create-festival" element={<Layout><ProtectedRoute><CreateFestivalPage /></ProtectedRoute></Layout>} />
            <Route path="/festivals/:festivalId/tickets/:ticketId/purchase" element={<Layout><ProtectedRoute><TicketPurchasePage /></ProtectedRoute></Layout>} />
            <Route path="/admin/:festivalId" element={<Layout><ProtectedRoute><FestivalManagement /></ProtectedRoute></Layout>} />
            <Route path="/festivals/:festivalId/tickets/:ticketId/payment" element={<Layout><ProtectedRoute><PaymentProcessPage /></ProtectedRoute></Layout>} />
            <Route path="/festivals/:festivalId/tickets/:ticketId/queue" element={<Layout><ProtectedRoute><TicketQueuePage /></ProtectedRoute></Layout>} />
            </Routes>
        </AuthProvider>
        </div>
        </ErrorBoundary>
    );
}

export default App;