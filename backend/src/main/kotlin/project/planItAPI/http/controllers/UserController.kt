package project.planItAPI.http.controllers

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import project.planItAPI.http.PathTemplates
import project.planItAPI.utils.UserRegisterInputModel
import project.planItAPI.services.UsersServices
import project.planItAPI.utils.Failure
import project.planItAPI.utils.Success
import project.planItAPI.utils.UserLoginInputModel
import com.google.gson.Gson as GSon

/**
 * Controller class for handling user-related operations in a RESTful manner.
 *
 * @property usersServices The service responsible for user-related business logic.
 */
@RestController
@RequestMapping(PathTemplates.PREFIX)
class UserController(private val usersServices: UsersServices) {

    /**
     * Handles user registration.
     *
     * @param s The UserRegisterInputModel representing the user registration data.
     * @param response The HttpServletResponse to set the response headers.
     * @return ResponseEntity with the appropriate status and/or the created user tokens.
     */
    @PostMapping(PathTemplates.REGISTER)
    fun register(@RequestBody s: UserRegisterInputModel, response: HttpServletResponse): ResponseEntity<*> {
        return when (val res = usersServices.register(s.name, s.username, s.email,
            s.description, s.interests.joinToString(","), s.password)) {
            is Failure -> {
                failureResponse(res)
            }

            is Success -> {
                setTokenCookies(response, res.value.accessToken, res.value.refreshToken)
                return responseHandler(201, res.value)
            }
        }
    }

    /**
     * Handles user login.
     *
     * @param s The UserLoginInputModel representing the user login data.
     * @param response The HttpServletResponse to set the response headers.
     * @return ResponseEntity with the appropriate status and/or the new user tokens.
     */
    @PostMapping(PathTemplates.LOGIN)
    fun login(@RequestBody s: UserLoginInputModel, response: HttpServletResponse): ResponseEntity<*> {
        return when (val res = usersServices.login(s.emailOrName, s.password)) {
            is Failure -> {
                failureResponse(res)
            }

            is Success -> {
                setTokenCookies(response, res.value.accessToken, res.value.refreshToken)
                return responseHandler(200, res.value)
            }
        }
    }

    /**
     * Handles user logout.
     *
     * @return ResponseEntity with the appropriate status and response body.
     */
    @PostMapping(PathTemplates.LOGOUT)
    fun logout(
        @CookieValue("access_token", required = true) accessToken: String,
        @CookieValue("refresh_token", required = true) refreshToken: String
    ): ResponseEntity<*> {
        return when (val res = usersServices.logout(accessToken, refreshToken)) {
            is Failure -> {
                responseHandler(401, res.value.message)
            }

            is Success -> {
                val responseBody = mapOf("message" to "Logged out.")
                ResponseEntity.status(200)
                    .header(HttpHeaders.SET_COOKIE, "access_token=; Max-Age=0; Path=/; HttpOnly; SameSite=Strict")
                    .header(HttpHeaders.SET_COOKIE, "refresh_token=; Max-Age=0; Path=/; HttpOnly; SameSite=Strict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseBody)
            }
        }
    }

    @GetMapping(PathTemplates.USER)
    fun getUser(@PathVariable id: Int): ResponseEntity<*> {
        return when (val res = usersServices.getUser(id)) {
            is Failure -> {
                failureResponse(res)
            }

            is Success -> {
                return responseHandler(200, res.value)
            }
        }
    }

   /* @PostMapping(PathTemplates.UPLOAD_PROFILE_PICTURE)
    fun uploadProfilePicture(@PathVariable id: Int, @RequestParam("image") image: MultipartFile?): ResponseEntity<*> {
        if(image != null){
            return when (val res = usersServices.uploadProfilePicture(id, image)) {
                is Failure -> {
                    failureResponse(res)
                }

                is Success -> {
                    return responseHandler(200, res)
                }
            }
        } else {
            return responseHandler(400, "No image provided.")
        }
    }*/

    /**
     * Retrieves information about the application or service.
     *
     * @return Information about the application.
     */
    @GetMapping(PathTemplates.ABOUT)
    fun about() = responseHandler(200, usersServices.about())

}