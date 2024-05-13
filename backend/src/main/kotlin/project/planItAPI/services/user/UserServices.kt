package project.planItAPI.services.user

import org.springframework.stereotype.Service
import project.planItAPI.domain.event.Category
import project.planItAPI.domain.user.Email
import project.planItAPI.domain.user.EmailOrName
import project.planItAPI.domain.user.Name
import project.planItAPI.domain.user.Password
import project.planItAPI.models.AccessRefreshTokensModel
import project.planItAPI.models.UserRegisterOutputModel
import project.planItAPI.repository.jdbi.user.UsersRepository
import project.planItAPI.services.user.utils.UsersDomain
import project.planItAPI.services.user.utils.UsersDomainConfig
import project.planItAPI.repository.transaction.TransactionManager
import project.planItAPI.utils.ExistingEmailException
import project.planItAPI.utils.ExistingUsernameException
import project.planItAPI.utils.IncorrectPasswordException
import project.planItAPI.models.SystemInfo
import project.planItAPI.models.UserInfo
import project.planItAPI.models.UserLogInOutputModel
import project.planItAPI.models.UserLogInValidation
import project.planItAPI.utils.IncorrectLoginException
import project.planItAPI.models.RefreshTokensOutputModel
import project.planItAPI.utils.UserNotFoundException
import project.planItAPI.utils.UserRegisterErrorException
import project.planItAPI.models.SuccessMessage

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

    val APPLICATION_NAME = "PlanIt"
    val APP_VERSION = "0.0.1"
    val CONTRIBUTORS_LIST = listOf("Tiago Neutel - 49510", "Daniel Pojega - 49521")

    /**
     * Registers a new user.
     *
     * @param name The user's name.
     * @param username The user's username.
     * @param email The user's email.
     * @param password The user's password.
     * @return The result of the user registration as [UserRegisterResult].
     */
    fun register(name: Name, username: Name, email: Email, password: Password): UserRegisterResult {
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
     * @param emailOrName The user's email.
     * @param password The user's password.
     * @return The result of the user login as [UserLoginResult].
     */
    fun login(emailOrName: EmailOrName, password: Password): UserLoginResult {
        return transactionManager.run {
            val usersRepository = it.usersRepository
            when (emailOrName) {
                is EmailOrName.EmailType -> {
                    val user = usersRepository.getUserByEmail(emailOrName.email.value)
                    if (user != null) {
                        return@run loginValidation(password.value, user, usersRepository)
                    }
                }
                is EmailOrName.NameType -> {
                    val user = usersRepository.getUserByUsername(emailOrName.name.value)
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
            return@run UserInfo(user.id, user.name, user.username,  user.email, user.description, user.interests.split(","))
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
    fun editUser(userID: Int, name: Name, description: String, interests: List<Category>): EditUserResult {
        return transactionManager.run {
            val usersRepository = it.usersRepository
            if (usersRepository.getUser(userID) == null) {
                throw UserNotFoundException()
            }
            usersRepository.editUser(userID, name.value, description, interests.toString())
            return@run SuccessMessage("User edited successfully.")
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
            if (!domain.checkRefreshToken(refreshToken)) throw Exception("Invalid refresh token")
            val userID = usersRepository.getUserIDByToken(refreshTokenHash)
                ?: throw Exception("Invalid refresh token")
            val user = usersRepository.getUser(userID) ?: throw Exception("Invalid refresh token")

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

    /**
     * Retrieves information about the application.
     *
     * @return [SystemInfo] containing application version and contributors.
     */
    fun about() = SystemInfo(
        APPLICATION_NAME,
        APP_VERSION,
        CONTRIBUTORS_LIST,
    )

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