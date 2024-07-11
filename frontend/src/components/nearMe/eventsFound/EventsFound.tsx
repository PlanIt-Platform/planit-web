import React from "react";


export function EventsFound({events, setSelectedEvent, setIsJoinPopupOpen}) {
    return (
        <div className={"near-right"}>
            <h2>Events (Name and Location):</h2>
            {events.map((event) => (
                <div key={event.id}>
                    <button onClick={() => {
                        setSelectedEvent(event);
                        setIsJoinPopupOpen(true);
                    }}>
                        <p title={`${event.title} - ${event.location}`}>{event.title} - {event.location}</p>
                    </button>
                </div>
            ))}
            {!events.length && <p>No events found</p>}
        </div>
    )
}