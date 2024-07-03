import pencilImage from "../../../../../images/pencil.png";
import description from "../../../../../images/description.png";
import location from "../../../../../images/location.png";
import {GoogleMap, LoadScript, Marker} from "@react-google-maps/api";
import arrow from "../../../../../images/arrow.png";
import date from "../../../../../images/date.png";
import price from "../../../../../images/price.png";
import lock from "../../../../../images/lock.png";
import React, {useState} from "react";
import {formatDate} from "../../shared/formatDate";
import {deleteEvent, leaveEvent} from "../../../../services/eventsServices";
import {collection, deleteDoc, doc, getDocs} from "firebase/firestore";
import {db} from "../../../../Router";

export function EventInformation(
    {
        event,
        eventId,
        isOrganizer,
        isInEvent,
        numberOfOrganizers,
        setIsEditing,
        setIsLoading,
        setError,
        setRedirectHome
    }) {
    const [isMapOpen, setIsMapOpen] = useState(false);


    const handleDelete = () => {
        setIsLoading(true)
        deleteEvent(eventId)
            .then(async (res) => {
                if (res.data.error) setError(res.data.error)
                else {
                    // Get a reference to the chat collection
                    const chatCollection = collection(db, `events/${eventId}/messages`);

                    // Get all documents in the collection
                    const chatSnapshot = await getDocs(chatCollection);

                    // Delete each document
                    chatSnapshot.forEach((docSnapshot) => {
                        deleteDoc(doc(db, `events/${eventId}/messages`, docSnapshot.id));
                    });

                    setRedirectHome(true)
                }
                setIsLoading(false)
            })
    }

    const handleLeave = () => {
        setIsLoading(true)
        leaveEvent(eventId)
            .then((res) => {
                if (res.data.error) setError(res.data.error)
                else setRedirectHome(true)
                setIsLoading(false)
            })
    }

    return (
        <div className="left">
            <h2>Information</h2>
            {isOrganizer && <img src={pencilImage} alt="Edit" className={"pencil_img"} onClick={() => setIsEditing(true)} />}
            {event.description &&
                <div className="info-pair">
                    <img src={description} alt="description" className={"info_img"} style={{width: "25px"}}/>
                    <span className="event-description" title={event.description}>{event.description}</span>
                </div>
            }
            {event.address &&
                <>
                    <div className="info-pair" onClick={() => {
                        setIsMapOpen(!isMapOpen)
                    }}>
                        <img src={location} alt="location" className={"info_img"}/>
                        <span className="info-value" title={event.address}>{event.address}</span>
                    </div>
                    {isMapOpen && (
                        <LoadScript googleMapsApiKey={'AIzaSyAWfweHlUl5oUcBz4qZiVm1H5jlvSJXg3E'}>
                            <div style={{width: '70%', height: '20vh', marginLeft: "50px"}}>
                                <GoogleMap
                                    mapContainerStyle={{ width: '100%', height: '100%' }}
                                    zoom={8}
                                    center={ { lat: event.latitude, lng: event.longitude } }
                                    options={{disableDefaultUI: true, clickableIcons: false, keyboardShortcuts: false}}
                                >
                                    {event && <Marker position={{lat: event.latitude, lng: event.longitude}}/>}
                                </GoogleMap>
                            </div>
                        </LoadScript>
                    )}
                </>
            }
            <div className={"date-container"}>
                {event.endDate && <img src={arrow} alt="arrow" className="arrow"/>}
                <div className={event.endDate ? "date-pairs-endDate" : "date-pairs"}>
                    {event.date &&
                        <div className="info-pair">
                            <img src={date} alt="date" className={"info_img"}/>
                            <span className="info-value" title={event.date}>{formatDate(event.date)}</span>
                        </div>
                    }
                    {event.endDate &&
                        <div className="info-pair">
                            <img src={date} alt="date" className={"info_img"}/>
                            <span className="info-value" title={event.endDate}>{formatDate(event.endDate)}</span>
                        </div>
                    }
                </div>
            </div>
            {event.priceCurrency &&
                <div className="info-pair">
                    <img src={price} alt="price" className={"info_img"}/>
                    <span className="info-value">{event.priceAmount} {event.priceCurrency}</span>
                </div>
            }
            {event.code && isOrganizer &&
                <div className="info-pair">
                    <img src={lock} alt="lock" className={"info_img"}/>
                    <span className="info-value">{event.code}</span>
                </div>
            }
            <div className={"buttons-container"}>
                {isOrganizer && <button className="del-button" onClick={handleDelete}>Delete</button>}
                {isInEvent && (!isOrganizer || (isOrganizer && numberOfOrganizers > 1)) && (
                    <button className="leave-button" onClick={handleLeave}>Leave</button>
                )}
            </div>
        </div>
    )
}