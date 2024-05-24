import {Link, Navigate} from "react-router-dom";
import React, {useContext, useState} from "react";
import {setSession} from "./Session";
import {editUser, register} from "../../services/usersServices";
import './authStyle.css';
import {PlanItContext} from "../../PlanItProvider";
import {getCategories} from "../../services/eventsServices";

const FormField = ({label, type, name, value, onChange}) => (
    <div className="inline-field">
        <label htmlFor={name}>{label}</label>
        <input id={name} type={type} name={name} value={value} onChange={onChange} />
    </div>
);

export default function Register(): React.ReactElement {
    const { setUserId } = useContext(PlanItContext);
    const [inputs, setInputs] = useState({email: "", username: "", password: "", name: ""})
    const [error, setError] = useState('')
    const [redirect, setRedirect] = useState(false)
    const [step, setStep] = useState(1); // Step 1: Registration details, Step 2: Interests, Step 3: Description
    const [interests, setInterests] = useState([]);
    const [categories, setCategories] = useState([]);
    const [description, setDescription] = useState('');

    if (redirect) return <Navigate to="/planit/events" replace={true}/>;

    function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault()
        if (step == 1) {
            register(inputs)
                .then(res => {
                    if (res.data.error) {
                        setError(res.data.error)
                        return
                    }
                    setSession(res.data.id, setUserId);
                    setStep(2)
                    setError('')

                    getCategories()
                        .then((res) => {
                            if (res.data.error) {
                                setError(res.data.error);
                                return
                            } else {
                                const filteredCategories = res.data.filter(category => category !== 'Simple Meeting');
                                setCategories(filteredCategories);
                            }
                        });

                })
        }
        if (step == 3){
            editUser(inputs.name, description, interests)
                .then(res => {
                        if (res.data.error) {
                            setError(res.data.error)
                            return
                        }
                        setRedirect(true)
                    }
                )
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
            <div className="form-content" style={{marginBottom: "110px"}}>
                <form onSubmit={handleSubmit}>
                    {step === 1 && (
                        <div>
                            <Link to="/" className={"linkStyle homeStyle"}
                                  style={{marginLeft: "145px", width: "14%"}}>Home</Link>
                            <FormField label="Email" type="text" name="email" value={inputs.email} onChange={handleChange} />
                            <FormField label="Username" type="text" name="username" value={inputs.username} onChange={handleChange} />
                            <FormField label="Name" type="text" name="name" value={inputs.name} onChange={handleChange} />
                            <FormField label="Password" type="password" name="password" value={inputs.password} onChange={handleChange} />
                            <button type="submit" style={{marginRight: "220px"}}>
                                {'Next'}
                            </button>
                        </div>
                    )}
                    {step === 2 && (
                        <div className={"int-div"} style={{marginTop: "190px"}}>
                            <Link to="/planit/register" onClick={() => setStep(1)} className={"linkStyle backStyle"}>Back</Link>
                            <h2>Interests</h2>
                            <div className="checkbox-wrapper">
                                {categories.map(interest => (
                                    <div key={interest}>
                                        <input type="checkbox" id={interest} onChange={() => handleInterestSelect(interest)}/>
                                        <label htmlFor={interest}>{interest}</label>
                                    </div>
                                ))}
                            </div>
                            <button type="button" onClick={() => setStep(3)} style={{marginRight: "10px"}}>
                                {'Next'}
                            </button>
                        </div>
                    )}
                    {step === 3 && (
                        <div className={"desc-div"} style={{marginTop: "150px"}}>
                            <Link to="/planit/register" onClick={() => setStep(1)} className={"linkStyle backStyle"}
                            style={{marginLeft: "10px"}}>Back</Link>
                            <h2>Almost there!</h2>
                            <textarea
                                value={description}
                                onChange={(ev) => setDescription(ev.target.value)}
                                placeholder="Tell us about yourself..."
                            />
                            <button type="submit" style={{marginRight: "10px"}}>
                                {'Register'}
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
