package project.planItAPI.domain.user

import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.InvalidValueException
import project.planItAPI.utils.InvalidValueLengthException
import project.planItAPI.utils.Success
import project.planItAPI.utils.ValueIsBlankException

typealias UsernameResult = Either<HTTPCodeException, Username>

class Username private constructor(val value: String) {
    companion object {
        private const val minLength = 5
        private const val maxLength = 20

        operator fun invoke(value: String): UsernameResult = when {
            value.isBlank() -> Failure(ValueIsBlankException("Username"))
            value.contains("@") -> Failure(InvalidValueException("username"))
            value.length !in minLength..maxLength -> Failure(InvalidValueLengthException("username"))
            else -> Success(Username(value))
        }
    }
}