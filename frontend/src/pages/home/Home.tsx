import React, {useEffect, useState} from 'react';
import {Link, Outlet} from 'react-router-dom';
import {clearSession, isLogged} from '../authentication/Session';
import {logout} from "../../services/usersServices";
import './homeStyle.css';
import logo from "../../../images/logo.png";

export default function Home() {
    const [loggedIn, setLoggedIn] = useState<boolean>(isLogged());
    const [error, setError] = useState('');

    const handleLogout = async (ev: React.FormEvent<HTMLFormElement>): Promise<boolean> => {
        ev.preventDefault();
        try {
            const data = await logout();
            console.log('Logout successful', data);
            setLoggedIn(false);
            clearSession();
            return true;
        } catch (error) {
            console.error('Logout failed', error);
            setError(error.message);
            throw error;
        }
    };

    return (
        <div className={"containerStyle"}>
            <img src={logo} alt={"Image"} className="image-overlay"/>
            <div className={"headerStyle"}>
                Welcome to PlanIt
            </div>
            {loggedIn ? (
                <div>
                    <div>
                        <form onSubmit={handleLogout}>
                            <button>Logout</button>
                        </form>
                    </div>
                </div>
            ) : (
                <div>
                    <div>
                        <Link to="/planit/login" className={"contentStyle"}>Login</Link>
                        <Link to="/planit/register" className={"contentStyle"}>Register</Link>
                    </div>
                </div>
            )}
            <Outlet />
            {error && <p>{error}</p>}
        </div>
    );
}