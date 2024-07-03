import React, {useEffect, useState} from 'react';
import {GoogleMap, Marker, useJsApiLoader} from "@react-google-maps/api";
import Error from "../../shared/error/Error";
import Loading from "../../shared/loading/Loading";
import {getCategories, getSubCategories} from "../../../services/eventsServices";

const googleMapsApiKey = "AIzaSyAWfweHlUl5oUcBz4qZiVm1H5jlvSJXg3E"

export default function EventFormFormat(
    {
        inputs,
        setInputs,
        handleSubmit,
        handleChange,
        onClose,
        setIsLoading,
        isLoading,
        setError,
        error,
        isEditing
    }) {
    const [categories, setCategories] = useState([]);
    const [subCategories, setSubCategories] = useState([]);
    const today = new Date().toISOString().split('T')[0];
    const [paymentType, setPaymentType] = useState('Free');
    const [markerPosition, setMarkerPosition] = useState({
        lat: 38.736946, lng: -9.142685
    });
    const { isLoaded } = useJsApiLoader({
        googleMapsApiKey: googleMapsApiKey
    });

    useEffect(() => {
        console.log("HERE")
        setIsLoading(true)
        getCategories()
            .then((res) => {
                if (res.data.error) setError(res.data.error);
                else setCategories(res.data);
            });
    }, []);

    useEffect(() => {
        console.log("second useffect")
        console.log(inputs.category)
        if (inputs.category) {
            const category = inputs.category.replace(/\s+/g, '-')
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

    function handleKeyPress(event) {
        const keyCode = event.keyCode || event.which;
        const keyValue = String.fromCharCode(keyCode);
        if (!/\d/.test(keyValue) && keyValue !== "." && event.key !== "Backspace")
            event.preventDefault();
    }

    function convertAddress(ev) {
        setInputs(prevInputs => ({
            ...prevInputs,
            location: ev.target.value
        }))
        const address = ev.target.value
        const geocoder = `https://maps.googleapis.com/maps/api/geocode/json?address=${address}&key=AIzaSyAWfweHlUl5oUcBz4qZiVm1H5jlvSJXg3E`
        fetch(geocoder).then(res => res.json()).then(data => {
            if (data.status === 'OK') {
                const location = data.results[0].geometry.location
                setMarkerPosition(location)
                setInputs(prevInputs => ({
                    ...prevInputs,
                    latitude: location.lat,
                    longitude: location.lng
                }))
            }
        })
    }

    function convertCoordinates(latLng) {
        const geocoder = `https://maps.googleapis.com/maps/api/geocode/json?latlng=${latLng.lat},${latLng.lng}&key=AIzaSyAWfweHlUl5oUcBz4qZiVm1H5jlvSJXg3E`
        fetch(geocoder).then(res => res.json()).then(data => {
            if (data.status === 'OK') {
                const location = data.results[0].formatted_address
                setInputs(prevInputs => ({
                    ...prevInputs,
                    location: location,
                    latitude: latLng.lat,
                    longitude: latLng.lng
                }))
            }
        })
    }

    if (isLoading) return <Loading />;

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
                    {inputs.category != "Simple Meeting" &&  (<label>
                            Subcategory:
                            <select name="subCategory" value={inputs.subCategory} onChange={handleChange}>
                                <option value="">None</option>
                                {subCategories.map(subCategory => (
                                    <option key={subCategory} value={subCategory}>{subCategory}</option>
                                ))}
                            </select>
                        </label>
                    )}
                    <label>
                        Location:
                        <select name="locationType" onChange={(ev) => {
                            setInputs(prevInputs => ({
                                ...prevInputs,
                                locationType: ev.target.value,
                                location: ""
                            }))
                        }}>
                            <option value="Physical">In-person</option>
                            <option value="Online">Remote</option>
                        </select>
                        <input type="text" name="location" value={inputs.location}
                               onChange={inputs.locationType === "Physical" ? convertAddress : handleChange}
                               placeholder={inputs.locationType === "Physical" ? "Type an address" : "Add a link (optional)"}/>
                    </label>
                    {inputs.locationType === 'Physical' && isLoaded && (
                        <div style={{ width: '40%', height: '30vh' }}>
                            <GoogleMap
                                mapContainerStyle={{ width: '100%', height: '100%' }}
                                zoom={8}
                                center={{ lat: markerPosition.lat, lng: markerPosition.lng }}
                                options={{ disableDefaultUI: true, clickableIcons: false, keyboardShortcuts: false }}
                                onClick={(ev) => {
                                    const latLng = { lat: ev.latLng.lat(), lng: ev.latLng.lng() };
                                    setMarkerPosition(latLng);
                                    convertCoordinates(latLng);
                                }}
                            >
                                {markerPosition &&
                                    <Marker
                                        position={{ lat: markerPosition.lat, lng: markerPosition.lng }}
                                    />
                                }
                            </GoogleMap>
                        </div>
                    )}
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
                        Payment Type:
                        <input type="radio" name="paymentType" value="Free" checked={paymentType === 'Free'} onChange={(ev) => setPaymentType(ev.target.value)} /> Free
                        <input type="radio" name="paymentType" value="Paid" checked={paymentType === 'Paid'} onChange={(ev) => setPaymentType(ev.target.value)} /> Paid
                    </label>
                    {paymentType === 'Paid' && (
                        <label>
                            Price*:
                            <input type="number" name="price" value={inputs.price} onChange={handleChange}
                                   onKeyDown={handleKeyPress} step="0.01" min="0.01" required/>
                            <input type={"text"} name={"currency"} value={inputs.currency} onChange={handleChange} required/>
                        </label>
                    )}
                    <button type="submit" className="event-form-button">
                        {isEditing ? 'Edit' : 'Create'}
                    </button>
                    <button type="button" onClick={onClose} className="event-form-button">Cancel</button>
                </form>
                {error && <Error message={error} onClose={() => setError(null)} />}
            </div>
        </>
    );
}
