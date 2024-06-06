import React, {useEffect, useState} from 'react';
import {editUser, getUser} from "../../services/usersServices";
import {getUserId} from "../authentication/Session";
import './AccountDetails.css';
import {getCategories} from "../../services/eventsServices";
import Error from "../error/Error";
import Loading from "../loading/Loading";

const capitalizeFirstLetter = (string) => {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

export default function AccountDetails(): React.ReactElement {
    const userId = getUserId();
    const [userData, setUserData] = useState<any>({
        name: '',
        description: '',
        interests: []
    });
    const [error, setError] = useState('')
    const [isLoading, setIsLoading] = useState(true)
    const [isEditing, setIsEditing] = useState(false);
    const [interests, setInterests] = useState([]);

    useEffect(() => {
        setIsLoading(true)
        getUser(userId)
            .then((res) => {
                if (res.data.error) {
                    setError(res.data.error)
                    setIsLoading(false)
                    return
                }
                setUserData(res.data);
                getCategories()
                    .then((res) => {
                        if (res.data.error) setError(res.data.error);
                        else {
                            const filteredInterests = res.data.filter(interest => interest !== 'Simple Meeting');
                            setInterests(filteredInterests);
                        }
                        setIsLoading(false)
                    })
            })
    }, [])

    const handleEdit = () => {
        setIsEditing(true);
    }

    const handleSave = () => {
        setIsLoading(true)
        editUser(userData.name, userData.description, userData.interests)
            .then((res) => {
                if (res.data.error) setError(res.data.error)
                else setIsEditing(false);
                setIsLoading(false)
            })
    }

    const handleChange = (event) => {
        setUserData({...userData, [event.target.name]: event.target.value});
    }

    const handleInterestChange = (event) => {
        if (event.target.checked) {
            setUserData({...userData, interests: [...userData.interests, event.target.value]});
        } else {
            setUserData({...userData, interests: userData.interests.filter(interest => interest !== event.target.value)});
        }
    }

    if (isLoading) return <Loading onClose={() => setIsLoading(false)} />

    return (
        <div className={"divStyle"}>
            <p className={"titleStyle"}>Account details</p>
            <div className="detail">
                <p className="attribute">Username:</p>
                <p className="value">{userData.username}</p>
            </div>
            <hr />
            <div className="detail">
                <p className="attribute">Email:</p>
                <p className="value">{userData.email}</p>
            </div>
            <hr />
            <div className="detail">
                <p className="attribute">Name:</p>
                {isEditing ? (
                    <input type="text" name="name" value={userData.name} onChange={handleChange} />
                ) : (
                    <p className="value">{userData.name}</p>
                )}
            </div>
            <hr />
            <div className="detail">
                <p className="attribute">Description:</p>
                {isEditing ? (
                    <input type="text" name="description" value={userData.description} onChange={handleChange} />
                ) : (
                    <p className="value">{userData.description}</p>
                )}
            </div>
            <hr />
            <div className="detail">
                <p className="attribute">Interests:</p>
                {isEditing ? (
                    interests.map(interest => (
                        <div key={interest}>
                            <input
                                type="checkbox"
                                id={interest}
                                value={interest}
                                checked={userData.interests.includes(interest)}
                                onChange={handleInterestChange}
                            />
                            <label htmlFor={interest}>{interest}</label>
                        </div>
                    ))
                ) : (
                    <p className="value">{(userData.interests || []).map(capitalizeFirstLetter).join(", ")}</p>
                )}
            </div>
            <hr />
            {isEditing ? (
                <button onClick={handleSave}>Save</button>
            ) : (
                <button onClick={handleEdit}>Edit</button>
            )}
            {error && <Error message={error} onClose={() => setError(null)} />}
        </div>
    )
}