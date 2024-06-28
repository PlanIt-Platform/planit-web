package project.planItAPI.http

object PathTemplates {
    const val PREFIX = "/api-planit"

    /**
     * User paths
     */
    const val REGISTER = "/register"
    const val LOGIN = "/login"
    const val LOGOUT = "/logout"
    const val USER = "/user/{pathId}"
    const val EDIT_USER = "/user"
    const val USER_EVENTS = "/user/events"
    const val ABOUT = "/about"
    const val REFRESH_TOKEN = "/refresh-token"
    const val ROLE = "/user/{userId}/event/{eventId}/role"
    const val REMOVE_ROLE = "/user/{userId}/event/{eventId}/role/{roleId}"
    //const val UPLOAD_PROFILE_PICTURE = "/user/{id}/upload-profile-picture"

    /**
     * Event paths
     */
    const val CREATE_EVENT = "/event"
    const val GET_EVENT = "/event/{id}"
    const val USERS_IN_EVENT = "/event/{id}/users"
    const val EDIT_EVENT = "/event/{eventId}/edit"
    const val DELETE_EVENT = "/event/{eventId}"
    const val JOIN_EVENT = "/event/{eventId}/join"
    const val JOIN_EVENT_WITH_CODE = "/event/{code}"
    const val LEAVE_EVENT = "/event/{eventId}/leave"
    const val SEARCH_EVENTS = "/events"
    const val CATEGORIES = "/event/categories"
    const val SUBCATEGORIES = "/event/categories/{category}/subcategories"
    const val KICK_USER = "/event/{eventId}/kick/{userId}"

    /**
     * Poll paths
     */
    const val CREATE_POLL = "/event/{eventId}/poll"
    const val GET_POLLS = "/event/{eventId}/polls"
    const val POLL = "/event/{eventId}/poll/{pollId}"
    const val VOTE_POLL = "/event/{eventId}/poll/{pollId}/vote/{optionId}"

}
