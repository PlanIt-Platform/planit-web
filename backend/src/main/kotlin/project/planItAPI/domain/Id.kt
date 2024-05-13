package project.planItAPI.domain

import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.InvalidIdException
import project.planItAPI.utils.Success
import java.lang.Exception

typealias IdResult = Either<Exception, Id>

class Id private constructor(val value: Int) {

    companion object {
        operator fun invoke(value: Int): IdResult {
            return if (value > 0) {
                Success(Id(value))
            } else {
                Failure(InvalidIdException())
            }
        }
    }
}