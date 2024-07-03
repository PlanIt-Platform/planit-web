import React, {useState} from "react";
import {editEvent} from "../../../services/eventsServices";
import Loading from "../../shared/loading/Loading";
import {formatDate} from "../shared/formatDate";
import EventFormFormat from "../shared/EventFormFormat";

export default function EventForm({ event, onClose }) {
    const [inputs, setInputs] =useState({
        title: event.title,
        description: event.description,
        category: event.category,
        subCategory: event.subCategory,
        address: event.address,
        latitude: event.latitude,
        longitude: event.longitude,
        visibility: event.visibility,
        date: formatDate(event.date),
        endDate: formatDate(event.endDate) || "",
        priceAmount: event.priceAmount,
        priceCurrency: event.priceCurrency,
        password: event.password
    })
    const [error, setError] = useState('')
    const [isLoading, setIsLoading] = useState(true)

    function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault();
        setIsLoading(true)
        editEvent(event.id, inputs)
            .then(res => {
                if (res.data.error) setError(res.data.error);
                else onClose();
                setIsLoading(false)
            });
    }

    function handleChange(ev) {
        const { name, value } = ev.currentTarget;
        setInputs(prevInputs => ({
            ...prevInputs,
            [name]: name === "date" || name === "endDate" ? formatDate(value, "hourMinute") : value
        }));
    }

    if (isLoading) return <Loading/>

    return (
        <EventFormFormat
            inputs={inputs}
            setInputs={setInputs}
            handleSubmit={handleSubmit}
            handleChange={handleChange}
            onClose={onClose}
            setIsLoading={setIsLoading}
            isLoading={isLoading}
            setError={setError}
            error={error}
        />
    );
}
