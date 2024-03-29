package project.planItAPI.repository

import org.springframework.stereotype.Component
import project.planItAPI.utils.RefreshTokenInfo
import project.planItAPI.utils.UserLogInValidation
import java.sql.Timestamp


/**
 * Repository interface for managing user-related data.
 */
@Component
interface UsersRepository {

    /**
     * Registers a new user with the provided information.
     *
     * @param name The username of the new user.
     * @param email The email address of the new user.
     * @param password The hashed password of the new user.
     * @return The ID of the newly registered user, or null if registration fails.
     */
    fun register(name: String, email: String, password: String): Int?


    /**
     * Checks if a username already exists in the repository.
     *
     * @param name The username to check for existence.
     * @return True if the username exists, false otherwise.
     */
    fun existsByUsername(name: String): Boolean


    /**
     * Retrieves user information for login validation based on the provided email.
     *
     * @param email The email address for login validation.
     * @return [UserLogInValidation] containing user information for login validation, or null if not found.
     */
    fun getUserByEmail(email: String): UserLogInValidation?


    /**
     * Retrieves user information for login validation based on the provided username.
     *
     * @param name The username for login validation.
     * @return [UserLogInValidation] containing user information for login validation, or null if not found.
     */
    fun getUserByUsername(name: String): UserLogInValidation?


    /**
     * Retrieves the user ID associated with the given username.
     *
     * @param name The username to retrieve the ID for.
     * @return The user ID associated with the username, or null if not found.
     */
    fun getUserIDByName(name: String): Int?


    /**
     * Inserts a refresh token for a user.
     *
     * @param id The ID of the user.
     * @param token The refresh token to be inserted.
     * @param expirationDate The expiration date of the refresh token.
     */
    fun insertRefreshToken(id: Int, token: String, expirationDate: Timestamp)


    /**
     * Retrieves a list of refresh tokens associated with a user.
     *
     * @param userID The ID of the user to retrieve refresh tokens for.
     * @return List of [RefreshTokenInfo] containing refresh token information.
     */
    fun getUserRefreshTokens(userID: Int): List<RefreshTokenInfo>


    /**
     * Deletes a specific refresh token associated with a user.
     *
     * @param userID The ID of the user.
     * @param hashedToken The hashed refresh token to be deleted.
     */
    fun deleteUserRefreshToken(userID: Int, hashedToken: String)


    /**
     * Retrieves the user ID associated with the given refresh token.
     *
     * @param refreshToken The refresh token to retrieve the user ID for.
     * @return The user ID associated with the refresh token, or null if not found.
     */
    fun getUserIDByToken(refreshToken: String): Int?

}