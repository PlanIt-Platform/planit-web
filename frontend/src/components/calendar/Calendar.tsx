import {useEffect, useState} from "react";
import React from "react";
import './Calendar.css';
import {CalendarDays} from "./CalendarDays";
import left_arrow from "../../../images/leftarrow.png";
import right_arrow from "../../../images/rightarrow.png";
import {getUserEvents} from "../../services/usersServices";
import yellow_paint from "../../../images/yellow_paint.png";
import purple_paint from "../../../images/purple_paint.png";
import green_paint from "../../../images/green_paint.png";
import Error from "../shared/error/Error";
import Loading from "../shared/loading/Loading";
import {GoogleCalendar} from "../googleCalendar/GoogleCalendar";

export function Calendar() {
    const [events, setEvents] = useState([]);
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(true)
    const [calendarDay, setCalendarDay] = useState(new Date());
    const [isGetEventsPopUpOpen, setGetEventsPopUpOpen] = useState(true)
    const [isAddEventsPopUpOpen, setAddEventsPopUpOpen] = useState(false)
    const weekDays = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    const months = ['January', 'February', 'March', 'April', 'May',
        'June', 'July', 'August', 'September', 'October', 'November', 'December'];

    useEffect(() => {
        setIsLoading(true)
        getUserEvents()
            .then((res) => {
                if (res.data.error) setError(res.data.error);
                else setEvents(res.data.events);
                setIsLoading(false)
            });
    }, []);

    const changeMonth = (monthChange) => {
        setCalendarDay(new Date(calendarDay.getFullYear(), calendarDay.getMonth() + monthChange, calendarDay.getDate()));
    }

    return (
        <div className="calendar-container">
            {isLoading && <Loading/>}
            <div className="calendar">
                <div className="calendar-header">
                    <button onClick={() => changeMonth(-1)}>
                        <img src={left_arrow} alt="left arrow"/>
                    </button>
                    <h2>{months[calendarDay.getMonth()]} {calendarDay.getFullYear()}</h2>
                    <button onClick={() => changeMonth(1)}>
                        <img src={right_arrow} alt="right arrow"/>
                    </button>
                </div>
                <div className="calendar-body">
                    <div className="table-header">
                        {weekDays.map((day, index) => (
                            <div key={index} className="calendar-weekday">
                                <p>{day}</p>
                            </div>
                        ))}
                    </div>
                    <CalendarDays events={events} calendarDay={calendarDay}/>
                </div>
                <p className="add_event_p">
                    Wish to add these events to your Google Calendar?
                    Click <button onClick={() => setAddEventsPopUpOpen(true)}>here</button>
                </p>
            </div>
            <div className="color-info">
                <div className="color-info-item">
                    <img src={yellow_paint} alt="yellow paint"/>
                    <p>1 event</p>
                </div>
                <div className="color-info-item">
                    <img src={green_paint} alt="green paint"/>
                    <p>2 events</p>
                </div>
                <div className="color-info-item">
                    <img src={purple_paint} alt="purple paint"/>
                    <p>3+ events</p>
                </div>
            </div>
            {error && <Error message={error} onClose={() => setError(null)} />}
            {isAddEventsPopUpOpen && <GoogleCalendar mode="addEvent" onClose={() => {
                setAddEventsPopUpOpen(false)
            }} input={events} />}
            {isGetEventsPopUpOpen && <GoogleCalendar mode="getEvents" onClose={() => {
                setGetEventsPopUpOpen(false)
            }} onEventsRetrieved={(newEvents) => setEvents(prevEvents => [...prevEvents, ...newEvents])} />}
        </div>
    );
}