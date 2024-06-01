package project.planItAPI.domain.user

import project.planItAPI.utils.Either
import project.planItAPI.utils.HTTPCodeException

sealed class EmailOrUsername {
    data class EmailType(val email: Email) : EmailOrUsername()
    data class UsernameType(val name: Username) : EmailOrUsername()

    companion object {
        operator fun invoke(value: String): Either<HTTPCodeException, EmailOrUsername> {
            return if (value.contains("@")) {
                Email(value).map { EmailType(it) }
            } else {
                Username(value).map { UsernameType(it) }
            }
        }
    }
}