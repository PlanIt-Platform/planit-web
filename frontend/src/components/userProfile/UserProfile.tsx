import React, { useEffect, useState} from 'react';
import {getUser} from "../../services/usersServices";
import {getUserId} from "../authentication/Session";
import "./UserProfile.css"

export default function UserProfile({onClose}) {
    const [userData, setUserData] = useState<any>('');
    const [error, setError] = useState<string>('');

    const userId = getUserId();

    useEffect(() => {
        getUser(userId).then((res) => {
            if(res.data.error) {
                setError(res.data.error)
                return
            }
            setUserData(res.data);
        })
    }, [userId]);

    return (
        <>
        <div className={"overlay"} onClick={onClose}></div>
        <div className="card-container">
            <img className="round" src="https://randomuser.me/api/portraits/men/69.jpg" alt="user"/> {/*29*/}
            <h3 className="h3_userProfile">{userData?.name}</h3>
            <h5 className="h5_userProfile">@{userData?.username}</h5>
            {userData?.description ? (
                <p className="p_userProfile">{userData.description}</p>
            ) : (
                <p>No description available</p>
            )}
            <div className="profile-buttons">
                <button className="primary">
                    Message
                </button>
                <button className="primary ghost">
                    Following
                </button>
            </div>
            <div className="skills">
                <h6>Interests</h6>
                {userData?.interests && userData.interests.length > 0 ? (
                    <ul>
                        {userData.interests.map((interest: string, index: number) => (
                            <li key={index}>{interest}</li>
                        ))}
                    </ul>
                ) : (
                    <p>No interests available</p>
                )}
            </div>
        </div>
    </>
    );


}
