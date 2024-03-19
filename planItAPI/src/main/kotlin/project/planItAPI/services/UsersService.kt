package project.planItAPI.services

import org.springframework.stereotype.Service
import project.planItAPI.repository.transaction.TransactionManager

@Service
class UsersService (
    private val transactionManager: TransactionManager
    ){

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
            if (!domain.isSafePassword(password)) {
                throw InsecurePassword("Password too weak")
            }
            val usersRepository = it.usersRepository

            if (usersRepository.getUserByEmail(email) != null) {
                throw AlreadyExistsException("User with email $email already exists")
            }

            if (usersRepository.existsByUsername(name)) {
                throw AlreadyExistsException("User with name $name already exists")
            }

            val hashedPassword = domain.createPasswordValidation(name + password)

            when (val newUserID = usersRepository.register(name, email, hashedPassword)) {
                null ->
                    throw Exception("Unexpected error occurred")

                else -> {
                    val accessToken = createTokens(newUserID, name, usersRepository)
                    return@run UserRegisterOutputModel(newUserID, name, newRefreshToken, accessToken)
                }
            }
        }
    }
}