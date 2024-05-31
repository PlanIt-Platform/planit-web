import React from 'react';

export function CalendarDays({events, calendarDay, changeCalendarDay}){
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
                const eventsForDay = events.filter(event => new Date(event.date).toDateString() === day.date.toDateString());
                if (eventsForDay.length === 1) {
                    dayClass = 'day-with-one-event';
                } else if (eventsForDay.length === 2) {
                    dayClass = 'day-with-two-events';
                } else if (eventsForDay.length >= 3) {
                    dayClass = 'day-with-three-or-more-events';
                }

                return (
                    <div key={index} className={`calendar-day ${dayClass} ${day.currentMoth ? 'current-month' : ''} 
                    ${day.selected ? 'selected' : ''}`} onClick={() => changeCalendarDay(day)}>
                        <p>{day.number}</p>
                    </div>
                )
            })}
        </div>
    )
}