package project.planItAPI.domain.poll

import project.planItAPI.domain.user.Name
import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.InvalidOptionLengthException
import project.planItAPI.utils.OptionIsBlankException
import project.planItAPI.utils.Success

typealias OptionResult = Either<HTTPCodeException, Option>

class Option private constructor(val value: String) {
    companion object {
        private const val maxLength = 50
        operator fun invoke(value: String): OptionResult = when {
            value.isBlank() -> Failure(OptionIsBlankException())
            value.length <= maxLength -> Failure(InvalidOptionLengthException())
            else -> Success(Option(value))
        }
    }
}