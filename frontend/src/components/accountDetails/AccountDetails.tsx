import React, {useEffect, useState} from 'react';
import {editUser, getUser} from "../../services/usersServices";
import {getUserId} from "../authentication/Session";
import './AccountDetails.css';
import Error from "../shared/error/Error";
import Loading from "../shared/loading/Loading";

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
    const [newInterest, setNewInterest] = useState('');

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
               setIsLoading(false)
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

    if (isLoading) return <Loading/>

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
                    <p className="value">{userData.description || "No description available"}</p>
                )}
            </div>
            <hr />
            <div className="detail">
                <p className="attribute">Interests:</p>
                {isEditing ? (
                    <>
                        <input type="text" style={{paddingRight: "0px"}}
                               placeholder={"Enter an interest"}
                               value={newInterest} onChange={(ev) => setNewInterest(ev.target.value)} />
                        <button onClick={() => {
                            setUserData({...userData, interests: [...userData.interests, newInterest]});
                            setNewInterest('');
                            }}>Add Interest
                        </button>
                        <div className="interests-container">
                            {userData.interests.map((interest, index) => (
                                <div key={index} className="interest">
                                    {interest}
                                    <button onClick={() =>
                                        setUserData({
                                            ...userData,
                                            interests: userData.interests.filter((_, i) => i !== index)})
                                    }>X</button>
                                </div>
                            ))}
                        </div>
                    </>
                ) : (
                    <p className="value">{userData.interests.length > 0 ? userData.interests.join(" ") : 'No interests available'}</p>
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