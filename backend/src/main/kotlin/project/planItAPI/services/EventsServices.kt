package project.planItAPI.services

import org.springframework.stereotype.Service
import project.planItAPI.isCategory
import project.planItAPI.isValidSubcategory
import project.planItAPI.repository.transaction.TransactionManager
import project.planItAPI.utils.CreateEventOutputModel
import project.planItAPI.utils.Either
import project.planItAPI.utils.EventNotFoundException
import project.planItAPI.utils.FailedToCreateEventException
import project.planItAPI.utils.Failure
import project.planItAPI.utils.Success
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.InvalidCategoryException
import project.planItAPI.utils.InvalidPriceFormatException
import project.planItAPI.utils.InvalidSubcategoryException
import project.planItAPI.utils.InvalidTimestampFormatException
import project.planItAPI.utils.InvalidVisibilityException
import project.planItAPI.utils.Money
import project.planItAPI.utils.UserIDParameterMissing
import project.planItAPI.utils.SuccessMessage
import project.planItAPI.utils.UserAlreadyInEventException
import project.planItAPI.utils.UserIsNotOrganizerException
import project.planItAPI.utils.UserNotInEventException
import java.sql.Timestamp

@Service
class EventsServices(
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
        category: String,
        subcategory: String?,
        location: String?,
        visibility: String?,
        date: String?,
        endDate: String?,
        price: String?,
        userID: Int
    ): CreateEventResult =
        transactionManager.run {
            val priceValidation = parsePriceParameter(price)
            val errorList = validateEventInputs(subcategory, category, visibility, date, endDate, priceValidation)
            if (errorList.isNotEmpty()) throw HTTPCodeException(errorList.joinToString { err -> err.message }, 400)
            val eventsRepository = it.eventsRepository
            val eventID = eventsRepository.createEvent(
                title,
                description ?: "",
                category,
                subcategory,
                location ?: "To be Determined",
                visibility ?: "Public",
                if(date!=null) Timestamp.valueOf("$date:00") else null,
                if(endDate!=null) Timestamp.valueOf("$endDate:00") else null,
                if(priceValidation is Success) priceValidation.value else null,
                userID
            ) ?: throw FailedToCreateEventException()
            return@run CreateEventOutputModel(eventID, title, "Created with success.")
        }

    /**
     * Retrieves the event associated with the given ID.
     * @param id The ID of the event to retrieve.
     * @return [EventOutputModel] The event associated with the ID. If the event is not found, a [Failure] is thrown.
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
        return@run eventsRepository.searchEvents(searchInput)
    }

    /**
     * Allows a user to join an event.
     * @param userId The ID of the user joining the event.
     * @param eventId The ID of the event to join.
     * @return [JoinEventResult] A message indicating the success of the operation. If the event is not found, a [Failure] is thrown.
     */
    fun joinEvent(userId: Int, eventId: Int): JoinEventResult = transactionManager.run {
        val eventsRepository = it.eventsRepository
        val event = eventsRepository.getEvent(eventId) ?: throw EventNotFoundException()
        val usersInEvent = eventsRepository.getUsersInEvent(event.id)
        if(usersInEvent != null && usersInEvent.users.any { user -> user.id == userId }) {
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
        if (eventsRepository.getUsersInEvent(event.id)?.users?.any { user -> user.id == userId } != false) {
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
        category: String,
        subcategory: String?,
        location: String?,
        visibility: String?,
        date: String?,
        endDate: String?,
        price: String?
    ): EditEventResult = transactionManager.run {
        val priceValidation = parsePriceParameter(price)
        val errorList = validateEventInputs(subcategory, category, visibility, date, endDate, priceValidation)
        if (errorList.isNotEmpty()) throw HTTPCodeException(errorList.joinToString { err -> err.message }, 400)
        val eventsRepository = it.eventsRepository
        val event = eventsRepository.getEvent(eventId) ?: throw EventNotFoundException()
        if (eventsRepository.getEventOrganizer(eventId) != userId) throw UserIsNotOrganizerException()
        eventsRepository.editEvent(
            eventId,
            title,
            description ?: event.description,
            category,
            subcategory ?: event.subcategory,
            location ?: event.location,
            visibility ?: event.visibility,
            (if(date!=null) Timestamp.valueOf("$date:00") else event.date) as Timestamp?,
            (if(endDate!=null) Timestamp.valueOf("$endDate:00") else event.date) as Timestamp?,
            if(priceValidation is Success) priceValidation.value else null,
        )
        return@run SuccessMessage("Event edited with success.")
    }
}