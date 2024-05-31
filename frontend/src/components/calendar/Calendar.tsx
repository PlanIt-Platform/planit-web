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

export function Calendar() {
    const [events, setEvents] = useState([]);
    const [error, setError] = useState('');
    const [calendarDay, setCalendarDay] = useState(new Date());
    const weekDays = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    const months = ['January', 'February', 'March', 'April', 'May',
        'June', 'July', 'August', 'September', 'October', 'November', 'December'];

    useEffect(() => {
        getUserEvents()
            .then((res) => {
                if (res.data.error) {
                    setError(res.data.error);
                    return;
                }
                setEvents(res.data.events);
            });
    }, []);

    const changeCalendarDay = (day) => {
        setCalendarDay(new Date(day.year, day.month, day.number));
    }

    const nextMonth = () => {
        setCalendarDay(new Date(calendarDay.getFullYear(), calendarDay.getMonth() + 1, calendarDay.getDate()));
    }

    const previousMonth = () => {
        setCalendarDay(new Date(calendarDay.getFullYear(), calendarDay.getMonth() - 1, calendarDay.getDate()));
    }

    return (
        <div className="calendar-container">
            <div className="calendar">
                <div className="calendar-header">
                    <button onClick={previousMonth}>
                        <img src={left_arrow} alt="left arrow"/>
                    </button>
                    <h2>{months[calendarDay.getMonth()]} {calendarDay.getFullYear()}</h2>
                    <button onClick={nextMonth}>
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
                    <CalendarDays events={events} calendarDay={calendarDay} changeCalendarDay={changeCalendarDay}/>
                </div>
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
        </div>
    );
}