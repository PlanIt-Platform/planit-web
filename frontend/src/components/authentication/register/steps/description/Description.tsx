import React from "react";
import {Link} from "react-router-dom";

export function Description({setStep, description, setDescription}) {
    return (
        <div className={"desc-div"} style={{marginTop: "150px"}}>
            <Link to="/planit/register" onClick={() => setStep(1)} className={"linkStyle backStyle"}
                  style={{marginLeft: "10px"}}>Back</Link>
            <h2>Almost there!</h2>
            <textarea
                value={description}
                onChange={(ev) => setDescription(ev.target.value)}
                placeholder="Tell us about yourself..."
            />
            <button type="submit" style={{marginRight: "10px"}}>
                {'Register'}
            </button>
        </div>
    )
}