import React, {useContext, useEffect, useState} from "react";
import {getUsersInEvent, searchEvents} from "../../services/eventsServices";
import './SearchEvents.css'
import sports_icon from "../../../images/sports_icon.png"
import globe_icon from "../../../images/globe_icon.png"
import culture_icon from "../../../images/culture_icon.png"
import education_icon from "../../../images/education_icon.png"
import food_icon from "../../../images/food_icon.png"
import charity_icon from "../../../images/charity_icon.png"
import technology_icon from "../../../images/technology_icon.png"
import business_icon from "../../../images/business_icon.png"
import sportsbg from "../../../images/sportbg.png";
import culturebg from "../../../images/culturebg.png";
import educationbg from "../../../images/educationbg.png";
import foodbg from "../../../images/foodbg.png";
import charitybg from "../../../images/caridadebg.png";
import technologybg from "../../../images/technologybg.png";
import businessbg from "../../../images/businessbg.png";
import simplemeetingbg from "../../../images/simplemeeting.png";
import private_bg from "../../../images/private.png";
import EventForm from "./createEvent/EventForm";
import JoinPopup from "./joinPopup/JoinPopup";
import {Navigate} from "react-router-dom";
import {PlanItContext} from "../../PlanItProvider";
import location from "../../../images/location.png";
import date from "../../../images/date.png";
import {getUserId} from "../authentication/Session";
import Error from "../error/Error";
import Loading from "../loading/Loading";

const categoryIcons = {
    'All': globe_icon,
    'Sports and Fitness': sports_icon,
    'Arts and Culture': culture_icon,
    'Education': education_icon,
    'Food and Drinks': food_icon,
    'Charity': charity_icon,
    'Technology': technology_icon,
    'Business': business_icon
};

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

export default function SearchEvents(): React.ReactElement {
    const { eventsSearched } = useContext(PlanItContext);
    const userId = getUserId()
    const [events, setEvents] = useState(eventsSearched)
    const [error, setError] = useState('')
    const [isLoading, setIsLoading] = useState(false)
    const [isEventFormOpen, setIsEventFormOpen] = useState(false);
    const [isJoinPopupOpen, setIsJoinPopupOpen] = useState(false);
    const [selectedEvent, setSelectedEvent] = useState(null);
    const [isParticipant, setIsParticipant] = useState(false);

    useEffect(() => {
        setEvents(eventsSearched);
    }, [eventsSearched]);

    useEffect(() => {
        if (!isEventFormOpen) {
            setIsLoading(true)
            searchEvents("All")
                .then((res) => {
                    if (res.data.error) setError(res.data.error);
                    else {
                        setEvents(res.data.events);
                        setError('')
                    }
                    setIsLoading(false)
                });
        }
    }, [isEventFormOpen])

    const handleCategoryClick = (category: string) => {
        setIsLoading(true)
        searchEvents(category)
            .then((res) => {
                if (res.data.error) setError(res.data.error);
                else {
                    if (category === "All") setEvents(res.data.events);
                    else setEvents(res.data.events.filter(event => event.visibility === "Public"));
                    setError('')
                }
                setIsLoading(false)
            });
    }

    const handleEventClick = (event) => {
        setSelectedEvent(event);
        setIsLoading(true)
        getUsersInEvent(event.id)
            .then((res) => {
                if (res.data.error) setError(res.data.error);
                else {
                    const users = res.data.users;
                    if (users.some(user => user.id === userId)) {
                        setIsParticipant(true);
                    } else {
                        setIsJoinPopupOpen(true);
                    }
                }
                setIsLoading(false)
            });
    };

    if (isParticipant) return <Navigate to={`/planit/event/${selectedEvent.id}`} replace={true}/>

    return (
        <div>
            {isLoading && <Loading onClose={() => setIsLoading(false)} />}
            <div className="category-scroll">
                {Object.keys(categoryIcons).map(category => (
                    <button key={category} onClick={() => handleCategoryClick(category)}>
                        <img src={categoryIcons[category]} alt={category} title={category} className={"images"}/>
                    </button>
                ))}
            </div>
            <div className="events-grid">
                {events.length === 0 && <h1>No events found.</h1>}
                {events.map((event: any) => (
                    <div key={event.id} className="event-card" onClick={() => handleEventClick(event)}>
                        <img src={
                            event.visibility === "Private"
                                ? private_bg
                                : categoryBackgrounds[event.category]
                        } alt={event.category} className={"event-card-img"}/>
                        <div className="search-event-title-container">
                            <p className="search-event-title">{event.title}</p>
                        </div>
                        {event.visibility === "Public"
                            ? <div>
                                    <div className="info-container">
                                        <img src={date} alt="date" className={"info_img"}/>
                                        <p style={{fontSize: 15}}>{formatDate(event.date)}</p>
                                    </div>
                                    <div className="info-container">
                                        <img src={location} alt="location" className={"info_img"}/>
                                        <p>{event.location}</p>
                                    </div>

                            </div>
                            :
                            <div>
                                <p style={{fontSize: 20, marginTop: 20}}>Private</p>
                                <p style={{fontSize: 18, fontWeight: "bold"}}>Password required</p>
                            </div>
                        }
                    </div>
                ))}
            </div>
            <button className="floating-button" onClick={() => {
                setIsLoading(false)
                setIsEventFormOpen(true)
            }}>+</button>
            {isJoinPopupOpen && !isParticipant && <JoinPopup event={selectedEvent} onClose={() => setIsJoinPopupOpen(false)} />}
            {isEventFormOpen && <EventForm onClose={() => setIsEventFormOpen(false)} />}
            {error && <Error message={error} onClose={() => setError(null)} />}
        </div>
    )
}