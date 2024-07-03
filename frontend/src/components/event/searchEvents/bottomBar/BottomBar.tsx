import enter from "../../../../../images/enter.png";
import React, {useState} from "react";
import {getEvent, joinEventWithCode} from "../../../../services/eventsServices";

export function BottomBar(
    {
        currentPage,
        setCurrentPage,
        pageEvents,
        itemsPerPage,
        setIsLoading,
        setIsEventFormOpen,
        setIsGooglePopupOpen,
        setEvent,
        setError
    }) {
    const [eventCode, setEventCode] = useState('');

    const handleJoinEvent = () => {
        setIsLoading(true)
        joinEventWithCode(eventCode)
            .then((res) => {
                if (res.data.error) {
                    setError(res.data.error);
                    setIsLoading(false);
                    return
                }
                else {
                    setIsGooglePopupOpen(true)
                    getEvent(res.data.id)
                        .then((res) => {
                            if (res.data.error) setError(res.data.error)
                            else {
                                setEvent(res.data)
                                console.log(res.data)
                                setError('');
                            }
                        });
                }
                setIsLoading(false)
            });
    };

    return (
        <>
            <div className="join-event">
                <p className="join-with-code-title">Don't waste time</p>
                <div className="join-with-code-container">
                    <input type="text"
                           placeholder="Insert event code"
                           onChange={(e) => setEventCode(e.target.value)}
                           className="eventCodeInput"/>
                    <img src={enter} alt="Join Event" className="enterButton"
                         onClick={handleJoinEvent}/>
                </div>
            </div>
            <div className="pagination">
                <button onClick={() => setCurrentPage(page => Math.max(page - 1, 1))}>&lt;</button>
                <p>{currentPage}</p>
                <button onClick={() => {
                    if (pageEvents.length < itemsPerPage) return
                    setCurrentPage(page => Math.min(page + 1))
                }}>&gt;</button>
            </div>
            <button className="floating-button" onClick={() => {
                setIsLoading(false)
                setIsEventFormOpen(true)
            }}>+</button>
        </>
    )
}