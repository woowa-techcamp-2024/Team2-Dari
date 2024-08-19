import React from 'react';
import AdminLayout from "./AdminLayout";
import "./style.css";
function FestivalInfo() {
    return (
        <AdminLayout>
            <div className="main-content">
                <main className="content">
                    <div className="card">
                        <h2>Event Information</h2>
                        <p>View and manage event details.</p>

                        <form>
                            <div className="form-group">
                                <label htmlFor="event-name">Event Name</label>
                                <input id="event-name" type="text"/>
                            </div>

                            <div className="form-group">
                                <label htmlFor="event-date">Event Date</label>
                                <input id="event-date" type="date"/>
                            </div>

                            <div className="form-group">
                                <label htmlFor="organizer-email">Organizer Email</label>
                                <input id="organizer-email" type="email"/>
                            </div>

                            <div className="form-group">
                                <label htmlFor="organizer-phone">Organizer Phone</label>
                                <input id="organizer-phone" type="tel"/>
                            </div>

                            <div className="form-group">
                                <label htmlFor="event-description">Event Description</label>
                                <textarea id="event-description" rows="3"></textarea>
                            </div>

                            <button type="submit" className="btn">Save Changes</button>
                        </form>
                    </div>
                </main>
            </div>
        </AdminLayout>
    )
        ;
}

export default FestivalInfo;
