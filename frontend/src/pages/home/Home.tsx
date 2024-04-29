import React, {useContext, useState} from 'react';
import './homeStyle.css';
import logo from "../../../images/logo.png";
import {isLogged} from "../authentication/Session";
import {Navigate} from "react-router-dom";
import {PlanItContext} from "../../PlanItProvider";

export default function Home() {
    const { userId } = useContext(PlanItContext);
    const [loggedIn] = useState<boolean>(isLogged(userId));
    if (loggedIn) return <Navigate to="/planit/events" replace={true}/>;

    return (
        <div className={"containerStyle"}>
            <img src={logo} alt={"Image"} className="image-overlay"/>
            <div className={"headerStyle"}>
                <p style={{textAlign: 'center'}}>Welcome!<br />Sign in to continue.</p>
            </div>
        </div>
    );
}