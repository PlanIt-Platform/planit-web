import {get, post, put} from "./custom/useFetch";
import {LOGIN, LOGOUT, REGISTER, USER} from "./navigation/URIS";
import {executeRequestAndRefreshToken} from "./requestUtils";

export async function register({username, name, email, password}) {
    return await executeRequestAndRefreshToken(
        post,
        REGISTER,
        JSON.stringify({username, name, email, password})
    )
}

export async function login(emailOrName, password) {
    return await executeRequestAndRefreshToken(
        post,
        LOGIN,
        JSON.stringify({emailOrName, password})
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
    return await executeRequestAndRefreshToken(
        put,
        USER,
        JSON.stringify({name, interests, description})
    )
}
