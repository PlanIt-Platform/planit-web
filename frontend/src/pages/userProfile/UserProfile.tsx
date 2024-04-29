import React, { useEffect, useState} from 'react';
import {getUser} from "../../services/usersServices";
import {getUserId} from "../authentication/Session";
import "./UserProfile.css"

export default function UserDetails(): React.ReactElement {
    const [userData, setUserData] = useState<any>('');
    const [error, setError] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(false);

    const userId = getUserId();

    useEffect(() => {
        setIsLoading(true);
        getUser(userId)
            .then((data) => {
                setUserData(data);
                if(data.error) setError(data.error);
            })
            .catch((error: string) => {
                setError(error);
            })
            .finally(() => {
                setIsLoading(false);
            });
    }, [userId]);

    if (isLoading) {
        return <div>Loading...</div>;
    }

    // if (error) {
    //     return <div>Error: {error}</div>;
    // }

    return (
        <div className="body_userProfile">
            <div className="card-container">
                <img className="round" src="https://randomuser.me/api/portraits/men/69.jpg" alt="user"/> {/*29*/}
                <h3 className="h3_userProfile">{userData?.name}</h3>
                <h6 className="h6_userProfile">@{userData?.username}</h6>
                {userData?.description ? (
                    <p className="p_userProfile">{userData.description}</p>
                ) : (
                    <p>No description available</p>
                )}
                <div className="buttons">
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
        </div>
    );


}
