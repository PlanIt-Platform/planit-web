import React, {useRef, useState} from 'react';
import './NavBar.css';
import Error from "../shared/error/Error";
import Loading from "../shared/loading/Loading";
import {HamburgerMenu} from "./hamburgerMenu/HamburgerMenu";
import {TopBar} from "./topBar/TopBar";

export function NavBar() {
    const [error, setError] = useState('')
    const [isLoading, setIsLoading] = useState(false)
    const navRef = useRef<HTMLDivElement>(null);

    return (
        <div className="header" ref={navRef}>
            {isLoading && <Loading/>}
            <nav role="navigation">
                <HamburgerMenu navRef={navRef} setIsLoading={setIsLoading} setError={setError}/>
                <TopBar setIsLoading={setIsLoading} setError={setError}/>
            </nav>
            {error && <Error message={error} onClose={() => setError(null)} />}
        </div>
    )
}