import React, {useEffect, useState} from 'react';
import {useParams} from "react-router-dom";
import {getUser} from "../../services/usersServices";
import "../style/components.css"

export default function UserDetails() {
    const [userData, setUserData] = useState<any>('');
    const userId = useParams().id;

    useEffect(() => {
        getUser(userId)
            .then((data) => {
                setUserData(data)
            })
    }, [])

    return (
       <div>
           TODO
       </div>
    );
}
