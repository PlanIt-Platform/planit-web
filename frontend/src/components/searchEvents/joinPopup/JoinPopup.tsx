import React, {useState} from 'react';
import {joinEvent} from "../../../services/eventsServices";
import {Navigate} from "react-router-dom";
import "./JoinPopup.css"
import Error from "../../error/Error";
import Loading from "../../loading/Loading";

export default function JoinPopup({ event, onClose }) {
    const [password, setPassword] = useState('');
    const [redirect, setRedirect] = useState(false);
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false)

    if (redirect) return <Navigate to={`/planit/event/${event.id}`} replace={true}/>

    const handleJoin = (ev) => {
        ev.preventDefault()
        setIsLoading(true)
        joinEvent(event.id, password)
            .then(res => {
                if (res.data.error) setError(res.data.error)
                else {
                    setError("")
                    setRedirect(true)
                }
                setIsLoading(false)
            })
    };

    return (
        <>
            {isLoading && <Loading onClose={() => setIsLoading(false)} />}
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
        </>
    );
}