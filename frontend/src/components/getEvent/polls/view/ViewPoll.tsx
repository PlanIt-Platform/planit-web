import React, {useEffect, useState} from 'react';
import {getPoll, votePoll} from "../../../../services/pollServices";
import './ViewPoll.css';
import Error from "../../../error/Error";
import Loading from "../../../loading/Loading";

export function ViewPoll({onClose, eventId, pollId}) {
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(true)
    const [checked, setChecked] = useState(null);
    const [optionId, setOptionId] = useState(0);
    const [poll, setPoll] = useState({
        id: 0,
        title: "",
        created_at: "",
        duration: 0,
        options: [{
            id: 0,
            title: "",
            votes: 0
        }]
    });

    useEffect(() => {
        setIsLoading(true)
        getPoll(eventId, pollId)
            .then(res => {
                if (res.data.error) setError(res.data.error)
                else setPoll(res.data);
                setIsLoading(false)
            })
    }, []);

    const handleVoteChange = (optionId, index) => {
        setChecked(index);
        setOptionId(optionId);
    }

    const handleSubmit = () => {
        if (checked === null) {
            setError("Select an option to vote");
            return;
        }
        setIsLoading(true)
        votePoll(eventId, pollId, optionId)
            .then(res => {
                if (res.data.error) setError(res.data.error);
                else onClose();
                setIsLoading(false)
            })
    }

    if (isLoading) return <Loading onClose={() => setIsLoading(false)} />

    return (
        <>
            <div className={"overlay"} onClick={onClose}></div>
            <div className="view-poll-container">
                <div className="view-poll-header">
                    <h2>{poll.title}</h2>
                    <p>Select only one answer</p>
                </div>
                <div className="view-poll-options">
                    {poll.options.map((option, index) => {
                        return (
                            <div key={index} className="view-poll-option">
                                <label htmlFor={`option${index}`}>{option.title} ({option.votes} votes)</label>
                                <input type="checkbox" id={`option${index}`}
                                       name="pollOption" checked={checked === index}
                                       onChange={() => handleVoteChange(option.id, index)}/>
                            </div>
                        )
                    })}
                </div>
                <button className="view-poll-submit" onClick={handleSubmit}>Submit</button>
                {error && <Error message={error} onClose={() => setError(null)} />}
            </div>
        </>
    );
}