package project.planItAPI.domain.event

import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.InvalidValueLengthException
import project.planItAPI.utils.Success

typealias DescriptionResult = Either<HTTPCodeException, Description>

class Description private constructor(val value: String) {
    companion object {
        private const val minLength = 1
        private const val maxLength = 400

        operator fun invoke(value: String?): DescriptionResult {
            return if (value == null || value == "") {
                Success(Description("No description yet!"))
            } else when (value.length) {
                !in minLength..maxLength -> Failure(InvalidValueLengthException("description"))
                else -> Success(Description(value))
            }
        }
    }
}