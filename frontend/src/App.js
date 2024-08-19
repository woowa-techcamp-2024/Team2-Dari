import {Route, Routes} from 'react-router-dom';
import FestivalList from "./pages/festival/FestivalList";
import FestivalDetail from "./pages/festival/FestivalDetail";
import MyTicketList from "./pages/my/MyTicketList";

function App() {
    return (
        <div>
            <Routes>
                <Route path="/" element={<FestivalList/>}/>
                <Route path="/festivals" element={<FestivalList/>}/>
                <Route path="/festivals/:id" element={<FestivalDetail/>}/>
                <Route path="/my/tickets" element={<MyTicketList/>}/>
            </Routes>
        </div>
    );
}

export default App;
