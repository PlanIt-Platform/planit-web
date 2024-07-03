import plus from "../../../../../images/plus.png";
import React from "react";


export function ParticipantsList({participants, setIsUserProfileOpen, setParticipantId, isOrganizer, setIsAssigningRole}) {

    return (
        <div className="right">
            <h2>Participants ({participants.length})</h2>
            <ul>
                {
                    participants.map((participant: any) => {
                        return (
                            <div className="participant-container" key={participant.id}
                                 onClick={
                                     () => {
                                         setIsUserProfileOpen(true)
                                         setParticipantId(participant.id)
                                     }
                                 }>
                                <li title={participant.username} className={`participant-item ${participant.roleName === 'Organizer'
                                    ? 'organizerName' : ''}`}>{participant.username}</li>
                                {
                                    participant.roleName != 'Participant'
                                        ? <li title={participant.roleName} className={`participant-role
                                                ${participant.roleName === 'Organizer'
                                            ? 'organizer' : ''}`}>{participant.roleName}</li>
                                        : isOrganizer && <img src={plus} alt="Assign" className={"plus_img"}
                                              onClick={(event) => {
                                                  event.stopPropagation();
                                                  setIsAssigningRole(true)
                                                  setParticipantId(participant.id)
                                              }}/>
                                }
                            </div>
                        )})
                }
            </ul>
        </div>
    )
}