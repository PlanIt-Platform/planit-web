import {post, get} from "./custom/useFetch";
import {CATEGORIES, CREATE_EVENT, GET_EVENT, SEARCH_EVENTS} from "./navigation/URIS";
import {executeRequestAndRefreshToken} from "./requestUtils";


export async function createEvent({
        title,
        description,
        category,
        subCategory,
        location,
        visibility,
        date,
        endDate,
        price,
        password
    }) {
    return await executeRequestAndRefreshToken(
        post,
        CREATE_EVENT,
        JSON.stringify({
            title,
            description: description || null,
            category,
            subCategory: subCategory || null,
            location: location || null,
            visibility,
            date,
            endDate: endDate || null,
            price: price || null,
            password: password
        }))
}

export async function getEvent(eventId) {
    return await executeRequestAndRefreshToken(get, GET_EVENT + eventId)
}

export async function searchEvents(searchInput) {
    return await executeRequestAndRefreshToken(get, SEARCH_EVENTS + '?searchInput=' + searchInput)
}

export async function getCategories() {
    return await executeRequestAndRefreshToken(get, CATEGORIES)
}
