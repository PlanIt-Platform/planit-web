import {Link, Navigate} from "react-router-dom";
import React, {useState} from "react";
import {setSession} from "./Session";
import {register} from "../../services/usersServices";
import './authStyle.css';
import logo from "../../../images/logo.png";

export default function Register(): React.ReactElement {
    const [inputs, setInputs] = useState({email: "", username: "", password: "", name: ""})
    const [submitting, setSubmitting] = useState(false)
    const [error, setError] = useState('')
    const [redirect, setRedirect] = useState(false)
    const [step, setStep] = useState(1); // Step 1: Registration details, Step 2: Interests, Step 3: Description
    const [interests, setInterests] = useState([]);
    const [description, setDescription] = useState('');

    if (redirect) return <Navigate to="/" replace={true}/>;

    function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault()
        setSubmitting(true)
        const email = inputs.email
        const username = inputs.username
        const name = inputs.name
        const password = inputs.password
        if (step == 3) {
            register(username, name, email, password, interests, description)
                .then(res => {
                    if (res.error) {
                        setError(res.error)
                        setSubmitting(false)
                        return
                    }
                    setSession(res.accessToken);
                    setSubmitting(false)
                    setRedirect(true)
                })
        }
    }

    function handleChange(ev: React.FormEvent<HTMLInputElement>) {
        const name = ev.currentTarget.name;
        setInputs({...inputs, [name]: ev.currentTarget.value})
    }

    function handleInterestSelect(interest: string) {
        if (interest in interests) {
            setInterests(interests.filter(i => i !== interest)); // Deselect interest if already selected
        } else {
            setInterests([...interests, interest]); // Select interest
        }
    }

    return (
        <div className="form-container fadeIn">
            <div className="form-content">
                <form onSubmit={handleSubmit}>
                    {step === 1 && (
                        <div>
                            <img src={logo} alt="Image" className="image-overlay" />
                            <Link to="/" className={"linkStyle homeStyle"}>Home</Link>
                            <div className="inline-field">
                            <label htmlFor="email">Email</label>
                            <input
                                id="email"
                                type="text"
                                name="email"
                                value={inputs.email}
                                onChange={handleChange}
                            />
                            <label htmlFor="username">Username</label>
                            <input
                                id="username"
                                type="text"
                                name="username"
                                value={inputs.username}
                                onChange={handleChange}
                            />
                            <label htmlFor="name">Name</label>
                            <input
                                id="name"
                                type="text"
                                name="name"
                                value={inputs.name}
                                onChange={handleChange}
                            />
                            <label htmlFor="password">Password</label>
                            <input
                                id="password"
                                type="password"
                                name="password"
                                value={inputs.password}
                                onChange={handleChange}
                            />
                            </div>
                            <button type="button" onClick={() => setStep(2)}>
                                {'Next'}
                            </button>
                        </div>
                    )}
                    {step === 2 && (
                        <div className={"int-div"}>
                            <Link to="/planit/register" onClick={() => setStep(1)} className={"linkStyle backStyle"}>Back</Link>
                            <h2>Interests</h2>
                            <div className="checkbox-wrapper">
                                <div>
                                    <input type="checkbox" id="sports" onChange={() => handleInterestSelect('sports')}/>
                                    <label htmlFor="sports">Sports and Outdoor</label>
                                </div>
                                <div>
                                    <input type="checkbox" id="culture"
                                           onChange={() => handleInterestSelect('culture')}/>
                                    <label htmlFor="culture">Culture</label>
                                </div>
                                <div>
                                    <input type="checkbox" id="education"
                                           onChange={() => handleInterestSelect('education')}/>
                                    <label htmlFor="education">Education</label>
                                </div>
                                <div>
                                    <input type="checkbox" id="entertainment"
                                           onChange={() => handleInterestSelect('entertainment')}/>
                                    <label htmlFor="entertainment">Entertainment</label>
                                </div>
                                <div>
                                    <input type="checkbox" id="volunteering"
                                           onChange={() => handleInterestSelect('volunteering')}/>
                                    <label htmlFor="volunteering">Volunteering</label>
                                </div>
                            </div>
                            <button type="button" onClick={() => setStep(3)}>
                                {'Next'}
                            </button>
                        </div>
                    )}
                    {step === 3 && (
                        <div className={"desc-div"}>
                            <Link to="/planit/register" onClick={() => setStep(1)} className={"linkStyle backStyle"}>Back</Link>
                            <h2>Description</h2>
                            <textarea
                                value={description}
                                onChange={(ev) => setDescription(ev.target.value)}
                                placeholder="Tell us about yourself..."
                            />
                            <button type="submit">
                                {submitting ? 'Registering...' : 'Register'}
                            </button>
                        </div>
                    )}
                        <p>
                            Already have an account? <Link to="/planit/login" className={"linkStyle"}>Log In</Link>
                        </p>
                </form>
                {error && <p>{error}</p>}
            </div>
        </div>
    );
};
