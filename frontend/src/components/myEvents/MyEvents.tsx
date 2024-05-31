import React, {useEffect, useState} from "react";
import {getUserEvents} from "../../services/usersServices";
import {Link, Navigate} from "react-router-dom";
import date from "../../../images/date.png";
import location from "../../../images/location.png";
import simplemeetingbg from "../../../images/simplemeeting.png";
import sportsbg from "../../../images/sportbg.png";
import culturebg from "../../../images/culturebg.png";
import educationbg from "../../../images/educationbg.png";
import foodbg from "../../../images/foodbg.png";
import charitybg from "../../../images/caridadebg.png";
import technologybg from "../../../images/technologybg.png";
import businessbg from "../../../images/businessbg.png";

const categoryBackgrounds = {
    'Simple Meeting': simplemeetingbg,
    'Sports and Fitness': sportsbg,
    'Arts and Culture': culturebg,
    'Education': educationbg,
    'Food and Drinks': foodbg,
    'Charity': charitybg,
    'Technology': technologybg,
    'Business': businessbg
}

function formatDate(dateString) {
    // Remove the seconds from the date string
    const dateWithoutSeconds = dateString.slice(0, 16);

    const options: Intl.DateTimeFormatOptions = {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    };
    return new Date(dateWithoutSeconds).toLocaleDateString(undefined, options);
}

export default function MyEvents() {
    const [events, setEvents] = useState([]);
    const [error, setError] = useState('');
    const [redirect, setRedirect] = useState(false);
    const [eventId, setEventId] = useState(0);

    useEffect(() => {
        getUserEvents()
            .then((res) => {
                if (res.data.error) {
                    setError(res.data.error);
                    return;
                }
                console.log(res.data)
                setEvents(res.data.events);
            });
    }, []);

    if (redirect) return <Navigate to={`/planit/event/${eventId}`} replace={true}/>

    return (
        <div>
            <div className="events-grid" style={{paddingTop: 130}}>
                {events.length === 0 && <h1>No events found.</h1>}
                {events.map((event: any) => (
                    <div key={event.id} className="event-card" onClick={() => {
                        setRedirect(true)
                        setEventId(event.id)
                    }}>
                        <img src={categoryBackgrounds[event.category]} alt={event.category} className={"event-card-img"}/>
                        <p style={{fontSize: 30, fontWeight: "bold"}}>{event.title}</p>
                        <div>
                            <div className="info-container">
                                <img src={date} alt="date" className={"info_img"}/>
                                <p style={{fontSize: 15}}>{formatDate(event.date)}</p>
                            </div>
                            <div className="info-container">
                                <img src={location} alt="location" className={"info_img"}/>
                                <p>{event.location}</p>
                            </div>
                        </div>
                    </div>
                ))}
                {error && <p>{error}</p>}
            </div>
        </div>
    );
}