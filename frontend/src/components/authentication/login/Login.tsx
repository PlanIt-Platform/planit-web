import {Link, Navigate} from "react-router-dom";
import React, {useContext, useState} from "react";
import {setSession} from "../Session";
import {login} from "../../../services/usersServices";
import '../authStyle.css';
import logo from "../../../../images/logo.png";
import {PlanItContext} from "../../../PlanItProvider";
import Error from "../../shared/error/Error";
import Loading from "../../shared/loading/Loading";
import {FormField} from "../shared/FormField";

export default function Login(): React.ReactElement {
    const { setUserId } = useContext(PlanItContext);
    const [inputs, setInputs] = useState({emailOrUsername: "", password: ""})
    const [submitting, setSubmitting] = useState(false)
    const [error, setError] = useState('')
    const [isLoading, setIsLoading] = useState(false)
    const [redirect, setRedirect] = useState(false)

    if (redirect) return <Navigate to="/planit/events" replace={true}/>;

    function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault()
        const emailOrUsername = inputs.emailOrUsername
        const password = inputs.password
        setSubmitting(true)
        setIsLoading(true)
        login(emailOrUsername, password)
            .then(res => {
                if (res.data.error) setError(res.data.error)
                else {
                    setSession(res.data.id, setUserId);
                    setRedirect(true)
                }
                setSubmitting(false)
                setIsLoading(false)
            })
    }

    function handleChange(ev: React.FormEvent<HTMLInputElement>) {
        const name = ev.currentTarget.name;
        setInputs({...inputs, [name]: ev.currentTarget.value});
    }

    return (
        <div className="form-container fadeIn">
            {isLoading && <Loading/>}
            <img src={logo} alt="Image" className="auth-image-overlay" />
            <div className="form-content">
                <Link to="/" className={"linkStyle homeStyle"}  style={{marginRight: "145px", width: "14%"}}>Home</Link>
                <form onSubmit={handleSubmit}>
                    <FormField label="Email or Username" type="text" name="emailOrUsername" value={inputs.emailOrUsername} onChange={handleChange} />
                    <FormField label="Password" type="password" name="password" value={inputs.password} onChange={handleChange} />
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
