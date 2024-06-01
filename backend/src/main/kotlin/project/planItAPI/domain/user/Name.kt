package project.planItAPI.domain.user

import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.InvalidValueException
import project.planItAPI.utils.InvalidValueLengthException
import project.planItAPI.utils.Success
import project.planItAPI.utils.ValueIsBlankException

typealias NameResult = Either<HTTPCodeException, Name>

class Name private constructor(val value: String) {
    companion object {
        private const val minLength = 1
        private const val maxLength = 20

        operator fun invoke(value: String): NameResult = when {
            value.isBlank() -> Failure(ValueIsBlankException("Name"))
            value.contains("@") -> Failure(InvalidValueException("name"))
            value.length !in minLength..maxLength -> Failure(InvalidValueLengthException("name"))
            else -> Success(Name(value))
        }
    }
}