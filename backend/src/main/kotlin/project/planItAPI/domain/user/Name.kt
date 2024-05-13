package project.planItAPI.domain.user

import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.InvalidUsernameException
import project.planItAPI.utils.InvalidUsernameLengthException
import project.planItAPI.utils.Success
import project.planItAPI.utils.UsernameIsBlankException
import java.lang.Exception

typealias NameResult = Either<HTTPCodeException, Name>

class Name private constructor(val value: String) {
    companion object {
        private const val minLength = 5
        private const val maxLength = 20

        operator fun invoke(value: String): NameResult = when {
            value.isBlank() -> Failure(UsernameIsBlankException())
            value.contains("@") -> Failure(InvalidUsernameException())
            value.length !in minLength..maxLength -> Failure(InvalidUsernameLengthException())
            else -> Success(Name(value))
        }
    }
}