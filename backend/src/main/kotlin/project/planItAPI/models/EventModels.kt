package project.planItAPI.models

import project.planItAPI.domain.event.Category
import project.planItAPI.domain.event.DateFormat
import project.planItAPI.domain.event.Money
import project.planItAPI.domain.event.Subcategory
import project.planItAPI.domain.event.Visibility

data class EventInputModel(
    val title: String,
    val description: String?,
    val category: String,
    val subCategory: String,
    val location: String?,
    val visibility: String,
    val date: String,
    val endDate: String?,
    val price: String,
    val password: String
)

data class CreateEventOutputModel(
    val id: Int,
    val title: String,
    val status: String,
)

data class EventOutputModel(
    val id: Int,
    val title: String,
    val description: String?,
    val category: String,
    val subcategory: String?,
    val location: String?,
    val visibility: String?,
    val date: String?,
    val endDate: String?,
    val priceAmount: Double?,
    val priceCurrency: String?,
    val password: String
)

data class SearchEventOutputModel(
    val events: List<EventOutputModel>
)

data class EventPasswordModel(
    val password: String
)

data class ValidatedEventInputsModel(
    val visibility: Visibility,
    val category: Category,
    val subCategory: Subcategory,
    val date: DateFormat,
    val endDate: DateFormat,
    val price: Money
)