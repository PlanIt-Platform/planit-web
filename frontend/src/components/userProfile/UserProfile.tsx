import React, { useEffect, useState} from 'react';
import {getUser, getUserTask, removeTask} from "../../services/usersServices";
import minus from "../../../images/minus.png";
import kick from "../../../images/kick.png";
import "./UserProfile.css"
import {kickUser} from "../../services/eventsServices";
import {getUserId} from "../authentication/Session";
import Error from "../error/Error";
import user_img from "../../../images/user_img.png";
import Loading from "../loading/Loading";

export default function UserProfile({onClose, userId, eventId, isOrganizer}) {
    const organizerId = getUserId();
    const [userData, setUserData] = useState<any>('');
    const [task, setTask] = useState<any>('');
    const [error, setError] = useState<string>('');
    const [isLoading, setIsLoading] = useState(true)

    useEffect(() => {
        setIsLoading(true)
        getUser(userId).then((res) => {
            if (res.data.error) {
                setError(res.data.error)
                setIsLoading(false)
                return
            }
            setUserData(res.data);
            getUserTask(userId, eventId)
                .then((res) => {
                    if (res.data.error) return
                    setTask(res.data);
                    setIsLoading(false)
                })
        })
    }, [userId]);

    const handleRemoveTask = () => {
        setIsLoading(true)
        removeTask(userId, task.id, eventId)
            .then((res) => {
                if(res.data.error) setError(res.data.error)
                else onClose()
                setIsLoading(false)
            })
    }

    const handleKickUser = () => {
        setIsLoading(true)
        kickUser(eventId, userId)
            .then((res) => {
                if(res.data.error) setError(res.data.error)
                else onClose()
                setIsLoading(false)
            })
    }

    if (isLoading) return <Loading onClose={() => setIsLoading(false)} />

    return (
        <>
            <div className={"overlay"} onClick={onClose}></div>
            <div className="card-container">
                {isOrganizer && userId != organizerId &&
                    <img src={kick} alt="Kick" className={"kick_img"} title={"Kick user"} onClick={handleKickUser}/>
                }
                <img src={user_img} alt="User" className={"user_img"}/>
                <h3 className="h3_userProfile" title={userData?.name}>{userData?.name}</h3>
                <h5 className="h5_userProfile">@{userData?.username}</h5>
                {userData?.description ? (
                    <p className="p_userProfile">{userData.description}</p>
                ) : (
                    <p>No description available</p>
                )}
                <div className="profile-buttons">
                    <div className="taskContainer">
                        {isOrganizer && task != '' && <img src={minus} alt="Remove" className={"minus_img"} title={"Remove task"}
                             onClick={(event) => {
                                 event.stopPropagation();
                                 handleRemoveTask();
                             }}/>}
                        <p className="taskName" title={task.name}>{task != '' ? task.name : "Task not found"}</p>
                     </div>
                </div>
                <div className="skills">
                    <h6>Interests</h6>
                    {userData?.interests && userData.interests.length > 0  && userData.interests[0] != "" ? (
                        <ul>
                            {userData.interests.map((interest: string, index: number) => (
                                <li key={index}>{interest}</li>
                            ))}
                        </ul>
                    ) : (
                        <p>No interests available</p>
                    )}
                </div>
                {error && <Error message={error} onClose={() => setError(null)} />}
            </div>
        </>
    );


}
