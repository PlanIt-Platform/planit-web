package project.planItAPI.http.controllers

import com.google.gson.Gson
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import project.planItAPI.utils.Either
import project.planItAPI.models.ExceptionReturn
import project.planItAPI.utils.HTTPCodeException
import java.time.Duration

/**
 * Generic response handler method to construct a ResponseEntity.
 *
 * @param status The HTTP status code.
 * @param body The response body.
 * @return ResponseEntity with the specified status and response body.
 */
inline fun <reified T> responseHandler(status: Int, body: T) =
    ResponseEntity.status(status)
        .contentType(MediaType.APPLICATION_JSON).body(turnToJson(body))


/**
 * Helper method to set access and refresh token cookies in the HTTP response.
 *
 * @param response The HttpServletResponse to set the response headers.
 * @param accessToken The access token to be set as a cookie.
 * @param refreshToken The refresh token to be set as a cookie.
 */
fun setTokenCookies(
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

/**
 * Helper method to construct a response for a failed operation.
 *
 * @param res The result of the operation.
 * @return ResponseEntity with the appropriate status and response body.
 */
fun failureResponse(res: Either.Left<Exception>) =
    if (res.value is HTTPCodeException) {
        responseHandler(res.value.httpCode, ExceptionReturn(res.value.message))
    } else {
        responseHandler(500, ExceptionReturn("Internal server error"))
    }


/**
 * Helper method to convert an object to a JSON string.
 * @param res The object to be converted to JSON.
 */
fun <T> turnToJson(res: T): String? = Gson().toJson(res)