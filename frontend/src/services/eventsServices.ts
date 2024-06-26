import {post, get, put, del} from "./custom/useFetch";
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
        currency,
        password
    }) {
    if (visibility == 'Public') password = ""
    return await executeRequestAndRefreshToken(
        post,
        CREATE_EVENT,
        JSON.stringify({
            title,
            description: description || null,
            category,
            subCategory: subCategory,
            location: location || null,
            visibility,
            date,
            endDate: endDate || null,
            price: price + " " + currency,
            password: password
        }))
}

export async function editEvent(eventId, {
    title,
    description,
    category,
    subCategory,
    location,
    visibility,
    date,
    endDate,
    priceAmount,
    priceCurrency,
    password
}) {
    if (visibility == 'Public') password = ""
    return await executeRequestAndRefreshToken(
        put,
        GET_EVENT + eventId + '/edit',
        JSON.stringify({
            title,
            description: description || null,
            category,
            subCategory: subCategory,
            location: location || null,
            visibility,
            date,
            endDate: endDate || null,
            price: priceAmount + " " + priceCurrency,
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

export async function getSubCategories(category) {
    return await executeRequestAndRefreshToken(get, CATEGORIES + '/' + category + '/subcategories')
}

export async function getUsersInEvent(eventId) {
    return await executeRequestAndRefreshToken(get, GET_EVENT + eventId + '/users')
}

export async function joinEvent(eventId, password) {
    return await executeRequestAndRefreshToken(post, GET_EVENT + eventId + '/join', JSON.stringify({password}))
}

export async function leaveEvent(eventId) {
    return await executeRequestAndRefreshToken(post, GET_EVENT + eventId + '/leave')
}

export async function deleteEvent(eventId) {
    return await executeRequestAndRefreshToken(del, GET_EVENT + eventId)
}

export async function kickUser(eventId, userId) {
    return await executeRequestAndRefreshToken(del, GET_EVENT + eventId + '/kick/' + userId)
}