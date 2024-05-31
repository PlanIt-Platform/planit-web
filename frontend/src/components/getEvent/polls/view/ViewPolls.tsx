import React, {useEffect, useState} from "react";
import {deletePoll, getPolls} from "../../../../services/pollServices";
import trashBin from "../../../../../images/trashbin.png";
import trophy from "../../../../../images/trophy.png";
import "./ViewPolls.css";
import {ViewPoll} from "./ViewPoll";

export function ViewPolls({ onClose, eventId, isOrganizer }) {
    const [error, setError] = useState("");
    const [polls, setPolls] = useState([]);
    const [isViewingPoll, setIsViewingPoll] = useState(false);
    const [update, setUpdate] = useState(false);
    const [selectedPollId, setSelectedPollId] = useState(0);

    useEffect(() => {
        getPolls(eventId).then(res => {
            if (res.data.error) {
                setError(res.data.error)
                return
            }
            setPolls(res.data);
        })
    }, [update])

    const handleDeletePoll = (pollId) => {
       deletePoll(eventId, pollId).then(res => {
              if (res.data.error) {
                setError(res.data.error)
                return
              }
              setPolls(polls.filter(poll => poll.id !== pollId));
       }
       )
    }

    const getWinningOption = (options) => {
        const sortedOptions = [...options].sort((a, b) => b.votes - a.votes);
        return sortedOptions[0];
    }

    const getRemainingTime = (duration, creationTime) => {
        const currentTime = new Date().getTime();
        const creationTimeMilliseconds = new Date(creationTime).getTime();
        const endTime = creationTimeMilliseconds + duration * 60 * 60 * 1000;
        const remainingTime = endTime - currentTime;
        if (remainingTime <= 0) {
            return "0h 0m";
        } else {
            const remainingHours = Math.floor(remainingTime / (60 * 60 * 1000));
            const remainingMinutes = Math.floor((remainingTime % (60 * 60 * 1000)) / (60 * 1000));
            return `${remainingHours}h ${remainingMinutes}m`;
        }
    }

    return (
        <>
            <div className={"overlay"} onClick={onClose}></div>
            <div className="view-polls-container">
                <h2>Active Polls</h2>
                <div className="view-polls">
                    {polls.length === 0 ? (
                        <div>No polls available</div>
                    ) : (
                        polls.map((poll, index) => {
                            const winningOption = getWinningOption(poll.options);
                            const remainingTime = getRemainingTime(poll.duration, poll.created_at);
                            return (
                                <div key={index} className="polls-container">
                                    <div  className="view-poll"
                                          onClick={() => {
                                              setIsViewingPoll(true)
                                              setSelectedPollId(poll.id)
                                          }}>
                                        <h3>{poll.title}</h3>
                                        <p>{remainingTime} remaining</p>
                                        <div className="winningVoteContainer">
                                            <img src={trophy} alt="WinningVote" className="view-trophy-img"/>
                                            <p>{winningOption.title} with {winningOption.votes} votes</p>
                                        </div>
                                        {isOrganizer && <img src={trashBin} alt="Delete poll"
                                                             onClick={(event) => {
                                                                 event.stopPropagation();
                                                                 handleDeletePoll(poll.id)
                                                             }} className="view-trash-img"/>}
                                    </div>
                                    {isViewingPoll && <ViewPoll
                                        onClose={() => {
                                            setIsViewingPoll(false)
                                            setUpdate(!update)
                                        }}
                                        eventId={eventId}
                                        pollId={selectedPollId}
                                    />}
                                </div>
                            )
                        })
                    )}
                </div>
            </div>
        </>
    )
}