package project.planItAPI.domain.event

import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.InvalidTimestampFormatException
import project.planItAPI.utils.Success

typealias DateResult = Either<HTTPCodeException, DateFormat>

class DateFormat private constructor(val value: String) {
    companion object {
        private val regex = """^(\d{4})-(\d{2})-(\d{2}) (\d{2}:\d{2})$""".toRegex()

        operator fun invoke(input: String?): DateResult {
            if (input.isNullOrBlank()) return Success(DateFormat(""))
            val matchResult = regex.matchEntire(input)
            if (matchResult != null) {
                val (_, month, day, _) = matchResult.destructured
                if (month.toInt() !in 1..12 || day.toInt() !in 1..31) {
                    return Failure(InvalidTimestampFormatException(input))
                }
                return Success(DateFormat(input))
            }
            return Failure(InvalidTimestampFormatException(input))
        }
    }
}