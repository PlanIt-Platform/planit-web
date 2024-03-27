import {Link, Navigate} from "react-router-dom";
import React, {useState} from "react";
import {setSession} from "./Session";
import {login} from "../../services/usersServices";
import './authStyle.css';
import logo from "../../../images/logo.png";

export default function Login(): React.ReactElement {
    const [inputs, setInputs] = useState({email: "", password: ""})
    const [submitting, setSubmitting] = useState(false)
    const [error, setError] = useState('')
    const [redirect, setRedirect] = useState(false)

    if (redirect) return <Navigate to="/" replace={true}/>;

    function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault()
        setSubmitting(true)
        const email = inputs.email
        const password = inputs.password
        login(email, password)
            .then(res => {
                if (res.error){
                    setError(res.error)
                    setSubmitting(false)
                    return
                }
                setSession(res.accessToken);
                setSubmitting(false)
                setRedirect(true)
            })
    }

    function handleChange(ev: React.FormEvent<HTMLInputElement>) {
        const name = ev.currentTarget.name;
        setInputs({...inputs, [name]: ev.currentTarget.value});
    }

    return (
        <div className="form-container fadeIn">
            <img src={logo} alt="Image" className="image-overlay" />
            <div className="form-content">
                <Link to="/" className={"linkStyle homeStyle"}>Home</Link>
                <form onSubmit={handleSubmit}>
                        <div>
                            <label htmlFor="email">Email</label>
                            <input
                                id="email"
                                name="email"
                                value={inputs.email}
                                onChange={handleChange}
                            />
                        </div>
                        <div>
                            <label htmlFor="password">Password</label>
                            <input
                                id="password"
                                type="password"
                                name="password"
                                value={inputs.password}
                                onChange={handleChange}
                            />
                        </div>
                        <div>
                            <button type="submit">
                                {submitting ? 'Logging in...' : 'Login'}
                            </button>
                        </div>
                        <p>
                            Don't have an account? <Link to="/planit/register" className={"linkStyle"}>Sign Up</Link>
                        </p>
                </form>
                {error && <p>{error}</p>}
            </div>
        </div>
    );
};
