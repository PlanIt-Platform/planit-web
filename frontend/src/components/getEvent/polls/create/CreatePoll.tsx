import React, {useState} from "react";
import "./CreatePoll.css";
import {createPoll} from "../../../../services/pollServices";
import trash_bin from "../../../../../images/trashbin.png";
import Error from "../../../error/Error";


export function CreatePoll({ onClose, eventId }) {
    const [error, setError] = useState("");
    const [inputs, setInputs] = useState({
        title: "",
        options: ["", ""],
        duration: "1"
    });

    function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault();
        createPoll(eventId, inputs)
            .then(res => {
                if (res.data.error) {
                    setError(res.data.error);
                    return;
                }
                onClose();
            });
    }

    function handleChange(ev: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
        const {name, value} = ev.target;
        setInputs(inputs => ({...inputs, [name]: value}));
    }

    function handleChangeOption(index: number, event: React.ChangeEvent<HTMLInputElement>) {
        const newOptions = [...inputs.options];
        newOptions[index] = event.target.value;
        setInputs(inputs => ({...inputs, options: newOptions}));
    }

    function handleAddOption() {
        if (inputs.options.length < 5) {
            setInputs(inputs => ({...inputs, options: [...inputs.options, ""]}));
            setError("")
        } else {
            setError("Maximum of 5 options allowed.");
        }
    }

    function handleDeleteOption(index: number) {
        setInputs(inputs => {
            const newOptions = [...inputs.options];
            newOptions.splice(index, 1);
            setError("")
            return {...inputs, options: newOptions};
        });
    }

    return (
        <>
            <div className={"overlay"} onClick={onClose}></div>
            <form onSubmit={handleSubmit}>
                <div className="poll-container">
                    <h2>Create a new poll!</h2>
                    <div className="poll-title">
                        <input type="text" placeholder="Poll Title" name="title" value={inputs.title} onChange={handleChange} required/>
                    </div>
                    {inputs.options.map((option, i) => (
                        <div className="poll-option" key={i}>
                            <input
                                type="text"
                                placeholder={`Option ${i + 1}`}
                                value={option}
                                onChange={event => handleChangeOption(i, event)}
                                required
                            />
                            {i >= 2 && (
                                <img
                                    src={trash_bin}
                                    className="trash-img"
                                    alt="Delete option"
                                    onClick={() => handleDeleteOption(i)}
                                    style={{cursor: 'pointer'}}
                                />
                            )}
                        </div>
                    ))}
                    <button className="add-option-button" onClick={() => {handleAddOption()}}>+</button>
                    <div className="durationSaveContainer">
                        <div className="duration-dropdown">
                            <label>Duration:</label>
                            <select name="duration" value={inputs.duration} onChange={handleChange} required>
                                <option value="" disabled>Select duration</option>
                                <option value="1">1 hour</option>
                                <option value="4">4 hours</option>
                                <option value="8">8 hours</option>
                                <option value="12">12 hours</option>
                                <option value="24">24 hours</option>
                                <option value="72">72 hours</option>
                            </select>
                        </div>
                        <button type="submit" className="save-button">Save</button>
                    </div>
                    {error && <Error message={error} onClose={() => setError(null)} />}
                </div>
            </form>
        </>
    )

}