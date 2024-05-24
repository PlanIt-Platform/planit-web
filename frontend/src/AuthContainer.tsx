import {isLogged} from "./components/authentication/Session";
import {Navigate} from "react-router-dom";
import React, {useEffect, useState} from 'react';


export function RequireAuth({ children }) {
    const [userId, setUserId] = useState(localStorage.getItem('user_id'));

    useEffect(() => {
        const intervalId = setInterval(() => {
            setUserId(localStorage.getItem('user_id'));
        }, 1000); // checks every second

        // cleanup on unmount
        return () => {
            clearInterval(intervalId);
        };
    }, []);

    if (!isLogged() || !userId) return <Navigate to="/" replace={true}/>;

    return children;
}