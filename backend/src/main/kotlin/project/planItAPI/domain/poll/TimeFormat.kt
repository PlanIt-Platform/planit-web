package project.planItAPI.domain.poll

import project.planItAPI.utils.DurationIsBlankException
import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.InvalidDurationException
import project.planItAPI.utils.Success

typealias TimeFormatResult = Either<HTTPCodeException, TimeFormat>

class TimeFormat private constructor(val value: String) {
    companion object {
        private val allowed_durations = listOf("1","4","8","12","24","72")

        operator fun invoke(value: String): TimeFormatResult = when {
            value.isBlank() -> Failure(DurationIsBlankException())
            value !in allowed_durations -> Failure(InvalidDurationException())
            else -> Success(TimeFormat(value))
        }
    }
}