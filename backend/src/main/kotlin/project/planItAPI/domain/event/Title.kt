package project.planItAPI.domain.event

import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.InvalidValueLengthException
import project.planItAPI.utils.Success
import project.planItAPI.utils.ValueIsBlankException

typealias TitleResult = Either<HTTPCodeException, Title>

class Title private constructor(val value: String) {
    companion object {
        private const val minLength = 1
        private const val maxLength = 25

        operator fun invoke(value: String): TitleResult = when {
            value.isBlank() -> Failure(ValueIsBlankException("Title"))
            value.length !in minLength..maxLength -> Failure(InvalidValueLengthException("title"))
            else -> Success(Title(value))
        }
    }
}