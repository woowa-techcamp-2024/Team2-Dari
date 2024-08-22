import { Route, Routes } from 'react-router-dom';
import FestivalList from "./pages/festival/FestivalList";
import FestivalDetail from "./pages/festival/FestivalDetail";
import MyTicketList from "./pages/my/MyTicketList";
import LoginPage from "./pages/auth/LoginPage";
import ProtectedRoute from "./components/ProtectedRoute";
import Layout from "./components/ui/layout"
import { AuthProvider } from "./components/ui/AuthContext";

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
            </Routes>
        </AuthProvider>
        </div>
    );
}

export default App;