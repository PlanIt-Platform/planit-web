package project.planItAPI.services.user

import org.springframework.stereotype.Service
import project.planItAPI.domain.event.Category
import project.planItAPI.domain.user.Email
import project.planItAPI.domain.user.EmailOrUsername
import project.planItAPI.domain.user.Name
import project.planItAPI.domain.user.Password
import project.planItAPI.domain.user.Username
import project.planItAPI.models.AccessRefreshTokensModel
import project.planItAPI.models.AssignRoleInputModel
import project.planItAPI.models.UserRegisterOutputModel
import project.planItAPI.repository.jdbi.user.UsersRepository
import project.planItAPI.services.user.utils.UsersDomain
import project.planItAPI.services.user.utils.UsersDomainConfig
import project.planItAPI.repository.transaction.TransactionManager
import project.planItAPI.utils.ExistingEmailException
import project.planItAPI.utils.ExistingUsernameException
import project.planItAPI.utils.IncorrectPasswordException
import project.planItAPI.models.UserInfo
import project.planItAPI.models.UserLogInOutputModel
import project.planItAPI.models.UserLogInValidation
import project.planItAPI.utils.IncorrectLoginException
import project.planItAPI.models.RefreshTokensOutputModel
import project.planItAPI.models.RoleOutputModel
import project.planItAPI.utils.UserNotFoundException
import project.planItAPI.utils.UserRegisterErrorException
import project.planItAPI.models.SuccessMessage
import project.planItAPI.models.UserEventsOutputModel
import project.planItAPI.services.getNowTime
import project.planItAPI.utils.EventHasEndedException
import project.planItAPI.utils.EventNotFoundException
import project.planItAPI.utils.FailedToAssignRoleException
import project.planItAPI.utils.FailedToRemoveRoleException
import project.planItAPI.utils.FeedbackIsBlankException
import project.planItAPI.utils.InvalidRefreshTokenException
import project.planItAPI.utils.OnlyOrganizerException
import project.planItAPI.utils.RoleNotFoundException
import project.planItAPI.utils.UserIsNotOrganizerException
import project.planItAPI.utils.UserNotInEventException
import java.sql.Timestamp

/**
 * Service class providing user-related functionality.
 *
 * @param transactionManager The transaction manager for handling repository interactions.
 * @param domain The domain layer for user-related functionality.
 * @param usersConfig The configuration for user-related functionality.
 */
@Service
class UserServices (
    private val transactionManager: TransactionManager,
    private val domain: UsersDomain,
    private val usersConfig: UsersDomainConfig
) {

    /**
     * Registers a new user.
     *
     * @param name The user's name.
     * @param username The user's username.
     * @param email The user's email.
     * @param password The user's password.
     * @return The result of the user registration as [UserRegisterResult].
     */
    fun register(name: Name, username: Username, email: Email, password: Password): UserRegisterResult {
        return transactionManager.run {
            val usersRepository = it.usersRepository

            if (usersRepository.getUserByEmail(email.value) != null)
                throw ExistingEmailException()

            if (usersRepository.existsByUsername(username.value))
                throw ExistingUsernameException()

            val hashedPassword = domain.createHashedPassword(password.value)
            when (val newUserID = usersRepository.register(
                name.value,
                username.value,
                email.value,
                hashedPassword
            )) {
                is Int -> {
                    val (accessToken, newRefreshToken) = createTokens(newUserID, username.value, usersRepository)
                    return@run UserRegisterOutputModel(newUserID, username.value, name.value, newRefreshToken, accessToken)
                }

                else ->
                    throw UserRegisterErrorException()
            }
        }
    }

    /**
     * Logs in a user.
     *
     * @param emailOrUsername The user's email.
     * @param password The user's password.
     * @return The result of the user login as [UserLoginResult].
     */
    fun login(emailOrUsername: EmailOrUsername, password: Password): UserLoginResult {
        return transactionManager.run {
            val usersRepository = it.usersRepository
            when (emailOrUsername) {
                is EmailOrUsername.EmailType -> {
                    val user = usersRepository.getUserByEmail(emailOrUsername.email.value)
                    if (user != null) {
                        return@run loginValidation(password.value, user, usersRepository)
                    }
                }
                is EmailOrUsername.UsernameType -> {
                    val user = usersRepository.getUserByUsername(emailOrUsername.name.value)
                    if (user != null) {
                        return@run loginValidation(password.value, user, usersRepository)
                    }
                }
            }
            throw IncorrectLoginException()
        }
    }

    // Private helper function to validate login credentials and create tokens for a user.
    private fun loginValidation(
        password: String,
        user: UserLogInValidation,
        usersRepository: UsersRepository
    ): UserLogInOutputModel {
        if (!domain.validatePassword(password, user.hashedPassword)) {
            throw IncorrectPasswordException()
        }
        val (accessToken, newRefreshToken) = createTokens(user.id, user.username, usersRepository)
        return UserLogInOutputModel(user.id, accessToken, newRefreshToken)
    }

    /**
     * Logs out a user.
     *
     * @param accessToken The user's access token.
     * @param refreshToken The user's refresh token.
     * @return The result of the user login as [LogoutResult].
     */
    fun logout(accessToken: String, refreshToken: String): LogoutResult {
        return transactionManager.run {
            val usersRepository = it.usersRepository
            val refreshTokenHash = domain.createTokenValidation(token = refreshToken)
            val userID =
                usersRepository.getUserIDByToken(refreshTokenHash) ?: throw Exception("invalid token")
            usersRepository.deleteUserRefreshToken(userID, refreshTokenHash)
            return@run
        }
    }

    /**
     * Retrieves information about a user.
     *
     * @param userID The ID of the user to retrieve information for.
     * @return The user's information as [GetUserResult].
     */
    fun getUser(userID: Int): GetUserResult {
        return transactionManager.run {
            val usersRepository = it.usersRepository
            val user = usersRepository.getUser(userID) ?: throw UserNotFoundException()
            return@run UserInfo(user.id, user.name, user.username,  user.email,
                user.description, user.interests.split(",").filter { i -> i.isNotBlank() })
        }
    }

    /**
     * Retrieves the events of a user.
     *
     * @param userID The ID of the user to retrieve events for.
     * @return The user's events as [GetUserEventsResult].
     */
    fun getUserEvents(userID: Int): GetUserEventsResult {
        return transactionManager.run {
            val usersRepository = it.usersRepository
            val user = usersRepository.getUser(userID) ?: throw UserNotFoundException()
            val events = usersRepository.getUserEvents(userID)
            return@run UserEventsOutputModel(user.id, user.username, events)
        }
    }

    /**
     * Edits a user's information.
     *
     * @param userID The ID of the user to edit.
     * @param name The new name of the user.
     * @param description The new description of the user.
     * @param interests The new interests of the user.
     * @return The result of the user edit as [EditUserResult].
     */
    fun editUser(userID: Int, name: Name, description: String, interests: List<String>): EditUserResult {
        return transactionManager.run {
            val usersRepository = it.usersRepository
            if (usersRepository.getUser(userID) == null) {
                throw UserNotFoundException()
            }
            usersRepository.editUser(userID, name.value, description, interests.joinToString(","))
            return@run SuccessMessage("User edited successfully.")
        }
    }

    /**
     * Assigns a role to a user.
     *
     * @param userId The ID of the user to assign the role to.
     * @param input The role information.
     * @param organizerId The ID of the event organizer.
     * @return The result of the role assignment as [AssignRoleResult].
     */
    fun assignRole(userId: Int, eventId: Int, input: AssignRoleInputModel, organizerId: Int): AssignRoleResult {
        return transactionManager.run {
            val usersRepository = it.usersRepository
            val eventsRepository = it.eventsRepository
            if (usersRepository.getUser(userId) == null) throw UserNotFoundException()
            val event = eventsRepository.getEvent(eventId) ?: throw EventNotFoundException()
            if (event.endDate != null && event.endDate < getNowTime()) throw EventHasEndedException()
            val usersInEvent = eventsRepository.getUsersInEvent(eventId)
            if (usersInEvent != null && !usersInEvent.users.any { user -> user.id == userId }) throw UserNotInEventException()
            if (organizerId !in eventsRepository.getEventOrganizers(eventId)) throw UserIsNotOrganizerException()
            val role = usersRepository.getUserRole(userId, eventId)
            if (role != null && role.name == input.roleName) throw FailedToAssignRoleException()
            val roleId = usersRepository.assignRole(
                userId,
                input.roleName,
                eventId
            ) ?: throw FailedToAssignRoleException()

            return@run RoleOutputModel(roleId, input.roleName)
        }
    }

    /**
     * Removes a role from a user.
     * @param userId The ID of the user to remove the role from.
     * @param roleId The ID of the role to remove.
     * @param eventId The ID of the event the role belongs to.
     * @param organizerId The ID of the event organizer.
     * @return The result of the role removal as [RemoveRoleResult].
     */
    fun removeRole(userId: Int, roleId: Int, eventId: Int, organizerId: Int): RemoveRoleResult {
        return transactionManager.run {
            val usersRepository = it.usersRepository
            val eventsRepository = it.eventsRepository
            if (usersRepository.getUser(userId) == null) throw UserNotFoundException()
            val event = eventsRepository.getEvent(eventId) ?: throw EventNotFoundException()
            if (event.endDate != null && event.endDate < getNowTime()) throw EventHasEndedException()
            val usersInEvent = eventsRepository.getUsersInEvent(eventId)
            if (usersInEvent != null && !usersInEvent.users.any { user -> user.id == userId }) throw UserNotInEventException()
            if (organizerId !in eventsRepository.getEventOrganizers(eventId)) throw UserIsNotOrganizerException()
            if (usersRepository.getUserRole(userId, eventId) == null) throw RoleNotFoundException()
            val eventOrganizers = eventsRepository.getEventOrganizers(eventId)
            if (eventOrganizers.size == 1 && eventOrganizers.contains(userId)) throw OnlyOrganizerException()
            val role = usersRepository.getUserRole(userId, eventId)
            if (role != null && role.name == "Participant") throw FailedToRemoveRoleException()
            usersRepository.removeRole(roleId)
            return@run SuccessMessage("Role removed successfully.")
        }
    }

    /**
     * Retrieves a user's role in a given event.
     * @param userId The ID of the user to retrieve the role for.
     * @param eventId The ID of the event the role belongs to.
     * @return The user's role as [RoleOutputModel].
     */
    fun getUserRole(userId: Int, eventId: Int): GetUserRoleResult {
        return transactionManager.run {
            val usersRepository = it.usersRepository
            val eventsRepository = it.eventsRepository
            if (usersRepository.getUser(userId) == null) throw UserNotFoundException()
            eventsRepository.getEvent(eventId) ?: throw EventNotFoundException()
            val usersInEvent = eventsRepository.getUsersInEvent(eventId)
            if (usersInEvent != null && !usersInEvent.users.any { user -> user.id == userId }) throw UserNotInEventException()
            val role = usersRepository.getUserRole(userId, eventId) ?: throw RoleNotFoundException()
            return@run RoleOutputModel(role.id, role.name)
        }
    }

    /**
     * Sends feedback to the application developers.
     * @param feedback The feedback to send.
     * @return The result of the feedback submission as [SuccessMessage].
     */
    fun sendFeedback(feedback: String): SendFeedbackResult {
        return transactionManager.run {
            val usersRepository = it.usersRepository
            if (feedback.isBlank()) throw FeedbackIsBlankException()
            val date = getNowTime()
            usersRepository.sendFeedback(feedback, Timestamp.valueOf("${date}:00"),)
            return@run SuccessMessage("Feedback sent successfully.")
        }
    }

    /**
     * Retrieves feedback to the application developers.
     * @return The feedback as [GetFeedbackResult].
     */
    fun getFeedback(): GetFeedbackResult {
        return transactionManager.run {
            val usersRepository = it.usersRepository
            return@run usersRepository.getFeedback()
        }
    }

    /**
     * Refreshes the user's access and refresh tokens.
     *
     * @param refreshToken The refresh token.
     * @return The result of the token refresh as [UserTokensResult].
     */
    fun refreshToken(refreshToken: String): UserTokensResult {
        return transactionManager.run {
            val refreshTokenHash = domain.createTokenValidation(token = refreshToken)
            val usersRepository = it.usersRepository
            if (!domain.checkRefreshToken(refreshToken)) throw InvalidRefreshTokenException()
            val userID = usersRepository.getUserIDByToken(refreshTokenHash)
                ?: throw InvalidRefreshTokenException()
            val user = usersRepository.getUser(userID) ?: throw InvalidRefreshTokenException()

            usersRepository.deleteUserRefreshToken(userID, refreshTokenHash)

            val (accessToken, newRefreshToken) = createTokens(userID, user.username, usersRepository)
            return@run RefreshTokensOutputModel(userID, accessToken, newRefreshToken)
        }
    }

    /**
     * Updates the user's profile picture.
     *
     * @param userID The ID of the user to update the profile picture for.
     * @param profilePicture The new profile picture.
     * @return The user's information as [UploadProfilePictureResult].
     */
    /* fun uploadProfilePicture(userID: Int, profilePicture: MultipartFile): UploadProfilePictureResult {
         return transactionManager.run {
             val usersRepository = it.usersRepository
             val imageBytes = profilePicture.bytes ?: throw UnsupportedMediaTypeException()
             val fileType = profilePicture.contentType ?: throw UnsupportedMediaTypeException()
             usersRepository.uploadProfilePicture(userID, imageBytes, fileType) ?: throw UserNotFoundException()
             return@run SuccessMessage("Profile picture uploaded successfully.")
         }
     }*/

    // Private helper function to create access and refresh tokens for a user.
    private fun createTokens(userID: Int, username: String, repo: UsersRepository): AccessRefreshTokensModel {
        val userRefreshTokens = repo.getUserRefreshTokens(userID)
        if (userRefreshTokens.size >= usersConfig.maxTokensPerUser) {
            repo.deleteUserRefreshToken(userID, userRefreshTokens.first().token_validation)
        }
        val accessToken = domain.createAccessToken(username)
        val (refreshToken, expirationDate) = domain.createRefreshToken(username)
        val refreshTokenHash = domain.createTokenValidation(refreshToken)

        repo.insertRefreshToken(userID, refreshTokenHash, expirationDate)

        return AccessRefreshTokensModel(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    /*
    fun byteArrayToMultipartFile(imageBytes: ByteArray, fileName: String, fileType: String): MultipartFile {
        val inputStream: InputStream = imageBytes.inputStream()
        return MockMultipartFile(fileName, fileName, fileType, inputStream)
    }
    */
}