import React from "react";


export function EventsFound({events}) {
    return (
        <div className={"near-right"}>
            <h2>Events (Name and Location):</h2>
            {events.map(({id, title, location}) => (
                <div key={id}>
                    <p title={`${title} - ${location}`}>{title} - {location}</p>
                </div>
            ))}
            {!events.length && <p>No events found</p>}
        </div>
    )
}