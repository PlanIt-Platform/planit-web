package project.planItAPI.http.controllers.event

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
        DateFormat(input.date),
        DateFormat(input.endDate),
        Money(input.price),
        if(input.locationType != null) LocationType(input.locationType) else Success(null),
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
                date = (results[5] as Success).value as DateFormat,
                endDate = (results[6] as Success).value as DateFormat,
                price = (results[7] as Success).value as Money,
                locationType = (results[8] as Success).value as LocationType?,
            )
        )
    } else {
        Failure(HTTPCodeException(errors.joinToString { it.message }, 400))
    }
}
