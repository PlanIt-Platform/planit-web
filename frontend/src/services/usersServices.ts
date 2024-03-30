import {get, post} from "./custom/useFetch";
import {GET_USER, LOGIN, LOGOUT, REGISTER} from "./navigation/URIS";

export async function register(username, name, email, password, interests, description) {
    return await post(REGISTER, JSON.stringify({username, name, email, password, interests, description}))
}

export async function login(emailOrName, password) {
    return await post(LOGIN, JSON.stringify({emailOrName, password}))
}

export async function logout() {
    return await post(LOGOUT, undefined)
}

export async function getUser(userId) {
    return await get(GET_USER + userId)
}
