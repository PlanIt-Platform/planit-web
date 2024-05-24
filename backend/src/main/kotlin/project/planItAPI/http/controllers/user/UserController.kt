package project.planItAPI.http.controllers.user

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import project.planItAPI.domain.Id
import project.planItAPI.http.PathTemplates
import project.planItAPI.http.PathTemplates.EDIT_USER
import project.planItAPI.http.PathTemplates.REFRESH_TOKEN
import project.planItAPI.http.PathTemplates.USER
import project.planItAPI.http.controllers.failureResponse
import project.planItAPI.http.controllers.responseHandler
import project.planItAPI.http.controllers.setTokenCookies
import project.planItAPI.models.UserRegisterInputModel
import project.planItAPI.services.user.UserServices
import project.planItAPI.utils.Failure
import project.planItAPI.utils.Success
import project.planItAPI.models.UserEditModel
import project.planItAPI.models.UserLoginInputModel

/**
 * Controller class for handling user-related operations in a RESTful manner.
 *
 * @property userServices The service responsible for user-related business logic.
 */
@RestController
@RequestMapping(PathTemplates.PREFIX)
class UserController(private val userServices: UserServices) {

    /**
     * Handles user registration.
     *
     * @param input The UserRegisterInputModel representing the user registration data.
     * @param response The HttpServletResponse to set the response headers.
     * @return ResponseEntity with the appropriate status and/or the created user tokens.
     */
    @PostMapping(PathTemplates.REGISTER)
    fun register(@RequestBody input: UserRegisterInputModel, response: HttpServletResponse, request: HttpServletRequest): ResponseEntity<*> {
        return when (val validation = validateUserRegisterInput(input)) {
            is Failure -> failureResponse(validation)
            is Success -> {
                val validatedInputs = validation.value
                when (val res = userServices.register(
                    validatedInputs.name,
                    validatedInputs.username,
                    validatedInputs.email,
                    validatedInputs.password
                )) {
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
        }
    }

    /**
     * Handles user login.
     *
     * @param input The UserLoginInputModel representing the user login data.
     * @param response The HttpServletResponse to set the response headers.
     * @return ResponseEntity with the appropriate status and/or the new user tokens.
     */
    @PostMapping(PathTemplates.LOGIN)
    fun login(@RequestBody input: UserLoginInputModel, response: HttpServletResponse, request: HttpServletRequest): ResponseEntity<*> {
        return when (val validation = validateUserLoginInput(input)) {
            is Failure -> failureResponse(validation)
            is Success -> {
                val validatedInputs = validation.value
                when (val res = userServices.login(
                    validatedInputs.emailOrName,
                    validatedInputs.password
                )) {
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
        return when (val res = userServices.logout(accessToken, refreshToken)) {
            is Failure -> {
                responseHandler(401, res)
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
        return when (val idResult = Id(pathId)) {
            is Failure -> failureResponse(idResult)
            is Success -> {
                when (val res = userServices.getUser(pathId)) {
                    is Failure -> {
                        failureResponse(res)
                    }

                    is Success -> {
                        return responseHandler(200, res.value)
                    }
                }
            }
        }
    }

    /**
     * Edits a user's information.
     * @param userId The unique identifier of the user.
     * @param input The UserEditModel representing the user's new information.
     */
    @PutMapping(EDIT_USER)
    fun editUser(@RequestBody input: UserEditModel, @RequestAttribute("userId") userId: String): ResponseEntity<*> {
        return when (val validation = validateUserEditInput(input)) {
            is Failure -> failureResponse(validation)
            is Success -> {
                val validatedInputs = validation.value
                when (
                    val res = userServices.editUser(
                        userId.toInt(),
                        validatedInputs.name,
                        validatedInputs.description,
                        validatedInputs.interests
                    )
                ) {
                    is Failure -> {
                        failureResponse(res)
                    }

                    is Success -> {
                        return responseHandler(200, res.value)
                    }
                }
            }
        }
    }

    /**
     * Handles refreshing the access token using the refresh token.
     *
     * @param refreshToken The refresh token obtained from the cookies.
     * @param response The HttpServletResponse to set the response headers.
     * @return ResponseEntity with the appropriate status and/or the new user tokens.
     */
    @PostMapping(REFRESH_TOKEN)
    fun refreshToken(
        @CookieValue("refresh_token", required = true) refreshToken: String,
        response: HttpServletResponse,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        return when (val res = userServices.refreshToken(refreshToken)) {
            is Failure -> failureResponse(res)
            is Success -> {
                setTokenCookies(response, res.value.accessToken, res.value.refreshToken)
                request.setAttribute("userId", res.value.userID)
                return responseHandler(201, res.value)
            }
        }
    }

    /* @PostMapping(PathTemplates.UPLOAD_PROFILE_PICTURE)
     fun uploadProfilePicture(@PathVariable id: Int, @RequestParam("image") image: MultipartFile?): ResponseEntity<*> {
         if(image != null){
             return when (val res = userServices.uploadProfilePicture(id, image)) {
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
    fun about() = responseHandler(200, userServices.about())

}