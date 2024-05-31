import React, {useEffect, useState} from 'react';
import {getPoll, votePoll} from "../../../../services/pollServices";
import './ViewPoll.css';

export function ViewPoll({onClose, eventId, pollId}) {
    const [error, setError] = useState('');
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
        getPoll(eventId, pollId).then(res => {
            if (res.data.error) {
                setError(res.data.error)
                return;
            }
            setPoll(res.data);
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
        votePoll(eventId, pollId, optionId).then(res => {
            if (res.data.error) {
                setError(res.data.error);
                return;
            }
            onClose();
        })
    }

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
                {error && <div className="error">{error}</div>}
            </div>
        </>
    );
}