import React from "react";
import {FormField} from "../../../shared/FormField";
import {Link} from "react-router-dom";

export function Credentials({inputs, setInputs, handleSubmit}) {

    function handleChange(ev: React.FormEvent<HTMLInputElement>) {
        const name = ev.currentTarget.name;
        setInputs({...inputs, [name]: ev.currentTarget.value})
    }

    return (
        <div>
            <Link to="/" className={"linkStyle homeStyle"}
                  style={{marginLeft: "145px", width: "14%"}}>Home</Link>
            <FormField label="Email" type="text" name="email" value={inputs.email} onChange={handleChange} />
            <FormField label="Username" type="text" name="username" value={inputs.username} onChange={handleChange} />
            <FormField label="Name" type="text" name="name" value={inputs.name} onChange={handleChange} />
            <FormField label="Password" type="password" name="password" value={inputs.password} onChange={handleChange} />
            <button className="form-container_button" type="submit" style={{marginRight: "220px"}} onClick={handleSubmit}>
                {'Next'}
            </button>
        </div>
    )
}