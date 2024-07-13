package project.planItAPI.domain.poll

import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.InvalidValueLengthException
import project.planItAPI.utils.Success
import project.planItAPI.utils.ValueIsBlankException

typealias OptionResult = Either<HTTPCodeException, Option>

class Option private constructor(val value: String) {
    companion object {
        private const val maxLength = 50
        operator fun invoke(value: String): OptionResult = when {
                value.isBlank() -> Failure(ValueIsBlankException("Option"))
                value.length >= maxLength -> Failure(InvalidValueLengthException("option"))
                else -> Success(Option(value))
        }
    }
}