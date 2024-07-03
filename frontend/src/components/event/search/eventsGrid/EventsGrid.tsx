import private_bg from "../../../../../images/private.png";
import date from "../../../../../images/date.png";
import {formatDate} from "../../shared/formatDate";
import location from "../../../../../images/location.png";
import React from "react";
import simplemeetingbg from "../../../../../images/simplemeeting.png";
import sportsbg from "../../../../../images/sportbg.png";
import culturebg from "../../../../../images/culturebg.png";
import educationbg from "../../../../../images/educationbg.png";
import foodbg from "../../../../../images/foodbg.png";
import charitybg from "../../../../../images/caridadebg.png";
import technologybg from "../../../../../images/technologybg.png";
import businessbg from "../../../../../images/businessbg.png";
import {getUsersInEvent} from "../../../../services/eventsServices";

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

export function EventsGrid(
    {
        userId,
        pageEvents,
        setSelectedEvent,
        setIsJoinPopupOpen,
        setIsParticipant,
        setIsLoading,
        setError
    }) {

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

    return (
        <div className="events-grid">
            {pageEvents.length === 0 && <h1>No events found.</h1>}
            {pageEvents.map((event: any) => (
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
                                <p title={event.date} style={{fontSize: 15}}>{formatDate(event.date)}</p>
                            </div>
                            <div className="info-container">
                                <img src={location} alt="location" className={"info_img"}/>
                                <p title={event.location}>{event.location || "To be determined"}</p>
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
    )
}