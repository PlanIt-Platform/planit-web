import React, {useState} from 'react';

export function CalendarDays({events, calendarDay}){
    const [selectedDay, setSelectedDay] = useState(null);
    const [showCard, setShowCard] = useState(false);
    const firstDayOfMonth = new Date(calendarDay.getFullYear(), calendarDay.getMonth(), 1);
    const weekDayOfFirstDay = firstDayOfMonth.getDay();
    let calendarDays = []

    for (let day = 0; day < 42; day++){
        if (day == 0 && weekDayOfFirstDay == 0) firstDayOfMonth.setDate(firstDayOfMonth.getDate() - 7);
        else if (day == 0) firstDayOfMonth.setDate(firstDayOfMonth.getDate() - weekDayOfFirstDay);
        else firstDayOfMonth.setDate(firstDayOfMonth.getDate() + 1);

        const currentDay = {
            currentMoth: (firstDayOfMonth.getMonth() === calendarDay.getMonth()),
            date: (new Date(firstDayOfMonth)),
            month: firstDayOfMonth.getMonth(),
            number: firstDayOfMonth.getDate(),
            selected: (firstDayOfMonth.toDateString() === calendarDay.toDateString()),
            year: firstDayOfMonth.getFullYear()
        }

        calendarDays.push(currentDay);
    }

    return (
        <div className="table-content">
            {calendarDays.map((day, index) => {
                let dayClass = '';
                const eventsPerDay = events.filter(event => new Date(event.date).toDateString() === day.date.toDateString());
                if (eventsPerDay.length === 1) {
                    dayClass = 'day-with-one-event';
                } else if (eventsPerDay.length === 2) {
                    dayClass = 'day-with-two-events';
                } else if (eventsPerDay.length >= 3) {
                    dayClass = 'day-with-three-or-more-events';
                }

                return (
                    <div key={index}>
                        <div key={index} className={`calendar-day ${dayClass} ${day.currentMoth ? 'current-month' : ''} 
                        ${day.selected ? 'selected' : ''}`} onClick={() => {
                            setSelectedDay(day.date.toDateString());
                            setShowCard(!showCard);
                        }}>
                            <p>{day.number}</p>
                            {showCard && selectedDay === day.date.toDateString() && eventsPerDay.length > 0 &&
                                <div className="day-events-container">
                                    {eventsPerDay.map((event, index) => (
                                        <div key={index} className="day-event-card">
                                            <p title={event.title}>{event.title}</p>
                                        </div>
                                    ))}
                                </div>
                            }
                        </div>
                    </div>

                )
            })}
        </div>
    )
}