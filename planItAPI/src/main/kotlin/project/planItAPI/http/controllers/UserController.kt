package project.planItAPI.http.controllers

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import project.planItAPI.http.PathTemplates
import project.planItAPI.utils.UserRegisterInputModel
import project.planItAPI.services.UsersServices
import project.planItAPI.utils.Either
import project.planItAPI.utils.ExceptionReturn
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.Success
import project.planItAPI.utils.UserLoginInputModel
import java.time.Duration
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
        return when (val res = usersServices.register(s.name, s.email, s.password)) {
            is Failure -> {
                failureResponse(res)
            }

            is Success -> {
                setTokenCookies(response, res.value.accessToken, res.value.refreshToken)
                return responseHandler(201, GSon().toJson(res.value))
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
                return responseHandler(200, GSon().toJson(res.value))
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
        @RequestAttribute("access_token", required = true) accessToken: String,
        @RequestAttribute("refresh_token", required = true) refreshToken: String
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
     * Retrieves information about the application or service.
     *
     * @return Information about the application.
     */
    @GetMapping(PathTemplates.ABOUT)
    fun about() = responseHandler(200, GSon().toJson(usersServices.about()))

    /**
     * Generic response handler method to construct a ResponseEntity.
     *
     * @param status The HTTP status code.
     * @param body The response body.
     * @return ResponseEntity with the specified status and response body.
     */
    private inline fun <reified T> responseHandler(status: Int, body: T) =
        ResponseEntity.status(status)
            .contentType(MediaType.APPLICATION_JSON).body(body)


    /**
     * Helper method to set access and refresh token cookies in the HTTP response.
     *
     * @param response The HttpServletResponse to set the response headers.
     * @param accessToken The access token to be set as a cookie.
     * @param refreshToken The refresh token to be set as a cookie.
     */
    private fun setTokenCookies(
        response: HttpServletResponse,
        accessToken: String,
        refreshToken: String,
    ) {
        val accessTokenCookie = ResponseCookie.from("access_token", accessToken)
            .httpOnly(true)
            .path("/")
            .maxAge(Duration.ofHours(1))
            .sameSite("Strict")
            .build()

        val refreshTokenCookie = ResponseCookie.from("refresh_token", refreshToken)
            .httpOnly(true)
            .path("/")
            .maxAge(Duration.ofDays(1))
            .sameSite("Strict")
            .build()

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
    }


    private fun failureResponse(res: Either.Left<Exception>) =
        if (res.value is HTTPCodeException) {
            responseHandler(res.value.httpCode, GSon().toJson(ExceptionReturn(res.value.message)))
        } else {
            responseHandler(500, GSon().toJson(ExceptionReturn("Internal server error")))
        }

}