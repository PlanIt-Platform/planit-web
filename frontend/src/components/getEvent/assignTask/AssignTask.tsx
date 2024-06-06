import React, {useState} from "react";
import {assignTask} from "../../../services/usersServices";
import "./AssignTask.css";
import Error from "../../error/Error";
import Loading from "../../loading/Loading";

export function AssignTask({ onClose, userId, eventId }) {
    const [error, setError] = useState("");
    const [isLoading, setIsLoading] = useState(false)
    const [taskName, setTaskName] = useState({
        taskName: "Organizer"
    });
    const [isCustomTask, setIsCustomTask] = useState(false);

    function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault();
        setIsLoading(true)
        assignTask(userId, eventId, taskName)
            .then(res => {
                if (res.data.error) setError(res.data.error);
                else {
                    setIsCustomTask(false)
                    onClose();
                }
                setIsLoading(false)
            });
    }

    function handleChange(ev: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
        const {name, value} = ev.target;
        if(value === "Organizer") setIsCustomTask(false)
        else if (value === "Custom") setIsCustomTask(true);
        else setTaskName(inputs => ({...inputs, [name]: value}));
    }

    return (
        <>
            {isLoading && <Loading onClose={() => setIsLoading(false)} />}
            <div className={"overlay"} onClick={onClose}></div>
            <div className="assign-task-container">
                <form onSubmit={handleSubmit}>
                    <label className="assign-label">
                       <h3> Assign new task:</h3>
                        <select name="taskName" onChange={handleChange} required>
                            <option value="" disabled>Select a task</option>
                            <option value="Organizer">Organizer</option>
                            <option value="Custom">Custom</option>
                        </select>
                        {isCustomTask && <input type="text" name="taskName"
                         placeholder="Insert custom task..." onChange={handleChange} required />}
                    </label>
                    <button type="submit">Save</button>
                </form>
                {error && <Error message={error} onClose={() => setError(null)} />}
            </div>
        </>
    )
}