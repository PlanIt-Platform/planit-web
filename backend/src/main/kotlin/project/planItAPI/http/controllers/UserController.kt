package project.planItAPI.http.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import project.planItAPI.http.PathTemplates
import project.planItAPI.http.PathTemplates.EDIT_USER
import project.planItAPI.http.PathTemplates.USER
import project.planItAPI.utils.UserRegisterInputModel
import project.planItAPI.services.UsersServices
import project.planItAPI.utils.Failure
import project.planItAPI.utils.Success
import project.planItAPI.utils.UserEditModel
import project.planItAPI.utils.UserLoginInputModel

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
    fun register(@RequestBody s: UserRegisterInputModel, response: HttpServletResponse, request: HttpServletRequest): ResponseEntity<*> {
        return when (val res = usersServices.register(s.name, s.username, s.email, s.password)) {
            is Failure -> {
                failureResponse(res)
            }

            is Success -> {
                setTokenCookies(response, res.value.accessToken, res.value.refreshToken)
                request.setAttribute("userId", res.value.id)
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
    fun login(@RequestBody s: UserLoginInputModel, response: HttpServletResponse, request: HttpServletRequest): ResponseEntity<*> {
        return when (val res = usersServices.login(s.emailOrName, s.password)) {
            is Failure -> {
                failureResponse(res)
            }

            is Success -> {
                setTokenCookies(response, res.value.accessToken, res.value.refreshToken)
                request.setAttribute("userId", res.value.id)
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

    /**
     * Retrieves information about a user.
     * @param pathId The unique identifier of the user.
     * @return ResponseEntity with the appropriate status and user information.
     *
     */
    @GetMapping(USER)
    fun getUser(@PathVariable pathId: Int): ResponseEntity<*> {
        return when (val res = usersServices.getUser(pathId)) {
            is Failure -> {
                failureResponse(res)
            }

            is Success -> {
                return responseHandler(200, res.value)
            }
        }
    }

    /**
     * Edits a user's information.
     * @param pathId The unique identifier of the user.
     * @param s The UserEditModel representing the user's new information.
     */
    @PutMapping(EDIT_USER)
    fun editUser(@PathVariable pathId: Int, @RequestBody s: UserEditModel, @RequestAttribute("userId") userId: String): ResponseEntity<*> {
        return when (
            val res = usersServices.editUser(
                pathId,
                userId.toInt(),
                s.name,
                s.description,
                s.interests.joinToString(",")
            )
        ) {
            is Failure -> {
                failureResponse(res)
            }

            is Success -> {
                return responseHandler(200, res)
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