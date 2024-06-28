package project.planItAPI.repository.jdbi.user

import org.springframework.stereotype.Component
import project.planItAPI.models.RefreshTokenInfo
import project.planItAPI.models.RoleOutputModel
import project.planItAPI.models.SearchEventsOutputModel
import project.planItAPI.models.UserInfoRepo
import project.planItAPI.models.UserLogInValidation
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
     * @param username The username of the new user.
     * @param email The email address of the new user.
     * @param hashed_password The hashed password of the new user.
     * @return The ID of the newly registered user, or null if registration fails.
     */
    fun register(
        name: String,
        username: String,
        email: String,
        hashed_password: String): Int?

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
     * @param username The username for login validation.
     * @return [UserLogInValidation] containing user information for login validation, or null if not found.
     */
    fun getUserByUsername(username: String): UserLogInValidation?


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

    /**
     * Retrieves user information based on the provided user ID.
     *
     * @param id The user ID to retrieve information for.
     * @return [UserInfoRepo] containing user information, or null if not found.
     */
    fun getUser(id: Int): UserInfoRepo?

    /**
     * Retrieves the events associated with the user.
     * @param id The user ID to retrieve events for.
     * @return List of [SearchEventsOutputModel] containing event information.
     */
    fun getUserEvents(id: Int): List<SearchEventsOutputModel>

    /**
     * Updates user information based on the provided user ID.
     * @param id The user ID to update information for.
     * @param name The new name of the user.
     * @param description The new description of the user.
     * @param interests The new interests of the user.
     * @return The ID of the updated user, or null if update fails.
     */
    fun editUser(id: Int, name: String, description: String, interests: String)

    /**
     * Assigns a role to a user.
     * @param userId The ID of the user to assign the role to.
     * @param roleName The name of the role.
     * @param eventId The ID of the event the role will belong to.
     * @return The ID of the assigned role, or null if assignment fails.
     */
    fun assignRole(userId: Int, roleName: String, eventId: Int): Int?

    /**
     * Removes a role from an event.
     * @param roleId The ID of the role to remove.
     */
    fun removeRole(roleId: Int)


    /**
     * Retrieves the role associated with the user and event.
     * @param userId The ID of the user to retrieve the role for.
     * @param eventId The ID of the event to retrieve the role for.
     * @return [RoleOutputModel] containing role information, or null if not found.
     */
    fun getUserRole(userId: Int, eventId: Int): RoleOutputModel?

    // fun uploadProfilePicture(id: Int, picture: ByteArray, fileType: String): Int?
}