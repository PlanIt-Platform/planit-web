package project.planItAPI.http.controllers.event

import project.planItAPI.domain.event.Coordinates
import project.planItAPI.domain.event.Category
import project.planItAPI.domain.event.DateFormat
import project.planItAPI.domain.event.Description
import project.planItAPI.domain.event.LocationType
import project.planItAPI.domain.event.Money
import project.planItAPI.domain.event.Subcategory
import project.planItAPI.domain.event.Title
import project.planItAPI.domain.event.Visibility
import project.planItAPI.models.EventInputModel
import project.planItAPI.models.ValidatedEventInputsModel
import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.Success

fun validateEventInput(input: EventInputModel): Either<Exception, ValidatedEventInputsModel> {
    val results = listOf(
        Title(input.title),
        Description(input.description),
        Visibility(input.visibility),
        Category(input.category),
        Subcategory(input.category, input.subCategory),
        if(input.locationType != null) LocationType(input.locationType) else Success(null),
        DateFormat(input.date),
        DateFormat(input.endDate),
        Money(input.price),
        Coordinates(input.latitude, input.longitude),
    )

    val errors = results.mapNotNull {
        when (it) {
            is Failure -> it.value
            else -> null
        }
    }

    return if (errors.isEmpty()) {
        Success(
            ValidatedEventInputsModel(
                title = (results[0] as Success).value as Title,
                description = (results[1] as Success).value as Description,
                visibility = (results[2] as Success).value as Visibility,
                category = (results[3] as Success).value as Category,
                subCategory = (results[4] as Success).value as Subcategory,
                locationType = (results[5] as Success).value as LocationType?,
                date = (results[6] as Success).value as DateFormat,
                endDate = (results[7] as Success).value as DateFormat,
                price = (results[8] as Success).value as Money,
                coordinates = (results[9] as Success).value as Coordinates
            )
        )
    } else {
        Failure(HTTPCodeException(errors.joinToString { it.message }, 400))
    }
}

fun validateFindNearbyEventsInput(
    radius: Int,
    latitude: Double,
    longitude: Double
): Either<Exception, Coordinates> {
    if (radius < 0) {
        return Failure(HTTPCodeException("Radius must be a positive integer", 400))
    }
    val result = Coordinates(latitude, longitude)

    return if (result is Success) {
        Success(result.value)
    } else {
        Failure(HTTPCodeException((result as Failure).value.message, 400))
    }
}
