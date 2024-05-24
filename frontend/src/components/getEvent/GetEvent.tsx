import React, {useEffect, useState} from "react";
import './GetEvent.css';
import {deleteEvent, editEvent, getEvent, getUsersInEvent, leaveEvent} from "../../services/eventsServices";
import {Navigate, useParams} from "react-router-dom";
import location from "../../../images/location.png";
import date from "../../../images/date.png";
import arrow from "../../../images/arrow.png";
import price from "../../../images/price.png";
import pencilImage from '../../../images/pencil.png';
import description from "../../../images/description.png";
import {getUserId} from "../authentication/Session";
import UserProfile from "../userProfile/UserProfile";
import EditForm from "./editEvent/EditForm";

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
    const [isOrganizer, setIsOrganizer] = useState(false);
    const [isInEvent, setIsInEvent] = useState(false);
    const [participants, setParticipants] = useState([])
    const [error, setError] = useState('')
    const [isLoading, setIsLoading] = useState(true);
    const [redirectHome, setRedirectHome] = useState(false);
    const [isUserProfileOpen, setIsUserProfileOpen] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const eventId = useParams().id
    const [event, setEvent] = useState(
        {title: "",
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
                    setParticipants(res.data.users);
                    setIsInEvent(res.data.users.some((user) => user.id === userId))
                    setIsOrganizer(res.data.users.some((user) => user.id === userId && user.taskName === "Organizer"));
                }
            });


    }, [eventId, event]);

    const handleDelete = () => {
        deleteEvent(eventId)
            .then((res) => {
                if (res.data.error) {
                    setError(res.data.error)
                    return
                }
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
        <div>
            <div className="event-title">
                <h1>{event.title}</h1>
            </div>
            <div className="container">
                <div className="left">
                    <h2>Information</h2>
                    {isOrganizer && <img src={pencilImage} alt="Edit" className={"pencil_img"} onClick={() => setIsEditing(true)} />}
                    {event.description &&
                        <div className="info-pair">
                            <img src={description} alt="description" className={"info_img"} style={{width: "25px"}}/>
                            <span className="info-value">{event.description}</span>
                        </div>
                    }
                    {event.location &&
                        <div className="info-pair">
                            <img src={location} alt="location" className={"info_img"}/>
                            <span className="info-value">{event.location}</span>
                        </div>
                    }
                    <div className={"date-container"}>
                        {event.endDate && <img src={arrow} alt="arrow" className="arrow"/>}
                        <div className={event.endDate ? "date-pairs-endDate" : "date-pairs"}>
                            <div className="info-pair">
                                <img src={date} alt="date" className={"info_img"}/>
                                <span className="info-value">{formatDate(event.date)}</span>
                            </div>
                            {event.endDate &&
                                    <div className="info-pair">
                                        <img src={date} alt="date" className={"info_img"}/>
                                        <span className="info-value">{formatDate(event.endDate)}</span>
                                    </div>
                            }
                        </div>
                    </div>
                    <div className="info-pair">
                        <img src={price} alt="price" className={"info_img"}/>
                        <span className="info-value">{event.priceAmount} {event.priceCurrency}</span>
                    </div>
                    <div className={"buttons-container"}>
                        {isInEvent && !isOrganizer && <button className="del-button" onClick={handleLeave}>Leave</button>}
                        {isOrganizer && <button className="del-button" onClick={handleDelete}>Delete</button>}
                    </div>
                </div>
                <div className="middle">
                    <div className="chat-container">
                        <div className="chat-input">
                            <input type="text" placeholder="Type a message..."/>
                            <button>Send</button>
                        </div>
                    </div>
                </div>
                <div className="right">
                    <h2>Participants ({participants.length})</h2>
                    <ul>
                        {
                            participants.map((participant: any) => {
                            return (
                                <li key={participant.id} className="participant-item" onClick={() => setIsUserProfileOpen(true)}>
                                        {participant.username}
                                </li>
                            )})
                        }
                    </ul>
                </div>
            </div>
            {isEditing && <EditForm onClose={() => setIsEditing(false)} event={event} />}
            {isUserProfileOpen && <UserProfile onClose={() => setIsUserProfileOpen(false)} />}
        </div>
    );
}