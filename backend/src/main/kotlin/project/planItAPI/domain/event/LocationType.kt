package project.planItAPI.domain.event

import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.InvalidValueException
import project.planItAPI.utils.Success

typealias LocationTypeResult = Either<HTTPCodeException, LocationType>

enum class LocationType {
    Physical, Online;

    companion object {
        operator fun invoke(locationType: String): LocationTypeResult {
            return when (locationType) {
                "Physical" -> Success(Physical)
                "Online" -> Success(Online)
                else ->  Failure(InvalidValueException("locationType"))
            }
        }
    }
}