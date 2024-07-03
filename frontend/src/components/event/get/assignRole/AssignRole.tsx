import React, {useState} from "react";
import {assignRole} from "../../../../services/usersServices";
import "./AssignRole.css";
import Error from "../../../shared/error/Error";
import Loading from "../../../shared/loading/Loading";

export function AssignRole({ onClose, userId, eventId }) {
    const [error, setError] = useState("");
    const [isLoading, setIsLoading] = useState(false)
    const [roleName, setRoleName] = useState({
        roleName: "Organizer"
    });

    function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault();
        setIsLoading(true)
        assignRole(userId, eventId, roleName)
            .then(res => {
                if (res.data.error) setError(res.data.error);
                else onClose();
                setIsLoading(false)
            });
    }

    function handleChange(ev: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
        const {name, value} = ev.target;
        setRoleName(inputs => ({...inputs, [name]: value}));
    }

    return (
        <>
            {isLoading && <Loading/>}
            <div className={"overlay"} onClick={onClose}></div>
            <div className="assign-role-container">
                <form onSubmit={handleSubmit}>
                    <label className="assign-label">
                       <h3> Assign new role:</h3>
                        <select name="roleName" onChange={handleChange} required>
                            <option value="" disabled>Select a role</option>
                            <option value="Organizer">Organizer</option>
                        </select>
                    </label>
                    <button type="submit">Save</button>
                </form>
                {error && <Error message={error} onClose={() => setError(null)} />}
            </div>
        </>
    )
}