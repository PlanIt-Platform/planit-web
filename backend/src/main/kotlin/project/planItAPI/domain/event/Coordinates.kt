package project.planItAPI.domain.event

import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.InvalidCoordinatesException
import project.planItAPI.utils.Success

typealias CoordinatesResult = Either<HTTPCodeException, Coordinates>

class Coordinates private constructor(val latitude: Double, val longitude: Double) {
    companion object {
        operator fun invoke(latitude: Double?, longitude: Double?): CoordinatesResult {
            if (latitude == null || longitude == null) {
                return Success(Coordinates(0.0, 0.0))
            }
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                return Failure(InvalidCoordinatesException())
            }
            return Success(Coordinates(latitude, longitude))
        }
    }
}