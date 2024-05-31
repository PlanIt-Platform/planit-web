import {executeRequestAndRefreshToken} from "./requestUtils";
import {del, post, put, get} from "./custom/useFetch";
import {GET_EVENT} from "./navigation/URIS";

export async function createPoll(eventId, {
    title,
    options,
    duration
}) {
    return await executeRequestAndRefreshToken(
        post,
        GET_EVENT + eventId + '/poll',
        JSON.stringify({
            title,
            options,
            duration
        }))
}

export async function votePoll(eventId, pollId, optionId) {
    return await executeRequestAndRefreshToken(
        put,
        GET_EVENT + eventId + '/poll/' + pollId + '/vote/' + optionId
    )
}

export async function getPoll(eventId, pollId) {
    return await executeRequestAndRefreshToken(
        get,
        GET_EVENT + eventId + '/poll/' + pollId
    )
}

export async function deletePoll(eventId, pollId) {
    return await executeRequestAndRefreshToken(
        del,
        GET_EVENT + eventId + '/poll/' + pollId
    )
}

export async function getPolls(eventId) {
    return await executeRequestAndRefreshToken(
        get,
        GET_EVENT + eventId + '/polls'
    )
}
