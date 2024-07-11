import React, {useContext, useEffect, useState} from "react";
import {searchEvents} from "../../../services/eventsServices";
import './SearchEvents.css'
import CreateForm from "../create/CreateForm";
import JoinPopup from "../join/JoinPopup";
import {Navigate} from "react-router-dom";
import {PlanItContext} from "../../../PlanItProvider";
import {getUserId} from "../../authentication/Session";
import Error from "../../shared/error/Error";
import Loading from "../../shared/loading/Loading";
import {GoogleCalendar} from "../../googleCalendar/GoogleCalendar";
import {CategoryList} from "./categoryList/CategoryList";
import {EventsGrid} from "./eventsGrid/EventsGrid";
import {BottomBar} from "./bottomBar/BottomBar";

export default function SearchEvents(): React.ReactElement {
    const { eventsSearched } = useContext(PlanItContext);
    const userId = getUserId()
    const [error, setError] = useState('')
    const [isLoading, setIsLoading] = useState(false)
    const [isEventFormOpen, setIsEventFormOpen] = useState(false);
    const [isJoinPopupOpen, setIsJoinPopupOpen] = useState(false);
    const [selectedEvent, setSelectedEvent] = useState(null);
    const [isParticipant, setIsParticipant] = useState(false);
    const [currentPage, setCurrentPage] = useState(1);
    const [redirect, setRedirect] = useState(false);
    const [event, setEvent] = useState(null);
    const [isGooglePopupOpen, setIsGooglePopupOpen] = useState(false)

    const itemsPerPage = 9
    const [pageEvents, setPageEvents] = useState(eventsSearched)

    useEffect(() => {
        setPageEvents(eventsSearched);
    }, [eventsSearched]);

    useEffect(() => {
        if (!isEventFormOpen) {
            setIsLoading(true)
            searchEvents("All", itemsPerPage, currentPage * itemsPerPage - itemsPerPage)
                .then((res) => {
                    if (res.data.error) setError(res.data.error);
                    else {
                        setPageEvents(res.data.events)
                        setError('')
                    }
                    setIsLoading(false)
                });
        }
    }, [isEventFormOpen, currentPage])

    if (redirect) return <Navigate to={`/planit/event/${event.id}`} replace={true}/>

    if (isParticipant) return <Navigate to={`/planit/event/${selectedEvent.id}`} replace={true}/>

    return (
        <div>
            {isLoading && <Loading/>}
            <CategoryList
                setPageEvents={setPageEvents}
                currentPage={currentPage}
                itemsPerPage={itemsPerPage}
                setIsLoading={setIsLoading}
                setError={setError}
            />
            <EventsGrid
                userId={userId}
                pageEvents={pageEvents}
                setSelectedEvent={setSelectedEvent}
                setIsJoinPopupOpen={setIsJoinPopupOpen}
                setIsParticipant={setIsParticipant}
                setIsLoading={setIsLoading}
                setError={setError}
            />
            <BottomBar
                currentPage={currentPage}
                setCurrentPage={setCurrentPage}
                pageEvents={pageEvents}
                itemsPerPage={itemsPerPage}
                setIsLoading={setIsLoading}
                setIsEventFormOpen={setIsEventFormOpen}
                setIsGooglePopupOpen={setIsGooglePopupOpen}
                setEvent={setEvent}
                setError={setError}
            />
            {isGooglePopupOpen && <GoogleCalendar mode="addEvent" onClose={() => {
                setIsGooglePopupOpen(false)
                setRedirect(true)
            }} input={event} />}
            {isJoinPopupOpen && !isParticipant && <JoinPopup event={selectedEvent} onClose={() => setIsJoinPopupOpen(false)} />}
            {isEventFormOpen && <CreateForm onClose={() => setIsEventFormOpen(false)} />}
            {error && <Error message={error} onClose={() => setError(null)} />}
        </div>
    )
}