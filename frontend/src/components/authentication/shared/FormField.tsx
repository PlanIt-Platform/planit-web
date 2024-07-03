import React from "react";

export const FormField = ({label, type, name, value, onChange}) => (
    <div className="inline-field">
        <label htmlFor={name}>{label}</label>
        <input id={name} type={type} name={name} value={value} onChange={onChange} />
    </div>
);
