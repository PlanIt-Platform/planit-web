import {get, post, put, del} from "./custom/useFetch";
import {FEEDBACK, LOGIN, LOGOUT, REGISTER, USER} from "./navigation/URIS";
import {executeRequestAndRefreshToken} from "./requestUtils";

export async function register({username, name, email, password}) {
    return await executeRequestAndRefreshToken(
        post,
        REGISTER,
        JSON.stringify({username, name, email, password})
    )
}

export async function login(emailOrUsername, password) {
    return await executeRequestAndRefreshToken(
        post,
        LOGIN,
        JSON.stringify({emailOrUsername: emailOrUsername, password})
    )
}

export async function logout() {
    return await executeRequestAndRefreshToken(
        post,
        LOGOUT,
        undefined
    )
}

export async function getUser(userId) {
    return await executeRequestAndRefreshToken(
        get,
        USER + '/' + userId,
    )
}

export async function editUser(name, description, interests) {
    if (interests )
    return await executeRequestAndRefreshToken(
        put,
        USER,
        JSON.stringify({name, interests, description})
    )
}

export async function getUserEvents() {
    return await executeRequestAndRefreshToken(
        get,
        USER + '/events',
    )
}

export async function assignRole(userId, eventId, {roleName}) {
    return await executeRequestAndRefreshToken(
        post,
        USER + '/' + userId +  '/event/' + eventId + '/role',
        JSON.stringify({roleName})
    )
}

export async function removeRole(userId, roleId, eventId) {
    return await executeRequestAndRefreshToken(
        del,
        USER + '/' + userId +  '/event/' + eventId + '/role/' + roleId
    )
}

export async function getUserRole(userId, eventId) {
    return await executeRequestAndRefreshToken(
        get,
        USER + '/' + userId + '/event/' + eventId + '/role'
    )
}

export async function sendFeedback(text) {
    return await executeRequestAndRefreshToken(
        post,
        FEEDBACK,
        JSON.stringify(text)
    )
}
