package project.planItAPI.domain.user

import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.MultiplePasswordExceptions
import project.planItAPI.utils.PasswordException
import project.planItAPI.utils.PasswordHasNoNumber
import project.planItAPI.utils.PasswordHasNoSpecialChar
import project.planItAPI.utils.PasswordHasNoUppercase
import project.planItAPI.utils.PasswordTooShort
import project.planItAPI.utils.Success

typealias PasswordSafeResult = Either<HTTPCodeException, Unit>
typealias PasswordResult = Either<HTTPCodeException, Password>

class Password private constructor(val value: String) {
    companion object {

        operator fun invoke(value: String): PasswordResult {
            val password = Password(value)
            return when (val result = password.isPasswordSafe()) {
                is Failure -> Failure(result.value)
                is Success -> Success(password)
            }
        }
    }

    /**
     * Checks if a password is considered safe.
     *
     * @return A PasswordSafeResult indicating if the password is safe or not.
     */
    fun isPasswordSafe(): PasswordSafeResult {
        val exceptions = mutableListOf<PasswordException>()

        if (value.length < 5) {
            exceptions.add(PasswordTooShort())
        }
        if (!value.any { it.isDigit() }) {
            exceptions.add(PasswordHasNoNumber())
        }
        if (!value.any { it.isUpperCase() }) {
            exceptions.add(PasswordHasNoUppercase())
        }
        if (!value.any { !it.isLetterOrDigit() }) {
            exceptions.add(PasswordHasNoSpecialChar())
        }

        if (exceptions.isNotEmpty()) {
            return Failure(MultiplePasswordExceptions(exceptions))
        }

        return Success(Unit)
    }
}