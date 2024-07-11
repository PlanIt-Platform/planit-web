import React, {useState} from "react";
import {Link} from "react-router-dom";

export function Interests({setStep, interests, setInterests}) {
    const [newInterest, setNewInterest] = useState('');

    return (
        <div className={"int-div"} style={{marginTop: "190px"}}>
            <Link to="/planit/register" onClick={() => setStep(1)} className={"linkStyle backStyle"}>Back</Link>
            <h2 className="form-container_h2">Interests</h2>
            <input type="text" style={{paddingRight: "0px"}}
                   placeholder={"Enter an interest (e.g. football, etc.)"}
                   value={newInterest} onChange={(ev) => setNewInterest(ev.target.value)} />
            <button onClick={() => {
                if (!interests.includes(newInterest)) {
                    setInterests([...interests, newInterest]);
                    setNewInterest('');
                }
            }}>Add Interest</button>
            <div className="interests-container">
                {interests.map((interest, index) => (
                    <div key={index} className="interest">
                        {interest}
                        <button onClick={() => setInterests(interests.filter((_, i) => i !== index))}>X</button>
                    </div>
                ))}
            </div>
            <button className="form-container_button" type="button" onClick={() => setStep(3)}
                    style={{marginRight: "10px"}}>
                {'Next'}
            </button>
        </div>
    )
}