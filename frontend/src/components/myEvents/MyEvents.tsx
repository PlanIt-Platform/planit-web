import React, {useEffect, useState} from "react";
import {getUserEvents} from "../../services/usersServices";
import {Navigate} from "react-router-dom";
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
import Error from "../shared/error/Error";
import Loading from "../shared/loading/Loading";
import {formatDate} from "../event/shared/formatDate";

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

export default function MyEvents() {
    const [events, setEvents] = useState([]);
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(true)
    const [redirect, setRedirect] = useState(false);
    const [eventId, setEventId] = useState(0);

    useEffect(() => {
        setIsLoading(true)
        getUserEvents()
            .then((res) => {
                if (res.data.error) setError(res.data.error);
                else setEvents(res.data.events);
                setIsLoading(false)
            });
    }, []);

    if (redirect) return <Navigate to={`/planit/event/${eventId}`} replace={true}/>

    return (
        <div>
            {isLoading && <Loading/>}
            <div className="events-grid" style={{paddingTop: 130}}>
                {events.length === 0 && <h1>No events found.</h1>}
                {events.map((event: any) => (
                    <div key={event.id} className="event-card" onClick={() => {
                        setRedirect(true)
                        setEventId(event.id)
                    }}>
                        <img src={categoryBackgrounds[event.category]} alt={event.category} className={"event-card-img"}/>
                        <div className="search-event-title-container">
                            <p className="search-event-title">{event.title}</p>
                        </div>
                        <div>
                            <div className="info-container">
                                <img src={date} alt="date" className={"info_img"}/>
                                <p style={{fontSize: 15}}>{formatDate(event.date)}</p>
                            </div>
                            <div className="info-container">
                                <img src={location} alt="location" className={"info_img"}/>
                                <p>{event.address}</p>
                            </div>
                        </div>
                    </div>
                ))}
                {error && <Error message={error} onClose={() => setError(null)} />}
            </div>
        </div>
    );
}