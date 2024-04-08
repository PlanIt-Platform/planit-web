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
    val price: String?,
    val userID: Int
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

