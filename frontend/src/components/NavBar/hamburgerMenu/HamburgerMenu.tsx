import {clearSession, isLogged} from "../../authentication/Session";
import {Link} from "react-router-dom";
import React, {useContext, useEffect, useState} from "react";
import {getUser, logout} from "../../../services/usersServices";
import {PlanItContext} from "../../../PlanItProvider";
import {Feedback} from "../../feedback/Feedback";


export function HamburgerMenu({navRef, setIsLoading, setError}) {
    const { userId, setUserId } = useContext(PlanItContext);
    const [username, setUsername] = useState<string>('');
    const [navOpen, setNavOpen] = useState<boolean>(false);
    const [isFeedbackOpen, setIsFeedbackOpen] = useState<boolean>(false);

    useEffect(() => {
        if (isLogged()) {
            getUser(userId)
                .then((res) => {
                    if (res.data.error) setError(res.data.error);
                    else setUsername(res.data.name);
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

    const handleLogout = async (): Promise<void> => {
        setIsLoading(true)
        logout()
            .then((res) => {
                if (res.data.error) setError(res.data.error)
                else clearSession(setUserId)
                setIsLoading(false)
            })
    }

    return (
        <div id="menuToggle">
            <input type="checkbox" className="menuToggleMenu" checked={navOpen} onChange={() => setNavOpen(!navOpen)} />
            <span className="menuToggleSpan"></span>
            <span className="menuToggleSpan"></span>
            <span className="menuToggleSpan"></span>
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
                            <Link to="/planit/nearme" className={"menuOption"}>Near me</Link>
                            <button type="button" onClick={handleLogout}>Logout</button>
                            <button className={"feedbackStyle"} onClick={() => setIsFeedbackOpen(true)}>
                                Give us your feedback!
                            </button>
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
            {isFeedbackOpen && (
                <Feedback onClose={() => setIsFeedbackOpen(false)} />
            )
            }
        </div>
    )
}