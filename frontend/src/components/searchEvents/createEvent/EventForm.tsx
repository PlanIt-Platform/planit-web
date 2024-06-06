import React, {useEffect, useState} from "react";
import {createEvent, getCategories, getSubCategories} from "../../../services/eventsServices";
import './EventForm.css';
import Error from "../../error/Error";
import Loading from "../../loading/Loading";

export default function EventForm({onClose}) {
    const [inputs, setInputs] = useState(
        {title: "",
            description: "",
            category: "Technology",
            subCategory: "Web Development",
            location: "",
            visibility: "Public",
            date: "",
            endDate: "",
            price: "0",
            currency: "Eur",
            password: ""
        })
    const [categories, setCategories] = useState([]);
    const [subCategories, setSubCategories] = useState([]);
    const [error, setError] = useState('')
    const [isLoading, setIsLoading] = useState(true)
    const today = new Date().toISOString().split('T')[0];

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
        const [datePart, timePart] = datetime.split('T');
        return datePart + ' ' + timePart;
    }

    function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault()
        setIsLoading(true)
        createEvent(inputs)
            .then(res => {
                if (res.data.error) setError(res.data.error)
                else {
                    setError("")
                    onClose()
                }
                setIsLoading(false)
            })
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
                <form onSubmit={handleSubmit}>
                    <label>
                        Title*:
                        <input type="text" name="title" value={inputs.title} onChange={handleChange} required/>
                    </label>
                    <label>
                        Description:
                        <textarea
                            name="description"
                            value={inputs.description}
                            onChange={handleChange}
                            placeholder="Add some information about your event..."
                        />
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
                        <select name="subCategory" value={inputs.subCategory} onChange={handleChange}>
                            <option value="">None</option>
                            {subCategories.map(subCategory => (
                                <option key={subCategory} value={subCategory}>{subCategory}</option>
                            ))}
                        </select>
                    </label>
                    <label>
                        Location:
                        <input type="text" name="location" value={inputs.location} onChange={handleChange} />
                    </label>
                    <label>
                        Visibility*:
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
                        <input type="datetime-local" name="date" value={inputs.date} min={today} onChange={handleChange} required/>
                    </label>
                    <label>
                        End Date:
                        <input type="datetime-local" name="endDate" value={inputs.endDate} min={inputs.date} onChange={handleChange}/>
                    </label>
                    <label>
                        Price*:
                        <input type="number" name="price" value={inputs.price} onChange={handleChange}
                               onKeyDown={handleKeyPress} step="0.01" min="0" required/>
                        <input type={"text"} name={"currency"} value={inputs.currency} onChange={handleChange} required/>
                    </label>
                    <button type="submit" className="event-form-button">Create Event</button>
                    <button type="button" onClick={onClose} className="event-form-button">Cancel</button>
                </form>
                {error && <Error message={error} onClose={() => setError(null)} />}
            </div>
        </>
    );
}