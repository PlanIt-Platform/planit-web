import {Link, useLocation} from "react-router-dom";
import {isLogged} from "../../authentication/Session";
import user_icon from "../../../../images/profile-icon.png";
import React, {useContext, useState} from "react";
import {searchEvents} from "../../../services/eventsServices";
import {PlanItContext} from "../../../PlanItProvider";


export function TopBar({setIsLoading, setError}) {
    const { setEventsSearched } = useContext(PlanItContext);
    const location = useLocation();
    const [searchInput, setSearchInput] = useState('');

    const handleSearchChange = (event) => {
        setSearchInput(event.target.value);
    };

    const handleKeyPress = (event) => {
        if (event.key === 'Enter') {
            setIsLoading(true)
            searchEvents(searchInput)
                .then((res) => {
                    if (res.data.error) setError(res.data.error);
                    else {
                        setEventsSearched(res.data.events);
                        setError('');
                    }
                    setIsLoading(false)
                });
        }
    };

    return (
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
    )
}