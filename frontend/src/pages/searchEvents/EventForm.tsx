import React, {useEffect, useState} from "react";
import {createEvent, getCategories} from "../../services/eventsServices";
import './EventForm.css';

export default function EventForm({onClose}) {
    const [inputs, setInputs] = useState(
        {title: "",
            description: "",
            category: "Technology",
            subCategory: "",
            location: "",
            visibility: "Public",
            date: "",
            endDate: "",
            price: "",
            password: ""
        })
    const [categories, setCategories] = useState([]);
    const [submitting, setSubmitting] = useState(false)
    const [error, setError] = useState('')

    useEffect(() => {
        getCategories().then((res) => {
            if (res.error) {
                setError(res.error);
            } else {
                setCategories(res);
            }
        });
    }, []);

    function formatDateTime(datetime) {
        const [datePart, timePart] = datetime.split('T');
        return datePart + ' ' + timePart;
    }

    function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault()
        setSubmitting(true)
        createEvent(inputs)
            .then(res => {
                if (res.error) {
                    setError(res.error)
                    setSubmitting(false)
                    return
                }
                setError("")
                setSubmitting(false)
                onClose()
            })
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

    function handleKeyPress(event) {
        const keyCode = event.keyCode || event.which;
        const keyValue = String.fromCharCode(keyCode);
        if (!/\d/.test(keyValue) && keyValue !== "." && event.key !== "Backspace")
            event.preventDefault();
    }

    return (
        <>
            <div className={"overlay"} onClick={onClose}></div>
            <div className="event-form">
                <form onSubmit={handleSubmit}>
                    <label>
                        Title*:
                        <input type="text" name="title" value={inputs.title} onChange={handleChange} required/>
                    </label>
                    <label>
                        Description:
                        <input type="text" name="description" value={inputs.description} onChange={handleChange} />
                    </label>
                    <label>
                        Category*:
                        <select name="category" value={inputs.category} onChange={handleChange} required>
                            {categories.map(category => (
                                <option key={category} value={category}>{category}</option>
                            ))}
                        </select>
                    </label>
                    <label>
                        Subcategory:
                        <input type="text" name="subCategory" value={inputs.subCategory} onChange={handleChange} />
                    </label>
                    <label>
                        Location:
                        <input type="text" name="location" value={inputs.location} onChange={handleChange} />
                    </label>
                    <label>
                        Visibility:
                        <select name="visibility" value={inputs.visibility} onChange={handleChange}>
                            <option value="Public">Public</option>
                            <option value="Private">Private</option>
                        </select>
                    </label>
                    {inputs.visibility === 'Private' && (
                        <label>
                            Password*:
                            <input type="password" name="password" value={inputs.password} onChange={handleChange} required/>
                        </label>
                    )}
                    <label>
                        Date*:
                        <input type="datetime-local" name="date" value={inputs.date} onChange={handleChange} required/>
                    </label>
                    <label>
                        End Date:
                        <input type="datetime-local" name="endDate" value={inputs.endDate} onChange={handleChange}/>
                    </label>
                    <label>
                        Price:
                        <input type="number" name="price" value={inputs.price} onChange={handleChange}
                               onKeyDown={handleKeyPress} step="0.01" min="0"/>
                        <span> €</span>
                    </label>
                    <button type="submit">Create Event</button>
                    <button type="button" onClick={onClose}>Cancel</button>
                </form>
                {error && <p>{error}</p>}
            </div>
        </>
    );
}