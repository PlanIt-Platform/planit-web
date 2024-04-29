import React, {useContext, useEffect, useState} from "react";
import {searchEvents} from "../../services/eventsServices";
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
import EventForm from "./EventForm";
import {Link} from "react-router-dom";
import {PlanItContext} from "../../PlanItProvider";

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

export default function SearchEvents(): React.ReactElement {
    const { eventsSearched } = useContext(PlanItContext);
    const [events, setEvents] = useState(eventsSearched)
    const [error, setError] = useState('')
    const [isEventFormOpen, setIsEventFormOpen] = useState(false);

    useEffect(() => {
        setEvents(eventsSearched);
    }, [eventsSearched]);

    useEffect(() => {
        searchEvents("All")
            .then((res) => {
                if (res.error) {
                    setError(res.error);
                    return;
                }
                setEvents(res.events);
                setError('')
            });
    }, [isEventFormOpen])

    const handleCategoryClick = (category: string) => {
        searchEvents(category)
            .then((res) => {
                if (res.error) {
                    setError(res.error);
                    return;
                }
                setEvents(res.events);
                setError('')
            });
    }

    return (
        <div>
            <div className="category-scroll">
                {Object.keys(categoryIcons).map(category => (
                    <button key={category} onClick={() => handleCategoryClick(category)}>
                        <img src={categoryIcons[category]} alt={category} title={category} className={"images"}/>
                    </button>
                ))}
            </div>
            <div className="events-grid">
                {events.map((event: any) => (
                    <Link to={`/event/${event.id}`} key={event.id} className="event-card">
                        <img src={categoryBackgrounds[event.category]} alt={event.category}/>
                        <p style={{fontSize: 30, fontWeight: "bold"}}>{event.title}</p>
                        <p>{event.location}</p>
                        <p>{event.date}</p>
                    </Link>
                ))}
                {error && <p>{error}</p>}
            </div>
            <button className="floating-button" onClick={() => setIsEventFormOpen(true)}>+</button>
            {isEventFormOpen && <EventForm onClose={() => setIsEventFormOpen(false)} />}
        </div>
    )
}