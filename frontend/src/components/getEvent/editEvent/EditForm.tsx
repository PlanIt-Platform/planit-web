import React, {useEffect, useState} from "react";
import {editEvent, getCategories, getSubCategories} from "../../../services/eventsServices";
import Error from "../../error/Error";
import Loading from "../../loading/Loading";

export default function EventForm({ event, onClose }) {
    const [inputs, setInputs] =useState({
        title: event.title,
        description: event.description,
        category: event.category,
        subCategory: event.subCategory,
        location: event.location,
        visibility: event.visibility,
        date: formatDateTime(event.date),
        endDate: formatDateTime(event.endDate) || "",
        priceAmount: event.priceAmount,
        priceCurrency: event.priceCurrency,
        password: event.password
    });
    const [categories, setCategories] = useState([]);
    const [subCategories, setSubCategories] = useState([]);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState("");
    const [isLoading, setIsLoading] = useState(true)
    const today = new Date().toISOString().split('T')[0]

    useEffect(() => {
        setIsLoading(true)
        getCategories()
            .then((res) => {
                if (res.data.error) setError(res.data.error);
                else setCategories(res.data);
                setIsLoading(false)
            });
    }, []);

    useEffect(() => {
        if (inputs.category) {
            const category = inputs.category.replace(/\s+/g, '-')
            setIsLoading(true)
            getSubCategories(category)
                .then((res) => {
                    if (res.data.error) setError(res.data.error);
                    else {
                        setSubCategories(res.data);
                        const defaultSubCategory = res.data[0];
                        setInputs(inputs => ({...inputs, subCategory: defaultSubCategory}));
                    }
                    setIsLoading(false)
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
        setIsLoading(true)
        editEvent(event.id, inputs)
            .then(res => {
                if (res.data.error) setError(res.data.error);
                else onClose();
                setSubmitting(false);
                setIsLoading(false)
            });
    }

    function handleChange(ev) {
        const { name, value } = ev.currentTarget;
        setInputs(prevInputs => ({
            ...prevInputs,
            [name]: name === "date" || name === "endDate" ? formatDateTime(value) : value
        }));
    }

    function handleKeyPress(event) {
        const keyCode = event.keyCode || event.which;
        const keyValue = String.fromCharCode(keyCode);
        if (!/\d/.test(keyValue) && keyValue !== "." && event.key !== "Backspace")
            event.preventDefault();
    }

    if (isLoading) return <Loading onClose={() => setIsLoading(false)} />

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
                    <select name="subCategory" value={inputs.subCategory} onChange={handleChange}>
                        <option value="disabled" disabled>Select Sub-Category</option>
                        <option value="">None</option>
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
                    <label>
                        Price*:
                        <input type="number" name="price" value={inputs.priceAmount} onChange={handleChange}
                               onKeyDown={handleKeyPress} step="0.01" min="0" required/>
                        <input type={"text"} name={"currency"} value={inputs.priceCurrency} onChange={handleChange} required/>
                    </label>
                    <button type="submit" disabled={submitting} className="event-form-button">Edit</button>
                    {error && <Error message={error} onClose={() => setError(null)} />}
                </form>
            </div>
        </>
    );
}
