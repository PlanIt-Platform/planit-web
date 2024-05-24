import {post} from "./custom/useFetch";
import {REFRESH_TOKEN} from "./navigation/URIS";

export async function executeRequestAndRefreshToken(requestFunction, ...args) {
    try {
        const response = await requestFunction(...args)
        if (response.status === 401) {
            // Token expired, refresh it and retry the request
            const tokenResponse = await refreshToken()
            if (tokenResponse.status != 201 && tokenResponse.status != 500) {
                localStorage.removeItem('user_id');
            }
            return requestFunction(...args)
        }
        return response
    } catch (error) {
        throw error
    }
}

export async function refreshToken() {
    return await post(REFRESH_TOKEN, undefined)
}
