async function fetchData(uri, method, body) {
    const options: RequestInit = {
        credentials: 'include',
        method: method,
        headers: {
            'Content-Type': 'application/json',
        },
        body: body
    }
    const response = await fetch(uri, options)
    return await response.json();
}

export function post(uri, body) {
    return fetchData(uri, 'POST', body)
}

export function get(uri){
    return fetchData(uri, 'GET', undefined)
}