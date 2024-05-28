package project.planItAPI.services.utils

import project.planItAPI.domain.event.Category
import project.planItAPI.domain.event.DateFormat
import project.planItAPI.domain.event.Money
import project.planItAPI.domain.event.Subcategory
import project.planItAPI.domain.event.Visibility
import project.planItAPI.models.CreateEventOutputModel
import project.planItAPI.models.EventOutputModel
import project.planItAPI.models.SearchEventListOutputModel
import project.planItAPI.models.SuccessMessage
import project.planItAPI.models.UserInEvent
import project.planItAPI.models.UsersInEventList
import project.planItAPI.repository.transaction.TransactionManager
import project.planItAPI.services.event.CategoriesResult
import project.planItAPI.services.event.CreateEventResult
import project.planItAPI.services.event.DeleteEventResult
import project.planItAPI.services.event.EditEventResult
import project.planItAPI.services.event.EventResult
import project.planItAPI.services.event.EventServices
import project.planItAPI.services.event.JoinEventResult
import project.planItAPI.services.event.LeaveEventResult
import project.planItAPI.services.event.SearchEventResult
import project.planItAPI.services.event.UsersInEventResult
import project.planItAPI.utils.EventNotFoundException
import project.planItAPI.utils.FailedToCreateEventException
import project.planItAPI.utils.Failure
import project.planItAPI.utils.IncorrectPasswordException
import project.planItAPI.utils.InvalidCategoryException
import project.planItAPI.utils.Success
import project.planItAPI.utils.UserAlreadyInEventException
import project.planItAPI.utils.UserIsNotOrganizerException
import project.planItAPI.utils.UserNotInEventException

class FakeEventServices(transactionManager: TransactionManager) : EventServices(transactionManager) {

    private fun generateEventID(): Int {
        // Simulate generating an event ID
        return 1 // Just a placeholder value for demonstration purposes
    }

    override fun createEvent(
        title: String,
        description: String?,
        category: Category,
        subcategory: Subcategory,
        location: String?,
        visibility: Visibility,
        date: DateFormat,
        endDate: DateFormat,
        price: Money,
        userID: Int,
        password: String
    ): CreateEventResult {
        // Simulate the behavior of creating an event
        return if (visibility == Visibility.Private && password.isBlank()) {
            Failure(FailedToCreateEventException())
        } else {
            val eventID = generateEventID()
            Success(CreateEventOutputModel(eventID, title, "Created with success."))
        }
    }

    override fun getEvent(id: Int): EventResult {
        // Simulate the behavior of retrieving an event
        return when (id) {
            1 -> {
                Success(
                    EventOutputModel(
                        id,
                        "Event Title",
                        "Event description",
                        "Technology",
                        "Web Development",
                        "Location",
                        "Public",
                        "2024-12-12 12:00",
                        "2024-12-24 15:00",
                        100.00,
                        "EUR",
                        ""
                    )
                )
            }
            2 -> {
                Success(
                    EventOutputModel(
                        id,
                        "Event 2 Title",
                        "Event 2 description",
                        "Business",
                        "Networking Events and Mixers",
                        "Location 2",
                        "Private",
                        "2024-01-12 11:00",
                        "2024-01-24 15:00",
                        50.00,
                        "EUR",
                        "123"
                    )
                )
            }
            else -> {
                Failure(EventNotFoundException())
            }
        }
    }

    override fun getUsersInEvent(id: Int): UsersInEventResult {
        // Simulate the behavior of retrieving users in an event
        return when (id) {
            1 -> {
                Success(
                    UsersInEventList(
                        listOf(
                            UserInEvent(
                                1,
                                "user1",
                                "user1"
                            ),
                            UserInEvent(
                                3,
                                "user3",
                                "user3"
                            )
                        )
                    )
                )
            }
            2 -> {
                Success(
                    UsersInEventList(
                        listOf(
                            UserInEvent(
                                2,
                                "user2",
                                "user2"
                            )
                        )
                    )
                )
            }
            else -> {
                Failure(EventNotFoundException())
            }
        }
    }

    override fun searchEvents(searchInput: String): SearchEventResult {
        // Simulate the behavior of searching for events
        return if (searchInput.isBlank() || searchInput == "All") {
            Success(
                SearchEventListOutputModel(
                    listOf(
                        EventOutputModel(
                            1,
                            "Event Title",
                            "Event description",
                            "Technology",
                            "Web Development",
                            "Location",
                            "Public",
                            "2024-12-12 12:00",
                            "2024-12-24 15:00",
                            100.00,
                            "EUR",
                            ""
                        ),
                        EventOutputModel(
                            2,
                            "Event 2 Title",
                            "Event 2 description",
                            "Business",
                            "Networking Events and Mixers",
                            "Location 2",
                            "Private",
                            "2024-01-12 11:00",
                            "2024-01-24 15:00",
                            50.00,
                            "EUR",
                            "123"
                        )
                    )
                )
            )
        }
        else if(searchInput == "Technology") {
            Success(
                SearchEventListOutputModel(
                    listOf(
                        EventOutputModel(
                            1,
                            "Event Title",
                            "Event description",
                            "Technology",
                            "Web Development",
                            "Location",
                            "Public",
                            "2024-12-12 12:00",
                            "2024-12-24 15:00",
                            100.00,
                            "EUR",
                            ""
                        )
                    )
                )
            )
        }
        else {
            Success(SearchEventListOutputModel(emptyList()))
        }
    }

    override fun joinEvent(userId: Int, eventId: Int, password: String): JoinEventResult {
        val eventResult = getEvent(eventId)
        if (eventResult is Failure) {
            return Failure(EventNotFoundException())
        }
        val usersInEvent = (getUsersInEvent(eventId) as Success).value
        if(password != (eventResult as Success).value.password) {
            return Failure(IncorrectPasswordException())
        }
        if (usersInEvent.users.any { user -> user.id == userId }) {
            return Failure(UserAlreadyInEventException())
        }

        return Success(SuccessMessage("User joined event with success."))
    }

    override fun leaveEvent(userId: Int, eventId: Int): LeaveEventResult {
        val eventResult = getEvent(eventId)
        if (eventResult is Failure) {
            return Failure(EventNotFoundException())
        }
        val usersInEvent = (getUsersInEvent(eventId) as Success).value
        if (!usersInEvent.users.any { user -> user.id == userId }) {
            return Failure(UserNotInEventException())
        }

        return Success(SuccessMessage("User left event with success."))
    }

    override fun deleteEvent(userId: Int, eventId: Int): DeleteEventResult {
        val eventResult = getEvent(eventId)
        val organizerId = 1
        if (eventResult is Failure) {
            return Failure(EventNotFoundException())
        }
        if (userId != organizerId) {
            return Failure(UserIsNotOrganizerException())
        }
        return Success(SuccessMessage("Event deleted with success."))
    }

    override fun editEvent(
        userId: Int,
        eventId: Int,
        title: String,
        description: String?,
        category: Category,
        subcategory: Subcategory,
        location: String?,
        visibility: Visibility,
        date: DateFormat,
        endDate: DateFormat,
        price: Money
    ): EditEventResult {
        val organizerId = 1
        val eventResult = getEvent(eventId)
        if (eventResult is Failure) {
            return Failure(EventNotFoundException())
        }
        if (userId != organizerId) {
            return Failure(UserIsNotOrganizerException())
        }

        return Success(SuccessMessage("Event edited with success."))
    }

    override fun getSubcategories(category: String): CategoriesResult {
        if (category != "Technology") {
            return Failure(InvalidCategoryException())
        }
        val subCategories = listOf(
            "Web Development",
            "Mobile Development",
            "Software Development",
            "Networking and Security",
            "Artificial Intelligence and Machine Learning",
            "Tech Startups and Entrepreneurship",
            "Emerging Technologies"
        )

        return Success(subCategories)
    }
}


