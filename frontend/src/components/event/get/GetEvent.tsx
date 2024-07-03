import React, {useEffect, useState} from "react";
import './GetEvent.css';
import {getEvent, getUsersInEvent} from "../../../services/eventsServices";
import {Navigate, useParams} from "react-router-dom";
import {getUserId} from "../../authentication/Session";
import UserProfile from "../../profile/UserProfile";
import EditForm from "../edit/EditForm";
import {AssignRole} from "./assignRole/AssignRole";
import {ChatRoom} from "../../chat/chatRoom/ChatRoom";
import Error from "../../shared/error/Error";
import Loading from "../../shared/loading/Loading";
import {EventInformation} from "./eventInformation/EventInformation";
import {ParticipantsList} from "./participantsList/ParticipantsList";

export default function GetEvent(): React.ReactElement {
    const userId = getUserId();
    const eventId = useParams().id
    const [isOrganizer, setIsOrganizer] = useState(false);
    const [numberOfOrganizers, setNumberOfOrganizers] = useState(1);
    const [isInEvent, setIsInEvent] = useState(false);
    const [participants, setParticipants] = useState([])
    const [error, setError] = useState('')
    const [isLoading, setIsLoading] = useState(false);
    const [isUserProfileOpen, setIsUserProfileOpen] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const [isAssigningRole, setIsAssigningRole] = useState(false);
    const [participantId, setParticipantId] = useState(0);
    const [update, setUpdate] = useState(false);
    const [redirectHome, setRedirectHome] = useState(false);
    const [event, setEvent] = useState({
        title: "",
        description: "",
        category: "",
        subCategory: "",
        location: "",
        latitude: 0,
        longitude: 0,
        visibility: "",
        date: "",
        endDate: "",
        priceAmount: "",
        priceCurrency: "",
        password: "",
        code: ""
        })

    useEffect(() => {
        setIsLoading(true)
        getEvent(eventId)
            .then((res) => {
                if (res.data.error) {
                    setError(res.data.error)
                    setIsLoading(false)
                    return
                }
                setEvent(res.data)
                getUsersInEvent(eventId)
                    .then((res) => {
                        if (res.data.error) setError(res.data.error)
                        else {
                            const sortedUsers = res.data.users.sort((a, b) => {
                                if (a.roleName === "Organizer") return -1;
                                if (b.roleName === "Organizer") return 1;
                                return 0;
                            })
                            setParticipants(sortedUsers);
                            setIsInEvent(res.data.users.some((user) => user.id === userId))
                            setIsOrganizer(res.data.users.some((user) => user.id === userId && user.roleName === "Organizer"));
                            setNumberOfOrganizers(res.data.users.filter((user) => user.roleName === "Organizer").length);

                            if (!res.data.users.some(participant => participant.id === userId)) {
                                setRedirectHome(true);
                            }
                        }
                        setIsLoading(false)
                    });
            });


    }, [eventId, update]);

    if (redirectHome) return <Navigate to="/planit/events" replace={true}/>;

    return (
        <>
            {isLoading && <Loading/>}
            <div className="event-title-container">
                <h1 className="event-title" title={event.title}>{event.title}</h1>
            </div>
            <div className="container">
                <EventInformation
                    event={event}
                    eventId={eventId}
                    isOrganizer={isOrganizer}
                    isInEvent={isInEvent}
                    numberOfOrganizers={numberOfOrganizers}
                    setIsEditing={setIsEditing}
                    setIsLoading={setIsLoading}
                    setError={setError}
                    setRedirectHome={setRedirectHome}
                />
                <ChatRoom uId={userId} eventId={eventId} isOrganizer={isOrganizer}/>
                <ParticipantsList
                    participants={participants}
                    setIsUserProfileOpen={setIsUserProfileOpen}
                    setParticipantId={setParticipantId}
                    isOrganizer={isOrganizer}
                    setIsAssigningRole={setIsAssigningRole}
                />
            </div>
            {isAssigningRole && <AssignRole onClose={() => {
                setIsAssigningRole(false)
                setUpdate(!update)
            }
            } userId={participantId} eventId={eventId}/> }
            {isEditing && <EditForm onClose={() => {
                setIsLoading(false)
                setIsEditing(false)
                setUpdate(!update)
            }} event={event} />}
            {isUserProfileOpen &&
                <UserProfile
                    onClose={() => {
                        setIsUserProfileOpen(false)
                        setUpdate(!update)
                    }}
                    userId={participantId}
                    eventId={eventId}
                    isOrganizer={isOrganizer}
                />
            }
            {error && <Error message={error} onClose={() => setError(null)} />}
        </>
    );
}