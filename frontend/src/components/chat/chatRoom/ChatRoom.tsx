import React, {useEffect, useRef, useState} from 'react';
import ChatMessage from "../chatMessage/ChatMessage";
import {addDoc, collection, limit, onSnapshot, orderBy, query, serverTimestamp} from "firebase/firestore";
import {getUser} from "../../../services/usersServices";
import arrowDown from "../../../../images/arrowdown.png";
import arrowUp from "../../../../images/arrowup.png";
import {CreatePoll} from "../../polls/create/CreatePoll";
import {ViewPolls} from "../../polls/view/ViewPolls";
import {db} from "../../../Router";
import "./ChatRoom.css";
import Error from "../../shared/error/Error";

export function ChatRoom({uId, eventId, isOrganizer}) {
    const [messages, setMessages] = useState([]);
    const [messageText, setMessageText] = useState('');
    const [name, setName] = useState('');
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const [isCreatingPoll, setIsCreatingPoll] = useState(false);
    const [isViewingPolls, setIsViewingPolls] = useState(false);
    const messagesEndRef = useRef(null);
    const [error, setError] = useState('')

    useEffect(() => {
        getUser(uId)
            .then((res) => {
                if (res.data.error) setError(res.data.error);
                else setName(res.data.name);
            })
    }, [])

    useEffect(() => {
        if (db) {
            const q = query(collection(db, `events/${eventId}/messages`), orderBy('createdAt'), limit(100));
            return onSnapshot(q, querySnapshot => {
                const data = querySnapshot.docs.map(doc => ({
                    ...doc.data(),
                    id: doc.id,
                }));
                setMessages(data);
            })
        }
    }, [db])

    useEffect(() => {
        if (messagesEndRef.current) {
            messagesEndRef.current.scrollIntoView({ behavior: "auto" });
        }
    }, [messages]);

    const handleOnChange = (e) => {
        setMessageText(e.target.value);
    }

    const handleOnSubmit = async (e) => {
        e.preventDefault()

        if (db) {
            try {
                await addDoc(collection(db, `events/${eventId}/messages`), {
                    text: messageText,
                    createdAt: serverTimestamp(),
                    uId,
                    name
                });
                setMessageText('');
            } catch (e) {
                console.error("Error adding document: ", e);
            }
        }
    }

    return (
        <div className="chat-background">
            <div className="chat-container">
                {messages.map((message, index) => (
                    <ChatMessage key={index} message={message} />
                ))}
                <div ref={messagesEndRef} />
                <form onSubmit={handleOnSubmit}>
                    {isDropdownOpen && (
                        <div className="dropdown-menu">
                            {isOrganizer && <button onClick={() => {
                                setIsCreatingPoll(true)
                                setIsDropdownOpen(!isDropdownOpen)
                            }}>Create a poll</button>}
                            <button onClick={() => {
                                setIsViewingPolls(true)
                                setIsDropdownOpen(!isDropdownOpen)
                            }}>View polls</button>
                        </div>
                    )}
                    <div className="chat-input">
                        <img src={isDropdownOpen ? arrowDown : arrowUp}
                             alt="Toggle menu" className={"arrows_img"} onClick={() => setIsDropdownOpen(!isDropdownOpen)}/>
                        <input
                            type="text"
                            value={messageText}
                            onChange={handleOnChange}
                            placeholder={'Enter your message...'}
                        />
                        <button className="chat-input-send-button" disabled={!messageText}>Send</button>
                    </div>
                </form>
                {isCreatingPoll &&
                    <CreatePoll
                        onClose={() => {
                            setIsCreatingPoll(false)
                        }}
                        eventId={eventId}
                    />}
                {isViewingPolls &&
                    <ViewPolls
                        onClose={() => {
                            setIsViewingPolls(false)
                        }}
                        eventId={eventId}
                        isOrganizer={isOrganizer}
                    />}
                {error && <Error message={error} onClose={() => setError(null)} />}
            </div>
        </div>
    );
}