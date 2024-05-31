package project.planItAPI.domain

import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.InvalidLimitAndOffsetException
import project.planItAPI.utils.Success

/**
 * Validates the limit and offset values for a query.
 *
 * @param limit The limit value to validate.
 * @param offset The offset value to validate.
 * @return A [Pair] containing the validated limit (first) and offset (second) values, or an [Exception] if the values are invalid.
 */
fun validateLimitAndOffset(limit: Int?, offset: Int?): Either<Exception, Pair<Int, Int>> {
    val validatedLimit = limit ?: 10
    val validatedOffset = offset ?: 0

    if(validatedLimit < 0 && validatedOffset < 0) {
        return Failure(InvalidLimitAndOffsetException(true, true))
    }
    if(validatedLimit < 0) {
        return Failure(InvalidLimitAndOffsetException(true, false))
    }
    if(validatedOffset < 0) {
        return Failure(InvalidLimitAndOffsetException(false, true))
    }
    return Success(Pair(validatedLimit, validatedOffset))
}