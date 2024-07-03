import React from "react";


export function EventsFound({events}) {
    return (
        <div className={"near-right"}>
            <h2>Events (Name and Location):</h2>
            {events.map(({id, title, address}) => (
                <div key={id}>
                    <p title={`${title} - ${address}`}>{title} - {address}</p>
                </div>
            ))}
            {!events.length && <p>No events found</p>}
        </div>
    )
}