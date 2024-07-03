import {Link, Navigate} from "react-router-dom";
import React, {useContext, useState} from "react";
import {setSession} from "../Session";
import {editUser, register} from "../../../services/usersServices";
import '../authStyle.css';
import {PlanItContext} from "../../../PlanItProvider";
import Error from "../../shared/error/Error";
import Loading from "../../shared/loading/Loading";
import {Credentials} from "./steps/credentials/Credentials";
import {Interests} from "./steps/interests/Interests";
import {Description} from "./steps/description/Description";

export default function Register(): React.ReactElement {
    const { setUserId } = useContext(PlanItContext);
    const [inputs, setInputs] = useState({email: "", username: "", password: "", name: ""})
    const [error, setError] = useState('')
    const [isLoading, setIsLoading] = useState(false)
    const [redirect, setRedirect] = useState(false)
    const [step, setStep] = useState(1); // Step 1: Registration details, Step 2: Interests, Step 3: Description
    const [interests, setInterests] = useState([]);
    const [description, setDescription] = useState('');

    if (redirect) return <Navigate to="/planit/events" replace={true}/>;

    function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault()
        if (step == 1) {
            setIsLoading(true)
            register(inputs)
                .then(res => {
                    if (res.data.error) {
                        setError(res.data.error)
                        setIsLoading(false)
                        return
                    }
                    setSession(res.data.id, setUserId);
                    setStep(2)
                    setError('')
                    setIsLoading(false)
                })
        }
        if (step == 3){
            setIsLoading(true)
            editUser(inputs.name, description, interests)
                .then(res => {
                    if (res.data.error) setError(res.data.error)
                    else setRedirect(true)
                    setIsLoading(false)
                    }
                )
        }
    }

    return (
        <div className="form-container fadeIn">
            {isLoading && <Loading/>}
            <div className="form-content" style={{marginBottom: "110px"}}>
                <form onSubmit={handleSubmit}>
                    {step === 1 && <Credentials inputs={inputs} setInputs={setInputs} handleSubmit={handleSubmit}/>}
                    {step === 2 && <Interests setStep={setStep} interests={interests} setInterests={setInterests}/>}
                    {step === 3 && <Description setStep={setStep} description={description} setDescription={setDescription}/>}
                    <p className="form-container_p">
                        Already have an account? <Link to="/planit/login" className={"linkStyle"}>Log In</Link>
                    </p>
                </form>
                {error && <Error message={error} onClose={() => setError(null)} />}
            </div>
        </div>
    );
};
