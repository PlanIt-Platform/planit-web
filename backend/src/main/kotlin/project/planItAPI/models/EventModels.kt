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
    val subCategory: String?,
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

data class SearchEventsOutputModel(
    val id: Int,
    val title: String,
    val description: String?,
    val category: String?,
    val location: String?,
    val visibility: String,
    val date: String?,
)

data class SearchEventListOutputModel(
    val events: List<SearchEventsOutputModel>
)

/**
 * Model for the password of an event.
 *
 */
data class EventPasswordModel(
    val password: String
)

/**
 * Model for validated event inputs.
 *
 * @property visibility The visibility of the event.
 * @property category The category of the event.
 * @property subCategory The subcategory of the event.
 * @property date The date of the event.
 * @property endDate The end date of the event.
 * @property price The price of the event.
 */
data class ValidatedEventInputsModel(
    val visibility: Visibility,
    val category: Category,
    val subCategory: Subcategory,
    val date: DateFormat,
    val endDate: DateFormat,
    val price: Money
)

/**
 * Model for returning a list of users.
 *
 * @property id The unique identifier of the user.
 * @property taskName The name of the task.
 * @property taskId The unique identifier of the task.
 * @property username The username of the user.
 */
data class UserInEvent(
    val id: Int,
    val taskName: String?,
    val taskId: Int?,
    val username: String
)

/**
 * Model for returning a list of users in an event.
 *
 * @property users The list of users.
 */
data class UsersInEventList(
    val users: List<UserInEvent>
)

/**
 * Model for getting an event.
 *
 * @property password The password of the event.
 */
data class GetEventInput(
    val password: String?
)