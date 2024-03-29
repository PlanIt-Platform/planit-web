package project.planItAPI.services

import org.springframework.stereotype.Service
import project.planItAPI.utils.AccessRefreshTokensModel
import project.planItAPI.utils.UserRegisterOutputModel
import project.planItAPI.repository.UsersRepository
import project.planItAPI.repository.jdbi.utils.UsersDomain
import project.planItAPI.repository.jdbi.utils.UsersDomainConfig
import project.planItAPI.repository.transaction.TransactionManager
import project.planItAPI.utils.ExistingEmailException
import project.planItAPI.utils.ExistingUsernameException
import project.planItAPI.utils.IncorrectPasswordException
import project.planItAPI.utils.SystemInfo
import project.planItAPI.utils.UserLogInOutputModel
import project.planItAPI.utils.UserLogInValidation
import project.planItAPI.utils.UserNotFoundException
import project.planItAPI.utils.UserRegisterErrorException


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
     * @param email The user's email.
     * @param password The user's password.
     * @return The result of the user registration as [UserRegisterResult].
     */
    fun register(name: String, email: String, password: String): UserRegisterResult {
        return transactionManager.run {
            // Check if the password is safe, if not throw exception with problems
            domain.isPasswordSafe(password)

            val usersRepository = it.usersRepository

            if (usersRepository.getUserByEmail(email) != null)
                throw ExistingEmailException()

            if (usersRepository.existsByUsername(name))
                throw ExistingUsernameException()

            val hashedPassword = domain.createHashedPassword(password)

            when (val newUserID = usersRepository.register(name, email, hashedPassword)) {
                is Int -> {
                    val (accessToken, newRefreshToken) = createTokens(newUserID, name, usersRepository)
                    return@run UserRegisterOutputModel(newUserID, name, newRefreshToken, accessToken)
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
            throw UserNotFoundException()
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
     * @return The result of the user login as [UserTokensResult].
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
}