package project.planItAPI.domain.event

import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.InvalidEventCodeException
import project.planItAPI.utils.Success

typealias CodeResult = Either<HTTPCodeException, Code>

class Code private constructor(val value: String) {
    companion object {
        operator fun invoke(input: String?): CodeResult {
            if (input.isNullOrBlank() || input.length != 6) return Failure(InvalidEventCodeException())
            return Success(Code(input))
        }
    }
}