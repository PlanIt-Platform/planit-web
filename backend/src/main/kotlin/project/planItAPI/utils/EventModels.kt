package project.planItAPI.utils

data class EventInputModel(
    val title: String,
    val description: String?,
    val category: String,
    val subcategory: String?,
    val location: String?,
    val visibility: String?,
    val date: String?,
    val endDate: String?,
    val price: String?
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
    val price: String?,
)

data class Money(
    val amount: Double,
    val currency: String
)

data class SearchEventInputModel(
    val category: String?,
    val subcategory: String?,
    val price: Money?
)

data class SearchEventOutputModel(
    val events: List<EventOutputModel>
)
