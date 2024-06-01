import React, { useEffect, useState} from 'react';
import {getUser, getUserTask, removeTask} from "../../services/usersServices";
import minus from "../../../images/minus.png";
import kick from "../../../images/kick.png";
import "./UserProfile.css"
import {kickUser} from "../../services/eventsServices";
import {getUserId} from "../authentication/Session";

export default function UserProfile({onClose, userId, eventId, isOrganizer}) {
    const organizerId = getUserId();
    const [userData, setUserData] = useState<any>('');
    const [task, setTask] = useState<any>('');
    const [error, setError] = useState<string>('');

    useEffect(() => {
        getUser(userId).then((res) => {
            if(res.data.error) {
                setError(res.data.error)
                return
            }
            setUserData(res.data);
        })

        getUserTask(userId, eventId).then((res) => {
            if(res.data.error) {
                setError(res.data.error)
                return
            }
            setTask(res.data);
        })
    }, [userId]);

    const handleRemoveTask = () => {
        removeTask(userId, task.id, eventId).then((res) => {
            if(res.data.error) {
                setError(res.data.error)
                return
            }
            onClose()
        })
    }

    const handleKickUser = () => {
        kickUser(eventId, userId).then((res) => {
            if(res.data.error) {
                setError(res.data.error)
                return
            }
            onClose()
        })
    }

    return (
        <>
        <div className={"overlay"} onClick={onClose}></div>
        <div className="card-container">
            {isOrganizer && userId != organizerId &&
                <img src={kick} alt="Kick" className={"kick_img"} title={"Kick user"} onClick={handleKickUser}/>
            }
            <img className="round" src="https://randomuser.me/api/portraits/men/69.jpg" alt="user"/> {/*29*/}
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
                    <p className="taskName" title={task.name}>{task != '' ? task.name : error}</p>
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
        </div>
    </>
    );


}
