package project.planItAPI.http

object PathTemplates {
    const val PREFIX = "/api-planit"

    /**
     * User paths
     */
    const val REGISTER = "/register"
    const val LOGIN = "/login"
    const val LOGOUT = "/logout"
    const val USER = "/user/{id}"
    //const val USER_BY_TOKEN = "/user/token"
    const val ABOUT = "/about"
    //const val UPLOAD_PROFILE_PICTURE = "/user/{id}/upload-profile-picture"


    /**
     * Event paths
     */
    const val CREATE_EVENT = "/event"
    const val GET_EVENT = "/event/{id}"
    const val USERS_IN_EVENT = "/event/{id}/users"
}
