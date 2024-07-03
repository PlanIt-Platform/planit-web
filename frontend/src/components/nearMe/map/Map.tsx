import {Circle, GoogleMap, InfoWindow, LoadScript, Marker} from "@react-google-maps/api";
import React, {useState} from "react";


export function Map(
    {
        userLocation,
        selectedRadius,
        events
    }) {
    const [selectedEvent, setSelectedEvent] = useState(null);

    return (
        <div className={"near-middle"}>
            <h1>Find out what's around you!</h1>
            <LoadScript googleMapsApiKey={'AIzaSyAWfweHlUl5oUcBz4qZiVm1H5jlvSJXg3E'}>
                <div style={{
                    width: '75vh',
                    height: '75vh',
                    borderRadius: '50%',
                    border: '5px solid black',
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    overflow: 'hidden'
                }}>
                    {userLocation.lat != undefined &&
                        <>
                            <GoogleMap
                                mapContainerStyle={{ width: '100%', height: '100%' }}
                                center={{lat: userLocation.lat, lng: userLocation.lng}}
                                zoom={7}
                                options={{disableDefaultUI: true, clickableIcons: false, keyboardShortcuts: false}}
                            >
                                <Marker
                                    position={{lat: userLocation.lat, lng: userLocation.lng}}
                                    icon={{url: 'https://maps.google.com/mapfiles/ms/icons/blue-dot.png'}}
                                />
                                <Circle
                                    center={userLocation}
                                    radius={selectedRadius}
                                    options={{
                                        strokeColor: '#FF0000',
                                        strokeOpacity: 0.8,
                                        strokeWeight: 2,
                                        fillColor: '#FF0000',
                                        fillOpacity: 0.35,
                                    }}
                                />
                                {events && events.map((event) => (
                                    <>
                                        <Marker
                                            key={event.id}
                                            position={{lat: event.latitude, lng: event.longitude}}
                                            icon={{url: 'https://maps.google.com/mapfiles/ms/icons/red-dot.png'}}
                                            onClick={() => {
                                                setSelectedEvent(event)
                                            }}
                                        />
                                        {selectedEvent && selectedEvent.id === event.id &&
                                            <InfoWindow
                                                position={{lat: event.latitude, lng: event.longitude}}
                                                onCloseClick={() => setSelectedEvent(null)}
                                            >
                                                <div>
                                                    <h2 style={{color: 'black'}}>{event.title}</h2>
                                                    <p style={{color: 'darkslategray'}}>{event.location}</p>
                                                </div>
                                            </InfoWindow>
                                        }
                                    </>
                                ))}
                            </GoogleMap>
                        </>}
                </div>
            </LoadScript>
        </div>
    )
}