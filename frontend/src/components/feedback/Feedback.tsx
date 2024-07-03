import Error from "../shared/error/Error";
import React, {useState} from "react";
import {sendFeedback} from "../../services/usersServices";
import './Feedback.css';

export function Feedback({onClose}){
    const [feedback, setFeedback] = useState({
        text: ""
    });
    const [error, setError] = useState('');

    function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault()
        sendFeedback(feedback)
            .then(res => {
                if (res.data.error) setError(res.data.error)
                else {
                    setError("")
                    onClose()
                }
            })
    }

    return (
        <>
            <div className={"overlay"} onClick={onClose}></div>
            <div className="feedback-container">
                <h2>Give us your feedback!</h2>
                <form onSubmit={handleSubmit}>
                    <textarea
                        name="feedback"
                        value={feedback.text}
                        onChange={e => setFeedback({text: e.target.value})}
                        placeholder="Tell us what you think.."
                        required
                    />
                    <button type="submit">Submit</button>
                </form>
                {error && <Error message={error} onClose={() => setError(null)} />}
            </div>
        </>
    );
}