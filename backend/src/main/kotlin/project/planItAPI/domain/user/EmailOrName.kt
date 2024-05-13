package project.planItAPI.domain.user

import project.planItAPI.utils.Either
import project.planItAPI.utils.HTTPCodeException

sealed class EmailOrName {
    data class EmailType(val email: Email) : EmailOrName()
    data class NameType(val name: Name) : EmailOrName()

    companion object {
        operator fun invoke(value: String): Either<HTTPCodeException, EmailOrName> {
            return if (value.contains("@")) {
                Email(value).map { EmailType(it) }
            } else {
                Name(value).map { NameType(it) }
            }
        }
    }
}