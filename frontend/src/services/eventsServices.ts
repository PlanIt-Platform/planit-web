import {post, get, put, del} from "./custom/useFetch";
import {CATEGORIES, CREATE_EVENT, GET_EVENT, SEARCH_EVENTS} from "./navigation/URIS";
import {executeRequestAndRefreshToken} from "./requestUtils";

export async function createEvent({
        title,
        description,
        category,
        locationType,
        location,
        latitude,
        longitude,
        visibility,
        date,
        endDate,
        price,
        currency,
        password
    }) {
    if (visibility == 'Public') password = ""
    if (locationType == "None") locationType = null
    return await executeRequestAndRefreshToken(
        post,
        CREATE_EVENT,
        JSON.stringify({
            title,
            description: description || null,
            category,
            locationType: locationType,
            location: location || null,
            latitude,
            longitude,
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
    locationType,
    location,
    latitude,
    longitude,
    visibility,
    date,
    endDate,
    priceAmount,
    priceCurrency,
    password
}) {
    if (visibility == 'Public') password = ""
    if (locationType == "None") locationType = null
    return await executeRequestAndRefreshToken(
        put,
        GET_EVENT + eventId + '/edit',
        JSON.stringify({
            title,
            description: description || null,
            category,
            locationType: locationType,
            location: location || null,
            latitude,
            longitude,
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

export async function searchEvents(searchInput, limit = 10, offset = 0) {
    let formattedSearchInput = searchInput.replace(" ", '+');
    return await executeRequestAndRefreshToken(
        get,
        SEARCH_EVENTS + '?searchInput=' + formattedSearchInput + '&limit='+ limit + '&offset=' + offset)
}

export async function findNearbyEvents(latitude, longitude, radius, limit) {
    return await executeRequestAndRefreshToken(
        get,
        SEARCH_EVENTS + '/' + radius + '/' + latitude + '/' + longitude + '?limit=' + limit)
}

export async function getCategories() {
    return await executeRequestAndRefreshToken(get, CATEGORIES)
}

export async function getUsersInEvent(eventId) {
    return await executeRequestAndRefreshToken(get, GET_EVENT + eventId + '/users')
}

export async function joinEvent(eventId, password) {
    return await executeRequestAndRefreshToken(post, GET_EVENT + eventId + '/join', JSON.stringify({password}))
}

export async function joinEventWithCode(code) {
    return await executeRequestAndRefreshToken(post, GET_EVENT + code)
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