package project.planItAPI.services.event

import org.springframework.stereotype.Service
import project.planItAPI.domain.event.Category
import project.planItAPI.repository.transaction.TransactionManager
import project.planItAPI.models.CreateEventOutputModel
import project.planItAPI.domain.event.DateFormat
import project.planItAPI.domain.event.Money
import project.planItAPI.domain.event.Subcategory
import project.planItAPI.utils.EventNotFoundException
import project.planItAPI.utils.FailedToCreateEventException
import project.planItAPI.utils.Failure
import project.planItAPI.utils.IncorrectPasswordException
import project.planItAPI.utils.InvalidCategoryException
import project.planItAPI.models.SuccessMessage
import project.planItAPI.domain.event.Visibility
import project.planItAPI.domain.event.readCategories
import project.planItAPI.domain.event.readSubCategories
import project.planItAPI.utils.UserAlreadyInEventException
import project.planItAPI.utils.UserIsNotOrganizerException
import project.planItAPI.utils.UserNotInEventException
import java.sql.Timestamp

@Service
class EventServices(
    private val transactionManager: TransactionManager
) {

    /**
     * Creates a new event with the provided information.
     * @param title The title of the new event.
     * @param description The description of the new event.
     * @param category The category of the new event.
     * @param subcategory The subcategory of the new event.
     * @param location The location of the new event.
     * @param visibility The visibility of the new event.
     * @param date The date of the new event.
     * @param endDate The end date of the new event.
     * @param price The price of the new event.
     * @param userID The ID of the user creating the event.
     * @return [CreateEventOutputModel] containing the ID of the newly created event, its title and a status message.
     * If the creation fails, a [Failure] is thrown.
     */
    fun createEvent(
        title: String,
        description: String?,
        category: Category,
        subcategory: Subcategory,
        location: String?,
        visibility: Visibility = Visibility.Public,
        date: DateFormat,
        endDate: DateFormat,
        price: Money,
        userID: Int,
        password: String
    ): CreateEventResult =
        transactionManager.run {
            val eventsRepository = it.eventsRepository
            if (visibility == Visibility.Private && password.isBlank()) throw FailedToCreateEventException()
            val eventID = eventsRepository.createEvent(
                title,
                description ?: "",
                category.name,
                subcategory.name,
                location ?: "To be Determined",
                visibility.name,
                Timestamp.valueOf("${date.value}:00"),
                Timestamp.valueOf("${endDate.value}:00"),
                price,
                userID,
                password
            ) ?: throw FailedToCreateEventException()
            return@run CreateEventOutputModel(eventID, title, "Created with success.")
        }


    /**
     * Retrieves the event associated with the given ID.
     * @param id The ID of the event to retrieve.
     * @return [EventResult] The event associated with the ID. If the event is not found, a [Failure] is thrown.
     */
    fun getEvent(id: Int): EventResult = transactionManager.run {
        return@run it.eventsRepository.getEvent(id) ?: throw EventNotFoundException()
    }

    /**
     * Retrieves the users that are participating in the event with the given ID.
     * @param id The ID of the event to retrieve the users from.
     * @return [UsersInEventResult] The users participating in the event. If the event is not found, a [Failure] is thrown.
     */
    fun getUsersInEvent(id: Int): UsersInEventResult = transactionManager.run {
        return@run it.eventsRepository.getUsersInEvent(id) ?: throw EventNotFoundException()
    }

    /**
     * Searches for events based on the provided user input.
     * @param searchInput The input to search for.
     * @return [SearchEventResult] The events that match the filters.
     */
    fun searchEvents(searchInput: String): SearchEventResult = transactionManager.run {
        val eventsRepository = it.eventsRepository
        if (searchInput.isBlank() || searchInput == "All") {
            return@run eventsRepository.getAllEvents()
        }
        return@run eventsRepository.searchEvents(searchInput)
    }

    /**
     * Allows a user to join an event.
     * @param userId The ID of the user joining the event.
     * @param eventId The ID of the event to join.
     * @param password The password of the event.
     * @return [JoinEventResult] A message indicating the success of the operation. If the event is not found, a [Failure] is thrown.
     */
    fun joinEvent(userId: Int, eventId: Int, password: String): JoinEventResult = transactionManager.run {
        val eventsRepository = it.eventsRepository
        val event = eventsRepository.getEvent(eventId) ?: throw EventNotFoundException()
        if (event.password != password) throw IncorrectPasswordException()
        val usersInEvent = eventsRepository.getUsersInEvent(event.id)
        if (usersInEvent != null && usersInEvent.users.any { user -> user.id == userId }) {
            throw UserAlreadyInEventException()
        }
        eventsRepository.joinEvent(userId, event.id)
        return@run SuccessMessage("User joined event with success.")
    }

    /**
     * Allows a user to leave an event.
     * @param userId The ID of the user leaving the event.
     * @param eventId The ID of the event to leave.
     * @return [LeaveEventResult] A message indicating the success of the operation. If the event is not found, a [Failure] is thrown.
     */
    fun leaveEvent(userId: Int, eventId: Int): LeaveEventResult = transactionManager.run {
        val eventsRepository = it.eventsRepository
        val event = eventsRepository.getEvent(eventId) ?: throw EventNotFoundException()
        val usersInEvent = eventsRepository.getUsersInEvent(event.id)
        if (usersInEvent != null && !usersInEvent.users.any { user -> user.id == userId }) {
            throw UserNotInEventException()
        }
        eventsRepository.leaveEvent(userId, event.id)
        return@run SuccessMessage("User left event with success.")
    }

    /**
     * Deletes the event with the given ID.
     * @param userId The ID of the user deleting the event.
     * @param eventId The ID of the event to delete.
     * @return [DeleteEventResult] A message indicating the success of the operation. If the event is not found, a [Failure] is thrown.
     */
    fun deleteEvent(userId: Int, eventId: Int): DeleteEventResult = transactionManager.run {
        val eventsRepository = it.eventsRepository
        val event = eventsRepository.getEvent(eventId) ?: throw EventNotFoundException()
        val creatorId = eventsRepository.getEventOrganizer(eventId)
        if (creatorId != userId) { throw UserIsNotOrganizerException() }
        eventsRepository.deleteEvent(event.id)
        return@run SuccessMessage("Event deleted with success.")
    }

    /**
     * Edits the event with the given ID.
     * @param userId The ID of the user editing the event.
     * @param eventId The ID of the event to edit.
     * @param title The new title of the event.
     * @param description The new description of the event.
     * @param category The new category of the event.
     * @param subcategory The new subcategory of the event.
     * @param location The new location of the event.
     * @param visibility The new visibility of the event.
     * @param date The new date of the event.
     * @param endDate The new end date of the event.
     * @param price The new price of the event.
     * @return [EditEventResult] A message indicating the success of the operation. If the event is not found, a [Failure] is thrown.
     */
    fun editEvent(
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
    ): EditEventResult = transactionManager.run {
        val eventsRepository = it.eventsRepository
        eventsRepository.getEvent(eventId) ?: throw EventNotFoundException()
        if (eventsRepository.getEventOrganizer(eventId) != userId) throw UserIsNotOrganizerException()
        eventsRepository.editEvent(
            eventId,
            title,
            description ?: "",
            category.name,
            subcategory.name,
            location ?: "To be Determined",
            visibility.name,
            Timestamp.valueOf("${date.value}:00"),
            Timestamp.valueOf("${endDate.value}:00"),
            price
        )
        return@run SuccessMessage("Event edited with success.")
    }

    /**
     * Retrieves the list of event categories.
     * @return [CategoriesResult] The list of event categories.
     * If the categories are not found, a [Failure] is thrown.
     */
    fun getCategories(): CategoriesResult = transactionManager.run {
        return@run readCategories().keys.toList()
    }

    /**
     * Retrieves the list of event subcategories for the given category.
     * @param category The category to retrieve the subcategories from.
     * @return [SubcategoriesResult] The list of event subcategories for the given category.
     */
    fun getSubcategories(category: String): SubcategoriesResult = transactionManager.run {
        return@run readSubCategories(category) ?: throw InvalidCategoryException()
    }
}