import {Link, Navigate} from "react-router-dom";
import React, {useContext, useState} from "react";
import {setSession} from "./Session";
import {login} from "../../services/usersServices";
import './authStyle.css';
import logo from "../../../images/logo.png";
import {PlanItContext} from "../../PlanItProvider";
import Error from "../error/Error";

export default function Login(): React.ReactElement {
    const { setUserId } = useContext(PlanItContext);
    const [inputs, setInputs] = useState({emailOrName: "", password: ""})
    const [submitting, setSubmitting] = useState(false)
    const [error, setError] = useState('')
    const [redirect, setRedirect] = useState(false)

    if (redirect) return <Navigate to="/planit/events" replace={true}/>;

    function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault()
        setSubmitting(true)
        const email = inputs.emailOrName
        const password = inputs.password
        login(email, password)
            .then(res => {
                if (res.data.error){
                    setError(res.data.error)
                    setSubmitting(false)
                    return
                }
                setSession(res.data.id, setUserId);
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
                <Link to="/" className={"linkStyle homeStyle"} style={{ width: "14%"}}>Home</Link>
                <form onSubmit={handleSubmit}>
                        <div>
                            <label htmlFor="emailOrName">Email Or Username</label>
                            <input
                                id="emailOrName"
                                type="text"
                                name="emailOrName"
                                value={inputs.emailOrName}
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
                            <button className="form-container_button" type="submit">
                                {submitting ? 'Logging in...' : 'Login'}
                            </button>
                        </div>
                        <p className="form-container_p">
                            Don't have an account? <Link to="/planit/register" className={"linkStyle"}>Sign Up</Link>
                        </p>
                </form>
                {error && <Error message={error} onClose={() => setError(null)} />}
            </div>
        </div>
    );
};
