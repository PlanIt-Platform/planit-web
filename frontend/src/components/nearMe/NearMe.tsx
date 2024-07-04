import React, {useEffect, useState} from "react";
import Loading from "../shared/loading/Loading";
import './NearMe.css';
import Error from "../shared/error/Error";
import {SearchConfiguration} from "./searchConfiguration/SearchConfiguration";
import {Map} from "./map/Map";
import {EventsFound} from "./eventsFound/EventsFound";
import {findNearbyEvents} from "../../services/eventsServices";

export default function NearMe(): React.ReactElement {
    const [error, setError] = useState('')
    const [isLoading, setIsLoading] = useState(true)
    const [events, setEvents] = useState([])
    const [selectedRadius, setSelectedRadius] = useState(25000)
    const [numEvents, setNumEvents] = useState(20)
    const [userLocation, setUserLocation] = useState<{lng: number | undefined, lat: number | undefined}>({
        lat: undefined, lng: undefined
    })

    useEffect(() => {
        setIsLoading(true)
        navigator.geolocation.getCurrentPosition((position) => {
            setUserLocation({lat: position.coords.latitude, lng: position.coords.longitude})
            handleSearch({lat: position.coords.latitude, lng: position.coords.longitude})
        })
    }, [])

    function handleSearch(userLocation: { lng: number; lat: number }) {
        setIsLoading(true)
        findNearbyEvents(userLocation.lat, userLocation.lng, selectedRadius, numEvents)
            .then((res) => {
                if (res.data.error) setError(res.data.error);
                else {
                    setEvents(res.data.events)
                    setError('')
                }
                setIsLoading(false)
            })
    }

    if (isLoading) return <Loading/>;

    return (
        <div className={"near-div"}>
            <SearchConfiguration
                userLocation={userLocation}
                selectedRadius={selectedRadius}
                setSelectedRadius={setSelectedRadius}
                setNumEvents={setNumEvents}
                numEvents={numEvents}
                handleSearch={handleSearch}
            />
            <Map
               selectedRadius={selectedRadius}
               userLocation={userLocation}
               events={events}
           />
           <EventsFound events={events}/>
           {error && <Error message={error} onClose={() => setError(null)} />}
        </div>
    )
}