import {get, post} from "./custom/useFetch";
import {LOGIN, LOGOUT, REGISTER} from "./navigation/URIS";

export async function register(name, email, password) {
    return await post(REGISTER, JSON.stringify({name, email, password}))
}

export async function login(email, password) {
    return await post(LOGIN, JSON.stringify({email, password}))
}

export async function logout() {
    return await post(LOGOUT, undefined)
}

