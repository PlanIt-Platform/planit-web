import React, {useEffect, useState} from 'react';
import {getEvent, joinEvent} from "../../../services/eventsServices";
import {Navigate} from "react-router-dom";
import "./JoinPopup.css"
import Error from "../../shared/error/Error";
import Loading from "../../shared/loading/Loading";
import {GoogleCalendar} from "../../googleCalendar/GoogleCalendar";

export default function JoinPopup({ event, onClose }) {
    const [password, setPassword] = useState('');
    const [redirect, setRedirect] = useState(false);
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false)
    const [isGooglePopupOpen, setIsGooglePopupOpen] = useState(false)
    const [eventDetails, setEventDetails] = useState(null)

    useEffect(() => {
        setIsLoading(true)
        getEvent(event.id)
            .then((res) => {
                if (res.data.error) setError(res.data.error)
                else setEventDetails(res.data)
                setIsLoading(false)
            });


    }, [event]);

    if (redirect) return <Navigate to={`/planit/event/${event.id}`} replace={true}/>

    const handleJoin = (ev) => {
        ev.preventDefault()
        setIsLoading(true)
        joinEvent(event.id, password)
            .then(res => {
                if (res.data.error) setError(res.data.error)
                else {
                    setError("")
                    setIsGooglePopupOpen(true)
                }
                setIsLoading(false)
            })
    };

    if (isLoading) return <Loading/>;

    return (
        <>
            <div className={"overlay"} onClick={onClose}></div>
            <div className="join-event-container">
                <h2>Do you wish to join this event?</h2>
                <form onSubmit={handleJoin}>
                    {event.visibility === 'Private' && (
                        <input type="password" name="password" value={password} onChange={e => setPassword(e.target.value)}
                               placeholder="Password*" required/>
                    )}
                    <button type="submit">Join</button>
                </form>
                {error && <Error message={error} onClose={() => setError(null)} />}
            </div>
            {isGooglePopupOpen && <GoogleCalendar mode="addEvent" onClose={() => {
                setIsGooglePopupOpen(false)
                setRedirect(true)
            }} input={eventDetails} />}
        </>
    );
}