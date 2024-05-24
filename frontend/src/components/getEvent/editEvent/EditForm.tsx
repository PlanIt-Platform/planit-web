import React, {useEffect, useState} from "react";
import {editEvent, getCategories, getSubCategories} from "../../../services/eventsServices";


export default function EventForm({ event, onClose }) {
    const [inputs, setInputs] =useState({
        title: event.title,
        description: event.description,
        category: event.category,
        subCategory: event.subCategory,
        location: event.location,
        visibility: event.visibility,
        date: formatDateTime(event.date),
        endDate: formatDateTime(event.endDate),
        priceAmount: event.priceAmount,
        priceCurrency: event.priceCurrency,
        password: event.password
    });
    const [categories, setCategories] = useState([]);
    const [subCategories, setSubCategories] = useState([]);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState("");
    const today = new Date().toISOString().split('T')[0];

    useEffect(() => {
        getCategories().then((res) => {
            if (res.data.error) {
                setError(res.data.error);
                return;
            }
            setCategories(res.data);
        });
    }, []);

    useEffect(() => {
        if (inputs.category) {
            getSubCategories(inputs.category)
                .then((res) => {
                    if (res.data.error) {
                        setError(res.data.error);
                        return;
                    }
                    setSubCategories(res.data);
                    const defaultSubCategory = res.data[0];
                    setInputs(inputs => ({ ...inputs, subCategory: defaultSubCategory }));
                });
        }
    }, [inputs.category]);

    function formatDateTime(datetime) {
        if (!datetime) return;
        const [datePart, timePart] = datetime.includes('T') ? datetime.split('T') : datetime.split(' ');
        const [hour, minute] = timePart.split(':');
        return `${datePart} ${hour}:${minute}`;
    }

    function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault();
        setSubmitting(true);
        editEvent(event.id, inputs)
            .then(res => {
                if (res.data.error) {
                    setError(res.data.error);
                    setSubmitting(false);
                    return;
                }
                onClose();
            });
    }

    function handleChange(ev: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) {
        const name = ev.currentTarget.name;
        const value = ev.currentTarget.value;
        if (name == "date" || name == "endDate") {
            const formattedValue = formatDateTime(value);
            setInputs({...inputs, [name]: formattedValue});
        }
        else {
            setInputs({...inputs, [name]: value})
        }
    }


    return (
        <>
            <div className={"overlay"} onClick={onClose}></div>
            <div className="event-form">
                <h2>Edit Event</h2>
                <form onSubmit={handleSubmit}>
                    <input type="text" name="title" value={inputs.title} onChange={handleChange} placeholder="Title*" required />
                    <textarea name="description" value={inputs.description} onChange={handleChange} placeholder="Description"/>
                    <select name="category" value={inputs.category} onChange={handleChange} required>
                        <option value="" disabled>Select Category*</option>
                        {categories.map(category => <option key={category} value={category}>{category}</option>)}
                    </select>
                    <select name="subCategory" value={inputs.subCategory} onChange={handleChange} required>
                        <option value="" disabled>Select Sub-Category*</option>
                        {subCategories.map(subCategory => <option key={subCategory} value={subCategory}>{subCategory}</option>)}
                    </select>
                    <input type="text" name="location" value={inputs.location} onChange={handleChange} placeholder="Location"/>
                    <select name="visibility" value={inputs.visibility} onChange={handleChange} required>
                        <option value="" disabled>Select Visibility*</option>
                        <option value="Public">Public</option>
                        <option value="Private">Private</option>
                    </select>
                    <input type="datetime-local" name="date" value={inputs.date} min={today} onChange={handleChange} required />
                    <input type="datetime-local" name="endDate" value={inputs.endDate} min={today} onChange={handleChange}/>
                    <input type="number" name="priceAmount" value={inputs.priceAmount} onChange={handleChange} placeholder="Price" />
                    <button type="submit" disabled={submitting}>Edit</button>
                    {error && <p>{error}</p>}
                </form>
            </div>
        </>
    );
}
