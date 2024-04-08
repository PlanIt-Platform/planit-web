package project.planItAPI.http

object PathTemplates {
    const val PREFIX = "/api-planit"

    // USER
    const val REGISTER = "/register"
    const val LOGIN = "/login"
    const val LOGOUT = "/logout"
    const val USER = "/user/{id}"
    const val EDIT_USER = "/user/{id}/edit"
    const val ABOUT = "/about"
    const val UPLOAD_PROFILE_PICTURE = "/user/{id}/upload-profile-picture"
}
