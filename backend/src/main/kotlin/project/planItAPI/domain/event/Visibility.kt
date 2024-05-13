package project.planItAPI.domain.event

import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.InvalidVisibilityValueException
import project.planItAPI.utils.Success

typealias VisibilityResult = Either<HTTPCodeException, Visibility>

enum class Visibility {
    Public, Private;

    companion object {
        operator fun invoke(visibility: String): VisibilityResult {
            return when (visibility) {
                "Public" -> Success(Public)
                "Private" -> Success(Private)
                else ->  Failure(InvalidVisibilityValueException())
            }
        }
    }
}