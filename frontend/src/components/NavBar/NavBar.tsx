import React, {useContext, useEffect, useRef, useState} from 'react';
import './NavBar.css';
import {Link, useLocation} from "react-router-dom";
import {clearSession, isLogged} from "../authentication/Session";
import {PlanItContext} from "../../PlanItProvider";
import user_icon from "../../../images/profile-icon.png";
import {getUser, logout} from "../../services/usersServices";
import {searchEvents} from "../../services/eventsServices";
import Error from "../error/Error";

export function NavBar() {
    const { userId, setUserId, setEventsSearched } = useContext(PlanItContext);
    const [error, setError] = useState('')
    const [username, setUsername] = useState<string>('');
    const [navOpen, setNavOpen] = useState<boolean>(false);
    const navRef = useRef<HTMLDivElement>(null);
    const location = useLocation();
    const [searchInput, setSearchInput] = useState('');

    const handleLogout = async (): Promise<void> => {
        logout()
            .then((res) => {
                if (res.data.error) {
                    setError(res.data.error)
                    return
                }
                clearSession(setUserId)
            })
        return
    }

    const handleSearchChange = (event) => {
        setSearchInput(event.target.value);
    };

    const handleKeyPress = (event) => {
        if (event.key === 'Enter') {
            searchEvents(searchInput)
                .then((res) => {
                    if (res.data.error) {
                        setError(res.data.error);
                        return;
                    }
                    setEventsSearched(res.data.events);
                    setError('');
                });
        }
    };

    useEffect(() => {
        if (isLogged()) {
            getUser(userId)
                .then((res) => {
                    if (res.data.error) {
                        setError(res.data.error);
                        return
                    }
                    setUsername(res.data.name);
                })
        }

        function handleClickOutside(event) {
            if (navRef.current && !navRef.current.contains(event.target)) {
                setNavOpen(false);
            }
        }

        // Bind the event listener
        document.addEventListener("mousedown", handleClickOutside);

        return () => {
            // Unbind the event listener on clean up
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, [navRef, isLogged()]);


    return (
        <div className="header" ref={navRef}>
            <nav role="navigation">
                <div id="menuToggle">
                    <input type="checkbox" checked={navOpen} onChange={() => setNavOpen(!navOpen)} />
                    <span></span>
                    <span></span>
                    <span></span>
                    <ul id="menu">
                        {isLogged() ? (
                            <div>
                                <div className={"menuText"}>
                                    <strong>{username}</strong>
                                    <br />
                                    <Link to="/planit/me" className={"menuStyleG"}>Manage account</Link>
                                </div>
                                <div className={"menuOptions"} style={{fontSize: "20px"}}>
                                <Link to="/planit/events" className={"menuOption"}>All Events</Link>
                                    <Link to="/planit/user/events" className={"menuOption"}>My Events</Link>
                                    <Link to="/planit/calendar" className={"menuOption"}>Calendar</Link>
                                    <button type="button" onClick={handleLogout}>Logout</button>
                                </div>
                            </div>
                        ) : (
                            <div className={"navStyle"}>
                                <Link to="/planit/login" className={"navButtonStyle"}>Login</Link>
                                <Link to="/planit/register" className={"navButtonStyle"}>Register</Link>
                                <p className={"textStyle"}> Discover all available functionalities! </p>
                            </div>
                        )}
                    </ul>
                </div>
                <div className="flex-container">
                    <div className="title-container">
                        <Link to={"/"} className="title">
                                Plan<strong>It</strong>
                        </Link>
                    </div>
                    {isLogged() ? (
                        <div className={"loggedInElements"}>
                            {location.pathname === '/planit/events' && (
                                <div className="search-container">
                                    <input type="text" placeholder="Search..."
                                           className="search-bar" value={searchInput}
                                           onChange={handleSearchChange} onKeyDown={handleKeyPress}/>
                                </div>
                            )}
                            <div className={`buttons ${location.pathname !== '/planit/events' ? 'buttonExtraPadding' : ''}`}>
                                <Link to={`/planit/me`}>
                                    <img src={user_icon} alt="Account Details" className="imageStyle"/>
                                </Link>
                            </div>
                        </div>
                    ) : (
                        <div className="buttons" style={{marginLeft: "1150px", marginBottom: "10px"}}>
                            <Link to="/planit/login" className={"contentStyle"}>Login</Link>
                            <Link to="/planit/register" className={"contentStyle"}>Register</Link>
                        </div>
                    )}
                </div>
            </nav>
            {error && <Error message={error} onClose={() => setError(null)} />}
        </div>
    )
}