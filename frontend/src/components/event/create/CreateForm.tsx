import React, {useEffect, useState} from "react";
import {createEvent} from "../../../services/eventsServices";
import '../shared/EventForm.css';
import {GoogleCalendar} from "../../googleCalendar/GoogleCalendar";
import EventFormFormat from "../shared/EventFormFormat";
import {formatDate} from "../shared/formatDate";

export default function CreateForm({onClose}) {
    const [inputs, setInputs] = useState(
        {title: "",
            description: "",
            category: "Technology",
            subCategory: "Web Development",
            address: "",
            latitude: 0,
            longitude: 0,
            visibility: "Public",
            date: "",
            endDate: "",
            price: "0",
            currency: "Eur",
            password: ""
        })

    const [isGooglePopupOpen, setIsGooglePopupOpen] = useState(false)
    const [isSubmitting, setSubmitting] = useState(false)
    const [error, setError] = useState('')
    const [isLoading, setIsLoading] = useState(true)

    function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault()
        setIsLoading(true)
        createEvent(inputs)
            .then(res => {
                if (res.data.error) setError(res.data.error)
                else {
                    setError("")
                    setIsGooglePopupOpen(true)
                }
                setIsLoading(false)
            })
    }

    function handleChange(ev) {
        const { name, value } = ev.currentTarget;
        setInputs(prevInputs => ({
            ...prevInputs,
            [name]: name === "date" || name === "endDate" ? formatDate(value, "dateAndTime") : value
        }));
    }

    useEffect(() => {
        if (isSubmitting) {
            onClose();
        }
    }, [isSubmitting, onClose]);

    return (
        <>
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
            {isGooglePopupOpen && <GoogleCalendar mode="addEvent" onClose={() => {
                setIsGooglePopupOpen(false)
                setSubmitting(true)
            }} inputs={inputs} />}
        </>
    );
}