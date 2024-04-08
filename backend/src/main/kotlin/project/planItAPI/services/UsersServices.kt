package project.planItAPI.services

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import project.planItAPI.utils.AccessRefreshTokensModel
import project.planItAPI.utils.UserRegisterOutputModel
import project.planItAPI.repository.jdbi.users.UsersRepository
import project.planItAPI.repository.jdbi.utils.users.UsersDomain
import project.planItAPI.repository.jdbi.utils.users.UsersDomainConfig
import project.planItAPI.repository.transaction.TransactionManager
import project.planItAPI.utils.ExistingEmailException
import project.planItAPI.utils.ExistingUsernameException
import project.planItAPI.utils.IncorrectPasswordException
import project.planItAPI.utils.SystemInfo
import project.planItAPI.utils.UserInfo
import project.planItAPI.utils.UserLogInOutputModel
import project.planItAPI.utils.UserLogInValidation
import project.planItAPI.utils.IncorrectLoginException
import project.planItAPI.utils.UserNotFoundException
import project.planItAPI.utils.UserRegisterErrorException
import java.io.InputStream
import org.springframework.mock.web.MockMultipartFile
import project.planItAPI.utils.SuccessMessage

/**
 * Service class providing user-related functionality.
 *
 * @param transactionManager The transaction manager for handling repository interactions.
 * @param domain The domain layer for user-related functionality.
 * @param usersConfig The configuration for user-related functionality.
 */
@Service
class UsersServices (
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
    fun register(
        name: String,
        username: String,
        email: String,
        password: String
    ): UserRegisterResult {
        return transactionManager.run {
            // Check if the password is safe, if not throw exception with problems
            domain.isPasswordSafe(password)

            val usersRepository = it.usersRepository

            if (usersRepository.getUserByEmail(email) != null)
                throw ExistingEmailException()

            if (usersRepository.existsByUsername(username))
                throw ExistingUsernameException()

            val hashedPassword = domain.createHashedPassword(password)
            when (val newUserID = usersRepository.register(
                name,
                username,
                email,
                hashedPassword
            )) {
                is Int -> {
                    val (accessToken, newRefreshToken) = createTokens(newUserID, username, usersRepository)
                    return@run UserRegisterOutputModel(newUserID, username, name, newRefreshToken, accessToken)
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
    fun login(emailOrName: String, password: String): UserLoginResult {
        return transactionManager.run {
            val usersRepository = it.usersRepository
            val userEmail = usersRepository.getUserByEmail(emailOrName)
            if( userEmail != null ){
                return@run loginValidation(password, userEmail, usersRepository)
            }
            val username = usersRepository.getUserByUsername(emailOrName)
            if (username != null) {
                return@run loginValidation(password, username, usersRepository)
            }
            throw IncorrectLoginException()
        }
    }

    // Private helper function to validate login credentials and create tokens for a user.
    private fun loginValidation(
        password: String,
        userEmail: UserLogInValidation,
        usersRepository: UsersRepository
    ): UserLogInOutputModel {
        if (!domain.validatePassword(password, userEmail.hashedPassword)) {
            throw IncorrectPasswordException()
        }
        val (accessToken, newRefreshToken) = createTokens(userEmail.id, userEmail.username, usersRepository)
        return UserLogInOutputModel(userEmail.id, accessToken, newRefreshToken)
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
    fun editUser(userID: Int, name: String, description: String, interests: String): EditUserResult {
        return transactionManager.run {
            val usersRepository = it.usersRepository
            if (usersRepository.getUser(userID) == null) {
                throw UserNotFoundException()
            }
            usersRepository.editUser(userID, name, description, interests)
            return@run SuccessMessage("User edited successfully.")
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
        "PlanIt",
        "0.0.1",
        listOf("Tiago Neutel - 49510", "Daniel Pojega - 49521"),
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

    fun byteArrayToMultipartFile(imageBytes: ByteArray, fileName: String, fileType: String): MultipartFile {
        val inputStream: InputStream = imageBytes.inputStream()
        return MockMultipartFile(fileName, fileName, fileType, inputStream)
    }
}