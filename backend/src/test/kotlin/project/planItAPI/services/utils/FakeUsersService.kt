package project.planItAPI.services.utils

import project.planItAPI.domain.user.Email
import project.planItAPI.domain.user.EmailOrUsername
import project.planItAPI.domain.user.Name
import project.planItAPI.domain.user.Password
import project.planItAPI.domain.user.Username
import project.planItAPI.models.AccessRefreshTokensModel
import project.planItAPI.models.SuccessMessage
import project.planItAPI.models.UserInfo
import project.planItAPI.models.UserLogInOutputModel
import project.planItAPI.models.UserRegisterOutputModel
import project.planItAPI.repository.transaction.TransactionManager
import project.planItAPI.services.user.EditUserResult
import project.planItAPI.services.user.GetUserResult
import project.planItAPI.services.user.LogoutResult
import project.planItAPI.services.user.UserLoginResult
import project.planItAPI.services.user.UserRegisterResult
import project.planItAPI.services.user.UserServices
import project.planItAPI.services.user.utils.UsersDomain
import project.planItAPI.services.user.utils.UsersDomainConfig
import project.planItAPI.utils.ExistingEmailException
import project.planItAPI.utils.ExistingUsernameException
import project.planItAPI.utils.Failure
import project.planItAPI.utils.IncorrectLoginException
import project.planItAPI.utils.IncorrectPasswordException
import project.planItAPI.utils.ServerConfiguration
import project.planItAPI.utils.Success
import project.planItAPI.utils.UserNotFoundException
import java.time.Duration
import java.time.Instant

private val usersDomainConfig = UsersDomainConfig(
    7,
    Duration.ofHours(24),
    Duration.ofHours(168),
    1)

private val serverConfig = ServerConfiguration(
    "mRk4Tl7Y5vtcmRn60C1mbLEGdNbaqPKtFPl0NY85geFZwu4uQfIOKajHu",
    "lbtiqTjif4y18LXlMEgDsEwkQiuIcD041lu7hmKxDvmYtgVM6JS",
    "J8Bon8MoizjQijRTHMl1JpcIYYbaNRU279Vvef9onhhmWhFb1CZIQ7szXT9xnYcWZ",
    "9okxeepSaFoj2nkn4doAb9yW5iKbvfz7Ro84w5HIbwOMBNcMP6PMQEeh9"
)

private val usersDomain = UsersDomain(usersDomainConfig, serverConfig)

data class FakeUser(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val password: String,
    val description: String,
    val interests: List<String>
)

data class FakeRefreshToken (
    val userID: Int,
    val token: String,
    val expirationDate: Instant
)

class FakeUserServices(transactionManager: TransactionManager)
    : UserServices(transactionManager, usersDomain, usersDomainConfig) {

    private val users = mutableListOf<FakeUser>()

    private val refreshTokens = mutableListOf<FakeRefreshToken>()

    private fun loginValidation(
        password: String,
        user: FakeUser
    ): UserLogInOutputModel {
        if (!usersDomain.validatePassword(password, user.password)) {
            throw IncorrectPasswordException()
        }

        val (accessToken, newRefreshToken) = createTokens(user.id, user.username)

        return UserLogInOutputModel(user.id, accessToken, newRefreshToken)
    }

    override fun register(name: Name, username: Username, email: Email, password: Password): UserRegisterResult {
        //Simulate the register of a user
        if (users.any { it.email == email.value }) return Failure(ExistingEmailException())
        if (users.any { it.username == username.value }) return Failure(ExistingUsernameException())
        val hashedPassword = usersDomain.createHashedPassword(password.value)

        users.add(
            FakeUser(
                users.size + 1,
                name.value,
                username.value,
                email.value,
                hashedPassword,
                "",
                emptyList()
            )
        )

        val (accessToken, newRefreshToken) = createTokens(users.size, username.value)

        return Success(
            UserRegisterOutputModel(
                users.size,
                username.value,
                name.value,
                newRefreshToken,
                accessToken
            )
        )
    }

    override fun login(emailOrUsername: EmailOrUsername, password: Password): UserLoginResult {
        //Simulate the login of a user
        when (emailOrUsername) {
            is EmailOrUsername.EmailType -> {
                val user = users.find { it.email == emailOrUsername.email.value }
                if (user != null) {
                    return Success(loginValidation(password.value, user))
                }
            }
            is EmailOrUsername.UsernameType -> {
                val user = users.find { it.username == emailOrUsername.name.value }
                if (user != null) {
                    return Success(loginValidation(password.value, user))
                }
            }
        }
        return Failure(IncorrectLoginException())
    }

    override fun logout(accessToken: String, refreshToken: String): LogoutResult {
        val refreshTokenHash = usersDomain.createTokenValidation(token = refreshToken)
        val tokenToRemove = refreshTokens.find { it.token == refreshTokenHash }
        if (tokenToRemove != null) {
            refreshTokens.remove(tokenToRemove)
            return Success(Unit)
        }
        return Failure(Exception("invalid token"))
    }

    override fun getUser(userID: Int): GetUserResult {
        //Simulate the retrieval of a user
        val user = users.find { it.id == userID }
        return if (user != null) {
            Success(
                UserInfo(
                    user.id,
                    user.name,
                    user.username,
                    user.email,
                    user.description,
                    user.interests
                )
            )
        } else {
            Failure(UserNotFoundException())
        }
    }

    override fun editUser(userID: Int, name: Name, description: String, interests: List<String>): EditUserResult {
        //Simulate the editing of a user
        val user = users.find { it.id == userID }
        if (user != null) {
            users.remove(user)
            users.add(
                FakeUser(
                    user.id,
                    name.value,
                    user.username,
                    user.email,
                    user.password,
                    description,
                    interests
                )
            )
            return Success(SuccessMessage("User edited successfully."))
        } else {
            return Failure(UserNotFoundException())

        }
    }

    private fun createTokens(userID: Int, username: String): AccessRefreshTokensModel {
        val userRefreshTokens = refreshTokens.filter { it.userID == userID }
        if (userRefreshTokens.size >= usersDomainConfig.maxTokensPerUser) {
            refreshTokens.remove(userRefreshTokens.minByOrNull { it.expirationDate })
        }
        val accessToken = usersDomain.createAccessToken(username)
        val (refreshToken, expirationDate) = usersDomain.createRefreshToken(username)
        val refreshTokenHash = usersDomain.createTokenValidation(refreshToken)

        refreshTokens.add(
            FakeRefreshToken(
                userID,
                refreshTokenHash,
                expirationDate.toInstant()
            )
        )

        return AccessRefreshTokensModel(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }
}