package project.planItAPI.domain.user

import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.InvalidValueException
import project.planItAPI.utils.Success

typealias EmailResult = Either<HTTPCodeException, Email>

class Email private constructor(val value: String)  {

    companion object {
        private const val EMAIL_FORMAT = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.com$"

        operator fun invoke(value: String): EmailResult = when {
            value.matches(EMAIL_FORMAT.toRegex()) -> Success(Email(value))
            else -> Failure(InvalidValueException("email format"))
        }
    }
}