package project.planItAPI.utils

import org.springframework.web.multipart.MultipartFile
import java.time.Instant

/**
 * Input model for user registration.
 *
 * @property name The name of the user.
 * @property email The email address of the user.
 * @property password The password chosen by the user.
 */
data class UserRegisterInputModel(
    val username: String,
    val name: String,
    val email: String,
    val password: String,
)

/**
 * Output model for successful user registration.
 *
 * @property id The unique identifier assigned to the user.
 * @property username The username of the registered user.
 * @property refreshToken The refresh token associated with the user session.
 * @property accessToken The access token associated with the user session.
 */
data class UserRegisterOutputModel(
    val id: Int,
    val username: String,
    val name: String,
    val refreshToken: String,
    val accessToken: String
)

/**
 * Input model for user login.
 *
 * @property emailOrName The email address or username of the user.
 * @property password The password provided by the user for login.
 */
data class UserLoginInputModel(
    val emailOrName: String,
    val password: String,
)

/**
 * Model containing user login validation information.
 *
 * @property id The unique identifier of the user.
 * @property username The username of the user.
 * @property hashedPassword The result of hashing the password.
 */
data class UserLogInValidation(
    val id: Int,
    val username: String,
    val hashedPassword: String,
)

/**
 * Output model for successful user login.
 *
 * @property id The unique identifier of the user.
 * @property accessToken The access token associated with the user session.
 * @property refreshToken The refresh token associated with the user session.
 */
data class UserLogInOutputModel(
    val id: Int,
    val accessToken: String,
    val refreshToken: String,
)

/**
 * Model containing the user's information.
 *
 * @property id The unique identifier of the user.
 * @property username The username of the user.
 * @property email The email address of the user.
 */
data class UserInfoRepo(
    val id: Int,
    val name: String,
    val username: String,
    val description: String,
    val profilePicture: ByteArray,
    val profilePictureType: String
)

/**
 * Model containing the user's information.
 *
 * @property id The unique identifier of the user.
 * @property username The username of the user.
 * @property email The email address of the user.
 */
data class UserInfo(
    val id: Int,
    val name: String,
    val username: String,
    val description: String,
    val profilePicture: MultipartFile
)

/**
 * Model containing information about a refresh token.
 *
 * @property token_validation The result of the hashing of the refresh token.
 * @property expiration_date The expiration date of the refresh token.
 */
data class RefreshTokenInfo(
    val token_validation: String,
    val expiration_date: Instant
)

/**
 * Model for returning the access and refresh tokens.
 *
 * @property accessToken The access token.
 * @property refreshToken The refresh token.
 */
data class AccessRefreshTokensModel(
    val accessToken: String,
    val refreshToken: String
)

/**
 * Model containing system information.
 *
 * @property name The name of the application.
 * @property version The version of the application.
 * @property contributors The list of contributors to the application.
 */
data class SystemInfo(
    val name: String,
    val version: String,
    val contributors: List<String>,
)

/**
 * Model for returning an exception message.
 *
 * @property error The error message.
 */
data class ExceptionReturn(
    val error: String
)

data class SuccessMessage(
    val success: String
)