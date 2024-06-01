import React, {useEffect, useState} from "react";
import './GetEvent.css';
import {deleteEvent, getEvent, getUsersInEvent, leaveEvent} from "../../services/eventsServices";
import {Navigate, useParams} from "react-router-dom";
import location from "../../../images/location.png";
import date from "../../../images/date.png";
import arrow from "../../../images/arrow.png";
import price from "../../../images/price.png";
import plus from "../../../images/plus.png";
import pencilImage from '../../../images/pencil.png';
import description from "../../../images/description.png";
import {getUserId} from "../authentication/Session";
import UserProfile from "../userProfile/UserProfile";
import EditForm from "./editEvent/EditForm";
import {AssignTask} from "./assignTask/AssignTask";
import {ChatRoom} from "../chat/chatRoom/ChatRoom";
import {collection, deleteDoc, getDocs, doc} from "firebase/firestore";
import {db} from "../../Router";

function formatDate(dateString) {
    // Remove the seconds from the date string
    const dateWithoutSeconds = dateString.slice(0, 16);

    const options: Intl.DateTimeFormatOptions = {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    };
    return new Date(dateWithoutSeconds).toLocaleDateString(undefined, options);
}


export default function GetEvent(): React.ReactElement {
    const userId = getUserId();
    const eventId = useParams().id
    const [isOrganizer, setIsOrganizer] = useState(false);
    const [numberOfOrganizers, setNumberOfOrganizers] = useState(1);
    const [isInEvent, setIsInEvent] = useState(false);
    const [participants, setParticipants] = useState([])
    const [error, setError] = useState('')
    const [isLoading, setIsLoading] = useState(true);
    const [redirectHome, setRedirectHome] = useState(false);
    const [isUserProfileOpen, setIsUserProfileOpen] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const [isAssigningTask, setIsAssigningTask] = useState(false);
    const [participantId, setParticipantId] = useState(0);
    const [update, setUpdate] = useState(false);
    const [event, setEvent] = useState({
        title: "",
        description: "",
        category: "",
        subCategory: "",
        location: "",
        visibility: "",
        date: "",
        endDate: "",
        priceAmount: "",
        priceCurrency: "",
        password: ""
        })

    useEffect(() => {
        getEvent(eventId)
            .then((res) => {
                if (res.data.error) {
                    setError(res.data.error)
                    return
                } else {
                    setEvent(res.data);
                    setIsLoading(false);
                }
            });

        getUsersInEvent(eventId)
            .then((res) => {
                if (res.data.error) {
                    setError(res.data.error)
                    return
                } else {
                    const sortedUsers = res.data.users.sort((a, b) => {
                        if (a.taskName === "Organizer") return -1;
                        if (b.taskName === "Organizer") return 1;
                        return 0;
                    })
                    setParticipants(sortedUsers);
                    setIsInEvent(res.data.users.some((user) => user.id === userId))
                    setIsOrganizer(res.data.users.some((user) => user.id === userId && user.taskName === "Organizer"));
                    setNumberOfOrganizers(res.data.users.filter((user) => user.taskName === "Organizer").length);

                    if (!res.data.users.some(participant => participant.id === userId)) {
                        setRedirectHome(true);
                    }
                }
            });


    }, [eventId, update]);

    const handleDelete = () => {
        deleteEvent(eventId)
            .then(async (res) => {
                if (res.data.error) {
                    setError(res.data.error)
                    return
                }

                // Get a reference to the chat collection
                const chatCollection = collection(db, `events/${eventId}/messages`);

                // Get all documents in the collection
                const chatSnapshot = await getDocs(chatCollection);

                // Delete each document
                chatSnapshot.forEach((docSnapshot) => {
                    deleteDoc(doc(db, `events/${eventId}/messages`, docSnapshot.id));
                });

                setRedirectHome(true)
            })
    }

    const handleLeave = () => {
       leaveEvent(eventId)
            .then((res) => {
                if (res.data.error) {
                    setError(res.data.error)
                    return
                }
                setRedirectHome(true)
            })
    }

    if (redirectHome) return <Navigate to="/planit/events" replace={true}/>;

    if (isLoading) {
        return <div>Loading...</div>;
    }

    return (
        <>
            <div className="event-title-container">
                <h1 className="event-title" title={event.title}>{event.title}</h1>
            </div>
            <div className="container">
                <div className="left">
                    <h2>Information</h2>
                    {isOrganizer && <img src={pencilImage} alt="Edit" className={"pencil_img"} onClick={() => setIsEditing(true)} />}
                    {event.description &&
                        <div className="info-pair">
                            <img src={description} alt="description" className={"info_img"} style={{width: "25px"}}/>
                            <span className="event-description" title={event.description}>{event.description}</span>
                        </div>
                    }
                    {event.location &&
                        <div className="info-pair">
                            <img src={location} alt="location" className={"info_img"}/>
                            <span className="info-value" title={event.location}>{event.location}</span>
                        </div>
                    }
                    <div className={"date-container"}>
                        {event.endDate && <img src={arrow} alt="arrow" className="arrow"/>}
                        <div className={event.endDate ? "date-pairs-endDate" : "date-pairs"}>
                            <div className="info-pair">
                                <img src={date} alt="date" className={"info_img"}/>
                                <span className="info-value" title={event.date}>{formatDate(event.date)}</span>
                            </div>
                            {event.endDate &&
                                <div className="info-pair">
                                    <img src={date} alt="date" className={"info_img"}/>
                                    <span className="info-value" title={event.endDate}>{formatDate(event.endDate)}</span>
                                </div>
                            }
                        </div>
                    </div>
                    <div className="info-pair">
                        <img src={price} alt="price" className={"info_img"}/>
                        <span className="info-value">{event.priceAmount} {event.priceCurrency}</span>
                    </div>
                    <div className={"buttons-container"}>
                        {isInEvent && (!isOrganizer || (isOrganizer && numberOfOrganizers > 1)) && (
                            <button className="leave-button" onClick={handleLeave}>Leave</button>
                        )
                        }
                        {isOrganizer && <button className="del-button" onClick={handleDelete}>Delete</button>}
                    </div>
                </div>
                <div className="middle">
                    <ChatRoom uId={userId} eventId={eventId} isOrganizer={isOrganizer}/>
                </div>
                <div className="right">
                    <h2>Participants ({participants.length})</h2>
                    <ul>
                        {
                            participants.map((participant: any) => {
                            return (
                                <div className="participant-container" key={participant.id}
                                     onClick={
                                         () => {
                                             setIsUserProfileOpen(true)
                                             setParticipantId(participant.id)
                                         }
                                     }>
                                    <li title={participant.username} className={`participant-item ${participant.taskName === 'Organizer' 
                                        ? 'organizerName' : ''}`}>{participant.username}</li>
                                    {
                                        participant.taskName
                                            ? <li title={participant.taskName} className={`participant-task 
                                                ${participant.taskName === 'Organizer' 
                                                    ? 'organizer' : ''}`}>{participant.taskName}</li>
                                            : isOrganizer && <img src={plus} alt="Assign" className={"plus_img"}
                                                   onClick={(event) => {
                                                       event.stopPropagation();
                                                       setIsAssigningTask(true)
                                                       setParticipantId(participant.id)
                                                   }}/>
                                    }
                                </div>
                            )})
                        }
                    </ul>
                </div>
            </div>
            {isAssigningTask && <AssignTask onClose={() => {
                setIsAssigningTask(false)
                setUpdate(!update)
            }
            } userId={participantId} eventId={eventId}/> }
            {isEditing && <EditForm onClose={() => {
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
        </>
    );
}